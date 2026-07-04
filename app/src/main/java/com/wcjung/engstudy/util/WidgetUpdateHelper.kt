package com.wcjung.engstudy.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** 위젯에 표시할 단어 한 건. */
@Serializable
data class WidgetWord(
    val id: Int,
    val word: String,
    val pronunciation: String,
    val meaning: String
)

/**
 * SharedPreferences 기반 위젯 데이터 관리.
 *
 * 홈 화면 진입 시 오늘의 단어 + 랜덤 단어 목록을 캐시해 두고,
 * 위젯의 "다음 단어" 버튼이 앱/DB 접근 없이 목록을 순환한다 (오프라인 안전).
 */
object WidgetUpdateHelper {

    private const val PREFS_NAME = "word_widget_prefs"
    private const val KEY_WORDS_JSON = "widget_words_json"
    private const val KEY_INDEX = "widget_index"

    private val json = Json { ignoreUnknownKeys = true }

    /** 순환 목록을 교체하고 첫 단어부터 표시한다. */
    fun updateWidgetWords(context: Context, words: List<WidgetWord>) {
        if (words.isEmpty()) return
        prefs(context).edit()
            .putString(KEY_WORDS_JSON, json.encodeToString(words))
            .putInt(KEY_INDEX, 0)
            .apply()
        broadcastUpdate(context)
    }

    /** "다음 단어" 버튼: 목록을 한 칸 순환한다 (끝에서 처음으로 되돌아감). */
    fun advanceToNextWord(context: Context) {
        val size = loadWords(context).size
        if (size == 0) return
        val next = (prefs(context).getInt(KEY_INDEX, 0) + 1) % size
        prefs(context).edit().putInt(KEY_INDEX, next).apply()
    }

    /** 현재 표시할 단어. 캐시가 없으면 null (위젯은 플레이스홀더 표시). */
    fun getCurrentWord(context: Context): WidgetWord? {
        val words = loadWords(context)
        if (words.isEmpty()) return null
        val index = prefs(context).getInt(KEY_INDEX, 0).coerceIn(0, words.size - 1)
        return words[index]
    }

    private fun loadWords(context: Context): List<WidgetWord> {
        val raw = prefs(context).getString(KEY_WORDS_JSON, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<WidgetWord>>(raw) }
            .getOrDefault(emptyList())
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun broadcastUpdate(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetReceiver::class.java))
        if (ids.isEmpty()) return
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = ComponentName(context, WordWidgetReceiver::class.java)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
