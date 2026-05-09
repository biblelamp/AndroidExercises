package cz.vitrazeart.app.data

import cz.vitrazeart.app.MAIN_URL
import cz.vitrazeart.app.TIMEOUT
import cz.vitrazeart.app.USER_AGENT
import cz.vitrazeart.app.model.ContentBlock
import cz.vitrazeart.app.model.Event
import cz.vitrazeart.app.model.EventDetail
import cz.vitrazeart.app.model.TextSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

suspend fun loadEvents(): Result<List<Event>> = runCatching {
    withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(MAIN_URL)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT)
            .get()
        parseEvents(doc)
    }
}

fun loadEventDetail(url: String): EventDetail {
    val doc = Jsoup.connect(url)
        .userAgent(USER_AGENT)
        .timeout(TIMEOUT)
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