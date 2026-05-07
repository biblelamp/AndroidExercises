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
                    o.getString("url"),
                    if (o.has("imageUrl")) o.getString("imageUrl") else null
                )
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
        detail.imageUrl?.let {
            put("imageUrl", it)
        }
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
