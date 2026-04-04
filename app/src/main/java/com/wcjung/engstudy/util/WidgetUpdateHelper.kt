package com.wcjung.engstudy.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * SharedPreferences 기반 위젯 데이터 관리.
 * HomeViewModel에서 오늘의 단어를 불러올 때 호출하여 위젯에 반영한다.
 */
object WidgetUpdateHelper {

    private const val PREFS_NAME = "word_widget_prefs"
    private const val KEY_WORD = "widget_word"
    private const val KEY_PRONUNCIATION = "widget_pronunciation"
    private const val KEY_MEANING = "widget_meaning"

    fun updateWidgetData(
        context: Context,
        word: String,
        pronunciation: String,
        meaning: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WORD, word)
            .putString(KEY_PRONUNCIATION, pronunciation)
            .putString(KEY_MEANING, meaning)
            .apply()

        // 위젯 갱신 브로드캐스트 전송
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = ComponentName(context, WordWidgetReceiver::class.java)
        }
        context.sendBroadcast(intent)
    }

    fun getWidgetData(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Triple(
            prefs.getString(KEY_WORD, "EngStudy") ?: "EngStudy",
            prefs.getString(KEY_PRONUNCIATION, "") ?: "",
            prefs.getString(KEY_MEANING, "앱을 열어 오늘의 단어를 확인하세요") ?: ""
        )
    }
}
