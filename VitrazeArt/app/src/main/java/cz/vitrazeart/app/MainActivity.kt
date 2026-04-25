package cz.vitrazeart.app

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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

const val MAIN_URL = "https://vitrazeart.cz/"
const val USER_AGENT = "Mozilla/5.0 (Android) VitrazeArtApp/1.0"

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VitrazeArtApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitrazeArtApp() {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        loadEvents { result ->
            result.onSuccess { list ->
                events = list
            }.onFailure {
                error = it.message
            }
            isLoading = false
        }
    }

    if (selectedEvent != null) {
        BackHandler { selectedEvent = null }
        EventDetailScreen(
            event = selectedEvent!!,
            onBack = { selectedEvent = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Пражские Витражи — Анонсы") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ошибка загрузки:\n$error", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
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

@Composable
fun EventCard(ann: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ann.date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ann.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ann.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text("Подробнее")
            }
        }
    }
}

// ─── Detail screen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(event: Event, onBack: () -> Unit) {
    var detail by remember { mutableStateOf<EventDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(event.url) {
        try {
            val loaded = withContext(Dispatchers.IO) { loadEventDetail(event.url) }
            detail = loaded
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.title ?: event.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ошибка загрузки:\n$error", color = MaterialTheme.colorScheme.error)
            }

            detail != null -> EventDetailContent(
                detail = detail!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EventDetailContent(detail: EventDetail, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date / place header
        if (detail.datePlace.isNotEmpty()) {
            Text(
                text = detail.datePlace,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Title
        Text(
            text = detail.title,
            style = MaterialTheme.typography.headlineSmall
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Rich content blocks
        detail.contentBlocks.forEach { block ->
            when (block) {
                is ContentBlock.HorizontalRule -> HorizontalDivider()
                is ContentBlock.Paragraph -> {
                    val annotated = buildAnnotatedString {
                        block.spans.forEach { span ->
                            if (span.href != null) {
                                pushStringAnnotation(tag = "URL", annotation = span.href)
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) { append(span.text) }
                                pop()
                            } else if (span.bold) {
                                withStyle(SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                                    append(span.text)
                                }
                            } else {
                                append(span.text)
                            }
                        }
                    }
                    ClickableText(
                        text = annotated,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        ),
                        onClick = { offset ->
                            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Network / parsing ────────────────────────────────────────────────────────

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
        val title = card.selectFirst("h5.card-title")?.text()?.trim() ?: ""
        val datePlace = card.selectFirst(".text-muted.small.mb-2")?.text()?.trim() ?: ""
        val description = card.selectFirst("p.card-text.text-muted.mb-3")?.text()?.trim() ?: ""
        val url = card.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""
        if (title.isNotEmpty()) list.add(Event(title, datePlace, description, url))
    }

    doc.select("div.border-bottom.py-3").forEach { block ->
        val title = block.selectFirst("h5.mb-1, h5")?.text()?.trim() ?: ""
        val datePlace = block.selectFirst(".text-muted.small.mb-1")?.text()?.trim() ?: ""
        val description = block.selectFirst("p.text-muted.mb-2")?.ownText()?.trim() ?: ""
        val url = block.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""
        if (title.isNotEmpty()) list.add(Event(title, datePlace, description, url))
    }

    return list
}

fun loadEventDetail(url: String): EventDetail {
    val doc = Jsoup.connect(url)
        .userAgent(USER_AGENT)
        .timeout(15000)
        .get()

    // Title: <h5 class="card-title ...">
    val title = doc.selectFirst("h5.card-title")?.text()?.trim()
        ?: doc.title().substringBefore("–").trim()

    // Date + place: .text-muted.small.mb-2  (first card in main content)
    val datePlace = doc.selectFirst(".text-muted.small.mb-2")?.text()?.trim() ?: ""

    // Hero image
    val imageUrl = doc.selectFirst("div.card img.img-fluid.event-img")?.attr("abs:src")

    // Main content: div.report-content
    val contentEl = doc.selectFirst("div.report-content")
    val blocks = mutableListOf<ContentBlock>()

    // Walk all direct child nodes of report-content, grouping consecutive
    // inline nodes (TextNode, <a>, <strong>, <br> etc.) into one paragraph.
    // Block elements (<p>, <hr>, headings) flush the current inline group first.
    val inlineBuffer = mutableListOf<org.jsoup.nodes.Node>()

    fun flushInlineBuffer() {
        if (inlineBuffer.isEmpty()) return
        val spans = mutableListOf<TextSpan>()
        fun walkInline(node: org.jsoup.nodes.Node, bold: Boolean, href: String?) {
            when (node) {
                is TextNode -> {
                    val t = node.text()
                    if (t.isNotEmpty()) spans.add(TextSpan(t, href = href, bold = bold))
                }
                is Element -> {
                    when (node.tagName()) {
                        "br" -> spans.add(TextSpan("\n"))
                        else -> {
                            val newHref = if (node.tagName() == "a") node.attr("abs:href").ifEmpty { null } else href
                            val newBold = bold || node.tagName() == "strong" || node.tagName() == "b"
                            node.childNodes().forEach { walkInline(it, newBold, newHref) }
                        }
                    }
                }
            }
        }
        inlineBuffer.forEach { walkInline(it, bold = false, href = null) }
        inlineBuffer.clear()
        val trimmed = spans.dropWhile { it.text.isBlank() }.dropLastWhile { it.text.isBlank() }
        if (trimmed.isNotEmpty()) blocks.add(ContentBlock.Paragraph(trimmed))
    }

    val blockTags = setOf("p", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "div", "ul", "ol", "li")

    contentEl?.childNodes()?.forEach { node ->
        val tag = (node as? Element)?.tagName() ?: ""
        if (tag in blockTags) {
            flushInlineBuffer()
            when {
                tag == "hr" -> blocks.add(ContentBlock.HorizontalRule)
                tag == "p" -> parseInlineBlocks(node as Element, blocks)
                tag in listOf("h1","h2","h3","h4","h5","h6") ->
                    blocks.add(ContentBlock.Paragraph(listOf(TextSpan((node as Element).text(), bold = true))))
                else -> parseInlineBlocks(node as Element, blocks)
            }
        } else {
            // inline node (TextNode or inline element like <a>) — buffer it
            inlineBuffer.add(node)
        }
    }
    flushInlineBuffer()

    return EventDetail(title, datePlace, imageUrl, blocks)
}

/**
 * Walks all child nodes of a <p> element.
 * When an <hr> is encountered mid-paragraph, the current spans are flushed
 * as a Paragraph block, an HorizontalRule is added, then collection resumes.
 */
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
            is TextNode -> {
                val t = node.text()
                if (t.isNotEmpty()) current.add(TextSpan(t, href = href, bold = bold))
            }
            is Element -> {
                when (node.tagName()) {
                    "br" -> current.add(TextSpan("\n", href = null, bold = false))
                    "hr" -> {
                        flush()
                        blocks.add(ContentBlock.HorizontalRule)
                    }
                    else -> {
                        val newHref = if (node.tagName() == "a") node.attr("abs:href").ifEmpty { null } else href
                        val newBold = bold || node.tagName() == "strong" || node.tagName() == "b"
                        node.childNodes().forEach { walk(it, newBold, newHref) }
                    }
                }
            }
        }
    }

    el.childNodes().forEach { walk(it, bold = false, href = null) }
    flush()
}
