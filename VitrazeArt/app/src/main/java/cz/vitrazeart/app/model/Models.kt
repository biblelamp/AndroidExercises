package cz.vitrazeart.app.model

// ─── Модели ───────────────────────────────────────────────────────────────────

data class Event(
    val title: String,
    val date: String,
    val description: String,
    val url: String,
    val imageUrl: String? = null
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