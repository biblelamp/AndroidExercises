package cz.vitrazeart.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cz.vitrazeart.app.model.ContentBlock
import cz.vitrazeart.app.model.Event
import cz.vitrazeart.app.model.EventDetail
import cz.vitrazeart.app.model.TextSpan
import cz.vitrazeart.app.ui.VitrazeArtApp
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
            e.imageUrl?.let {
                put("imageUrl", it)
            }
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
                Event(
                    o.getString("title"),
                    o.getString("date"),
                    o.getString("description"),
                    o.getString("url"))
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
        val imageUrl    = card.selectFirst("div.col-md-4 img")?.attr("abs:src")
        if (title.isNotEmpty()) list.add(Event(title, datePlace, description, url, imageUrl))
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
