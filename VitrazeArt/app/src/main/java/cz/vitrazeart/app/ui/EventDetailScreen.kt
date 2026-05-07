package cz.vitrazeart.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.vitrazeart.app.data.loadEventDetail
import cz.vitrazeart.app.model.Event
import cz.vitrazeart.app.model.EventDetail
import cz.vitrazeart.app.loadCachedEventDetail
import cz.vitrazeart.app.saveEventDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    IconButton(onClick = onBack) {
                        AppIcon(modifier = Modifier.size(32.dp))
                    }
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