package cz.vitrazeart.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cz.vitrazeart.app.model.Event

// ─── Карточка анонса ──────────────────────────────────────────────────────────

@Composable
fun EventCard(event: Event, onClick: () -> Unit, showImage: Boolean = false, showButton: Boolean = false) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showImage && event.imageUrl != null) {
                AsyncImage(
                    model              = event.imageUrl,
                    contentDescription = event.title,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale       = ContentScale.Crop,
                    //placeholder        = painterResource(R.mipmap.ic_launcher_round),
                    //error              = painterResource(R.mipmap.ic_launcher_round)
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(event.date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(event.title,
                style    = MaterialTheme.typography.titleLarge,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            val description = buildAnnotatedString {
                append(event.description)
                if (!showButton) {
                    append(" ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Подробнее →")
                    }
                }
            }
            Text(description,
                style    = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (showButton) {
                //Spacer(Modifier.height(8.dp))
                Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                    Text("Подробнее")
                }
            }
        }
    }
}