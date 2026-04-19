package cz.vitrazeart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class Announcement(
    val title: String,
    val date: String,
    val description: String,
    val url: String
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
    val uriHandler = LocalUriHandler.current
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadAnnouncements { result ->
            result.onSuccess { list ->
                announcements = list
            }.onFailure {
                error = it.message
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пражские Витражи — Анонсы") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                    items(announcements) { ann ->
                        AnnouncementCard(ann, onClick = { uriHandler.openUri(ann.url) })
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(ann: Announcement, onClick: () -> Unit) {
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

suspend fun loadAnnouncements(onResult: (Result<List<Announcement>>) -> Unit) {
    try {
        val list = withContext(Dispatchers.IO) {
            val doc = Jsoup.connect("https://vitrazeart.cz/")
                .userAgent("Mozilla/5.0 (Android) VitrazeArtApp/1.0")
                .timeout(15000)
                .get()

            parseAnnouncements(doc)
        }
        onResult(Result.success(list))
    } catch (e: Exception) {
        onResult(Result.failure(e))
    }
}

private fun parseAnnouncements(doc: Document): List<Announcement> {
    val list = mutableListOf<Announcement>()

    // 1. Первый большой блок с картинкой
    doc.select("div.card.mb-4.border-0.shadow-sm.overflow-hidden").forEach { card ->
        val title = card.selectFirst("h5.card-title")?.text()?.trim() ?: ""
        val datePlace = card.selectFirst(".text-muted.small.mb-2")?.text()?.trim() ?: ""
        val description = card.selectFirst("p.card-text.text-muted.mb-3")?.text()?.trim() ?: ""
        val url = card.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""

        if (title.isNotEmpty()) {
            list.add(Announcement(title, datePlace, description, url))
        }
    }

    // 2. Все остальные анонсы (border-bottom)
    doc.select("div.border-bottom.py-3").forEach { block ->
        val title = block.selectFirst("h5.mb-1, h5")?.text()?.trim() ?: ""
        val datePlace = block.selectFirst(".text-muted.small.mb-1")?.text()?.trim() ?: ""
        val description = block.selectFirst("p.text-muted.mb-2")?.ownText()?.trim() ?: ""
        val url = block.selectFirst("a[href^='/events/']")?.attr("abs:href") ?: ""

        if (title.isNotEmpty()) {
            list.add(Announcement(title, datePlace, description, url))
        }
    }

    return list
}