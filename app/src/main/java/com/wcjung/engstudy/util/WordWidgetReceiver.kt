package com.wcjung.engstudy.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.wcjung.engstudy.MainActivity
import com.wcjung.engstudy.R

/**
 * 홈 화면 "오늘의 단어" 위젯.
 * RemoteViews 기반으로 SharedPreferences에서 데이터를 읽어 표시한다.
 */
class WordWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val (word, pronunciation, meaning) = WidgetUpdateHelper.getWidgetData(context)

        val views = RemoteViews(context.packageName, R.layout.widget_placeholder).apply {
            setTextViewText(R.id.widget_word, word)
            setTextViewText(R.id.widget_pronunciation, pronunciation)
            setTextViewText(R.id.widget_meaning, meaning)

            // 위젯 클릭 시 앱 실행
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_word, pendingIntent)
            setOnClickPendingIntent(R.id.widget_pronunciation, pendingIntent)
            setOnClickPendingIntent(R.id.widget_meaning, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
