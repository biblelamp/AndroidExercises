package cz.vitrazeart.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

const val MAIN_URL = "https://vitrazeart.cz/"
const val USER_AGENT = "Mozilla/5.0 (Android) VitrazeArtApp/1.0"

// ─── Константы кэша ───────────────────────────────────────────────────────────

private const val PREFS_NAME      = "vitrazeart_cache"
private const val KEY_EVENTS_LIST = "events_list"
private const val KEY_DETAIL_PFX  = "detail_"

// ─── Модели ───────────────────────────────────────────────────────────────────

data class Event(
    val title: String,
    val date: String,
    val description: String,
    val url: String
)

data class EventDetail(
    val title: String,
    val datePlace: String,
    val imageUrl: String?,
    val contentBlocks: List<ContentBlock>
)

sealed class ContentBlock {
    data class Paragraph(val spans: List<TextSpan>) : ContentBlock()
    object HorizontalRule : ContentBlock()
}

data class TextSpan(
    val text: String,
    val href: String? = null,
    val bold: Boolean = false
)

// ─── Кэш: список анонсов ─────────────────────────────────────────────────────
// SharedPreferences хранятся на диске устройства и переживают закрытие
// приложения, перезагрузку телефона и т.д.
// Данные удаляются только при удалении приложения или ручной очистке.

fun saveEventsList(context: Context, events: List<Event>) {
    val arr = JSONArray()
    events.forEach { e ->
        arr.put(JSONObject().apply {
            put("title",       e.title)
            put("date",        e.date)
            put("description", e.description)
            put("url",         e.url)
        })
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_EVENTS_LIST, arr.toString()).apply()
}

fun loadCachedEventsList(context: Context): List<Event>? {
    val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_EVENTS_LIST, null) ?: return null
    return try {
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            arr.getJSONObject(i).let { o ->
                Event(o.getString("title"), o.getString("date"),
                      o.getString("description"), o.getString("url"))
            }
        }
    } catch (_: Exception) { null }
}

// ─── Кэш: детали анонса ───────────────────────────────────────────────────────

fun saveEventDetail(context: Context, url: String, detail: EventDetail) {
    val blocksArr = JSONArray()
    detail.contentBlocks.forEach { block ->
        when (block) {
            is ContentBlock.HorizontalRule ->
                blocksArr.put(JSONObject().put("type", "hr"))
            is ContentBlock.Paragraph -> {
                val spansArr = JSONArray()
                block.spans.forEach { span ->
                    spansArr.put(JSONObject().apply {
                        put("text", span.text)
                        span.href?.let { put("href", it) }
                        put("bold", span.bold)
                    })
                }
                blocksArr.put(JSONObject().apply {
                    put("type",  "p")
                    put("spans", spansArr)
                })
            }
        }
    }
    val obj = JSONObject().apply {
        put("title",     detail.title)
        put("datePlace", detail.datePlace)
        detail.imageUrl?.let { put("imageUrl", it) }
        put("blocks", blocksArr)
    }
    val key = KEY_DETAIL_PFX + url.hashCode()
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(key, obj.toString()).apply()
}

fun loadCachedEventDetail(context: Context, url: String): EventDetail? {
    val key  = KEY_DETAIL_PFX + url.hashCode()
    val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(key, null) ?: return null
    return try {
        val obj       = JSONObject(json)
        val blocksArr = obj.getJSONArray("blocks")
        val blocks = List(blocksArr.length()) { i ->
            val b = blocksArr.getJSONObject(i)
            when (b.getString("type")) {
                "hr" -> ContentBlock.HorizontalRule
                else -> {
                    val spansArr = b.getJSONArray("spans")
                    ContentBlock.Paragraph(
                        List(spansArr.length()) { j ->
                            val s = spansArr.getJSONObject(j)
                            TextSpan(
                                text = s.getString("text"),
                                href = if (s.has("href")) s.getString("href") else null,
                                bold = s.getBoolean("bold")
                            )
                        }
                    )
                }
            }
        }
        EventDetail(
            title         = obj.getString("title"),
            datePlace     = obj.getString("datePlace"),
            imageUrl      = if (obj.has("imageUrl")) obj.getString("imageUrl") else null,
            contentBlocks = blocks
        )
    } catch (_: Exception) { null }
}

// ─── Activity ─────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VitrazeArtApp() }
    }
}

// ─── Главный экран ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitrazeArtApp() {
    val context = LocalContext.current

    var events        by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var error         by remember { mutableStateOf<String?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    // true — сеть недоступна, показываем сохранённый список
    var fromCache     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 1. Немедленно показываем кэш, пока сеть ещё не ответила
        val cached = withContext(Dispatchers.IO) { loadCachedEventsList(context) }
        if (cached != null) {
            events    = cached
            fromCache = true
            isLoading = false
        }

        // 2. Загружаем свежие данные из сети
        loadEvents { result ->
            result.onSuccess { list ->
                events    = list
                fromCache = false
                saveEventsList(context, list)   // обновляем кэш
            }.onFailure { err ->
                // Кэш уже показан — просто молчим.
                // Только если вообще нечего показать — выводим ошибку.
                if (events.isEmpty()) error = err.message
            }
            isLoading = false
        }
    }

    if (selectedEvent != null) {
        BackHandler { selectedEvent = null }
        EventDetailScreen(
            event  = selectedEvent!!,
            onBack = { selectedEvent = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title  = { Text("Пражские Витражи — Анонсы") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            when {
                isLoading && events.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null && events.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ошибка загрузки:\n$error",
                             color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        if (fromCache) {
                            Surface(
                                color    = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text     = "⚠ Нет соединения — показаны сохранённые данные",
                                    style    = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events) { ann ->
                                EventCard(ann, onClick = { selectedEvent = ann })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Карточка анонса ──────────────────────────────────────────────────────────

@Composable
fun EventCard(ann: Event, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(ann.date,
                 style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(ann.title,
                 style    = MaterialTheme.typography.titleLarge,
                 maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(ann.description,
                 style    = MaterialTheme.typography.bodyMedium,
                 maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text("Подробнее")
            }
        }
    }
}

// ─── Экран деталей ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(event: Event, onBack: () -> Unit) {
    val context = LocalContext.current

    var detail    by remember { mutableStateOf<EventDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }

    LaunchedEffect(event.url) {
        // 1. Мгновенно показываем кэш (если анонс уже открывался ранее,
        //    в том числе в прошлых сессиях приложения)
        val cached = withContext(Dispatchers.IO) { loadCachedEventDetail(context, event.url) }
        if (cached != null) {
            detail    = cached
            fromCache = true
            isLoading = false
        }

        // 2. Загружаем свежие данные из сети
        try {
            val loaded = withContext(Dispatchers.IO) { loadEventDetail(event.url) }
            detail    = loaded
            fromCache = false
            // Сохраняем/обновляем кэш после каждого успешного запроса
            withContext(Dispatchers.IO) { saveEventDetail(context, event.url, loaded) }
        } catch (e: Exception) {
            if (detail == null) error = e.message   // показываем ошибку только если кэша нет
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(detail?.title ?: event.title,
                         maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when {
            isLoading && detail == null -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null && detail == null -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text("Ошибка загрузки:\n$error",
                     color = MaterialTheme.colorScheme.error)
            }

            detail != null -> Column(Modifier.padding(padding)) {
                if (fromCache) {
                    Surface(
                        color    = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "⚠ Нет соединения — показан сохранённый анонс",
                            style    = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                EventDetailContent(detail = detail!!)
            }
        }
    }
}

// ─── Содержимое деталей ───────────────────────────────────────────────────────

@Composable
fun EventDetailContent(detail: EventDetail, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier            = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (detail.datePlace.isNotEmpty()) {
            Text(detail.datePlace,
                 style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.primary)
        }
        Text(detail.title, style = MaterialTheme.typography.headlineSmall)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        detail.contentBlocks.forEach { block ->
            when (block) {
                is ContentBlock.HorizontalRule -> HorizontalDivider()
                is ContentBlock.Paragraph -> {
                    val annotated = buildAnnotatedString {
                        block.spans.forEach { span ->
                            if (span.href != null) {
                                pushStringAnnotation("URL", span.href)
                                withStyle(SpanStyle(
                                    color          = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )) { append(span.text) }
                                pop()
                            } else if (span.bold) {
                                withStyle(SpanStyle(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )) { append(span.text) }
                            } else {
                                append(span.text)
                            }
                        }
                    }
                    ClickableText(
                        text  = annotated,
                        style = TextStyle(
                            color      = MaterialTheme.colorScheme.onSurface,
                            fontSize   = MaterialTheme.typography.bodyMedium.fontSize,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        ),
                        onClick = { offset ->
                            annotated.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ─── Сеть / парсинг ───────────────────────────────────────────────────────────

suspend fun loadEvents(onResult: (Result<List<Event>>) -> Unit) {
    try {
        val list = withContext(Dispatchers.IO) {
            val doc = Jsoup.connect(MAIN_URL)
                .userAgent(USER_AGENT)
                .timeout(15000)
                .get()
            parseEvents(doc)
        }
        onResult(Result.success(list))
    } catch (e: Exception) {
        onResult(Result.failure(e))
    }
}

private fun parseEvents(doc: Document): List<Event> {
    val list = mutableListOf<Event>()

    doc.select("div.card.mb-4.border-0.shadow-sm.overflow-hidden").forEach { card ->
        val title       = card.selectFirst("h5.card-title")?.text()?.trim() ?: ""
        val datePlace   = card.selectFirst(".text-muted.small.mb-2")?.text()?.trim() ?: ""
        val description = card.selectFirst("p.card-text.text-muted.mb-3")?.text()?.trim() ?: ""
        val url         = card.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""
        if (title.isNotEmpty()) list.add(Event(title, datePlace, description, url))
    }

    doc.select("div.border-bottom.py-3").forEach { block ->
        val title       = block.selectFirst("h5.mb-1, h5")?.text()?.trim() ?: ""
        val datePlace   = block.selectFirst(".text-muted.small.mb-1")?.text()?.trim() ?: ""
        val description = block.selectFirst("p.text-muted.mb-2")?.ownText()?.trim() ?: ""
        val url         = block.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""
        if (title.isNotEmpty()) list.add(Event(title, datePlace, description, url))
    }

    return list
}

fun loadEventDetail(url: String): EventDetail {
    val doc = Jsoup.connect(url)
        .userAgent(USER_AGENT)
        .timeout(15000)
        .get()

    val title     = doc.selectFirst("h5.card-title")?.text()?.trim()
        ?: doc.title().substringBefore("–").trim()
    val datePlace = doc.selectFirst(".text-muted.small.mb-2")?.text()?.trim() ?: ""
    val imageUrl  = doc.selectFirst("div.card img.img-fluid.event-img")?.attr("abs:src")
    val contentEl = doc.selectFirst("div.report-content")
    val blocks    = mutableListOf<ContentBlock>()

    val inlineBuffer = mutableListOf<org.jsoup.nodes.Node>()

    fun flushInlineBuffer() {
        if (inlineBuffer.isEmpty()) return
        val spans = mutableListOf<TextSpan>()
        fun walkInline(node: org.jsoup.nodes.Node, bold: Boolean, href: String?) {
            when (node) {
                is TextNode -> { val t = node.text(); if (t.isNotEmpty()) spans.add(TextSpan(t, href, bold)) }
                is Element  -> when (node.tagName()) {
                    "br"  -> spans.add(TextSpan("\n"))
                    else  -> {
                        val newHref = if (node.tagName() == "a") node.attr("abs:href").ifEmpty { null } else href
                        val newBold = bold || node.tagName() == "strong" || node.tagName() == "b"
                        node.childNodes().forEach { walkInline(it, newBold, newHref) }
                    }
                }
            }
        }
        inlineBuffer.forEach { walkInline(it, false, null) }
        inlineBuffer.clear()
        val trimmed = spans.dropWhile { it.text.isBlank() }.dropLastWhile { it.text.isBlank() }
        if (trimmed.isNotEmpty()) blocks.add(ContentBlock.Paragraph(trimmed))
    }

    val blockTags = setOf("p","hr","h1","h2","h3","h4","h5","h6","div","ul","ol","li")

    contentEl?.childNodes()?.forEach { node ->
        val tag = (node as? Element)?.tagName() ?: ""
        if (tag in blockTags) {
            flushInlineBuffer()
            when {
                tag == "hr" -> blocks.add(ContentBlock.HorizontalRule)
                tag == "p"  -> parseInlineBlocks(node as Element, blocks)
                tag in listOf("h1","h2","h3","h4","h5","h6") ->
                    blocks.add(ContentBlock.Paragraph(
                        listOf(TextSpan((node as Element).text(), bold = true))
                    ))
                else -> parseInlineBlocks(node as Element, blocks)
            }
        } else {
            inlineBuffer.add(node)
        }
    }
    flushInlineBuffer()

    return EventDetail(title, datePlace, imageUrl, blocks)
}

private fun parseInlineBlocks(el: Element, blocks: MutableList<ContentBlock>) {
    val current = mutableListOf<TextSpan>()

    fun flush() {
        if (current.isNotEmpty()) {
            blocks.add(ContentBlock.Paragraph(current.toList()))
            current.clear()
        }
    }

    fun walk(node: org.jsoup.nodes.Node, bold: Boolean, href: String?) {
        when (node) {
            is TextNode -> { val t = node.text(); if (t.isNotEmpty()) current.add(TextSpan(t, href, bold)) }
            is Element  -> when (node.tagName()) {
                "br" -> current.add(TextSpan("\n", null, false))
                "hr" -> { flush(); blocks.add(ContentBlock.HorizontalRule) }
                else -> {
                    val newHref = if (node.tagName() == "a") node.attr("abs:href").ifEmpty { null } else href
                    val newBold = bold || node.tagName() == "strong" || node.tagName() == "b"
                    node.childNodes().forEach { walk(it, newBold, newHref) }
                }
            }
        }
    }

    el.childNodes().forEach { walk(it, false, null) }
    flush()
}
