package cz.vitrazeart.app.data

import android.content.Context
import cz.vitrazeart.app.model.ContentBlock
import cz.vitrazeart.app.model.Event
import cz.vitrazeart.app.model.EventDetail
import cz.vitrazeart.app.model.TextSpan
import org.json.JSONArray
import org.json.JSONObject

class CacheManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─── Список анонсов ───────────────────────────────────────────────────────

    fun saveEventsList(events: List<Event>) {
        val arr = JSONArray()
        events.forEach { e ->
            arr.put(JSONObject().apply {
                put("title",       e.title)
                put("date",        e.date)
                put("description", e.description)
                put("url",         e.url)
                e.imageUrl?.let { put("imageUrl", it) }
            })
        }
        prefs.edit().putString(KEY_EVENTS_LIST, arr.toString()).apply()
    }

    fun loadEventsList(): List<Event>? {
        val json = prefs.getString(KEY_EVENTS_LIST, null) ?: return null
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                arr.getJSONObject(i).let { o ->
                    Event(
                        title       = o.getString("title"),
                        date        = o.getString("date"),
                        description = o.getString("description"),
                        url         = o.getString("url"),
                        imageUrl    = if (o.has("imageUrl")) o.getString("imageUrl") else null
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("CacheManager", "Failed to parse events list", e)
            null
        }
    }

    // ─── Детали анонса ────────────────────────────────────────────────────────

    fun saveEventDetail(url: String, detail: EventDetail) {
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
            detail.imageUrl?.let { put("imageUrl", it) }
            put("blocks", blocksArr)
        }
        prefs.edit().putString(detailKey(url), obj.toString()).apply()
    }

    fun loadEventDetail(url: String): EventDetail? {
        val json = prefs.getString(detailKey(url), null) ?: return null
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
        } catch (e: Exception) {
            android.util.Log.w("CacheManager", "Failed to parse event detail for $url", e)
            null
        }
    }

    // ─── Внутренние ───────────────────────────────────────────────────────────

    private fun detailKey(url: String): String =
        KEY_DETAIL_PFX + url.hashCode()

    companion object {
        private const val PREFS_NAME      = "vitrazeart_cache"
        private const val KEY_EVENTS_LIST = "events_list"
        private const val KEY_DETAIL_PFX  = "detail_"
    }
}