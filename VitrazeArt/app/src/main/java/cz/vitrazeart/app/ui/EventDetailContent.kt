package cz.vitrazeart.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cz.vitrazeart.app.ContentBlock
import cz.vitrazeart.app.EventDetail

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