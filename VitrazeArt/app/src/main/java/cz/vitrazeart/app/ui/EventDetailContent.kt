package cz.vitrazeart.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cz.vitrazeart.app.R
import cz.vitrazeart.app.model.ContentBlock
import cz.vitrazeart.app.model.EventDetail

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

        if (detail.imageUrl != null) {
            AsyncImage(
                model              = detail.imageUrl,
                contentDescription = detail.title,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale       = ContentScale.Crop,
                placeholder        = painterResource(R.mipmap.ic_launcher_round),
                error              = painterResource(R.mipmap.ic_launcher_round)
            )
        }

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