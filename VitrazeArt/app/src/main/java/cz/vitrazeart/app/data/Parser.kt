package cz.vitrazeart.app.data

import cz.vitrazeart.app.model.ContentBlock
import cz.vitrazeart.app.model.Event
import cz.vitrazeart.app.model.TextSpan
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun parseEvents(doc: Document): List<Event> {
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

fun parseInlineBlocks(el: Element, blocks: MutableList<ContentBlock>) {
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