package cz.vitrazeart.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cz.vitrazeart.app.Event
import cz.vitrazeart.app.MAIN_URL
import cz.vitrazeart.app.loadCachedEventsList
import cz.vitrazeart.app.loadEvents
import cz.vitrazeart.app.saveEventsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─── Главный экран ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitrazeArtApp() {
    val context = LocalContext.current

    var events        by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var error         by remember { mutableStateOf<String?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    // true — сеть недоступна, показываем сохранённый список
    var fromCache     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 1. Немедленно показываем кэш, пока сеть ещё не ответила
        val cached = withContext(Dispatchers.IO) { loadCachedEventsList(context) }
        if (cached != null) {
            events    = cached
            fromCache = true
            isLoading = false
        }

        // 2. Загружаем свежие данные из сети
        loadEvents { result ->
            result.onSuccess { list ->
                events    = list
                fromCache = false
                saveEventsList(context, list)   // обновляем кэш
            }.onFailure { err ->
                // Кэш уже показан — просто молчим.
                // Только если вообще нечего показать — выводим ошибку.
                if (events.isEmpty()) error = err.message
            }
            isLoading = false
        }
    }

    if (selectedEvent != null) {
        BackHandler { selectedEvent = null }
        EventDetailScreen(
            event  = selectedEvent!!,
            onBack = { selectedEvent = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppIcon(modifier = Modifier.size(32.dp))
                            Text("Пражские Витражи — Анонсы")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            when {
                isLoading && events.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null && events.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ошибка загрузки:\n$error",
                            color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        if (fromCache) {
                            Surface(
                                color    = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text     = "⚠ Нет соединения — показаны сохранённые данные",
                                    style    = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events) { ann ->
                                EventCard(ann, onClick = { selectedEvent = ann })
                            }
                            item {
                                val uriHandler = LocalUriHandler.current
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(onClick = { uriHandler.openUri(MAIN_URL) }) {
                                        Text("Перейти на сайт")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}