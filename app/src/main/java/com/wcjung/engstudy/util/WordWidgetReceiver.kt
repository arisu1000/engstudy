package com.wcjung.engstudy.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.wcjung.engstudy.MainActivity
import com.wcjung.engstudy.R

/**
 * 홈 화면 "오늘의 단어" 위젯.
 * RemoteViews 기반으로 SharedPreferences 캐시(WidgetUpdateHelper)에서 데이터를 읽어 표시한다.
 *
 * - 단어 영역 탭 → 앱의 해당 단어 상세 화면으로 이동
 * - "다음 단어" 탭 → 캐시된 목록을 순환 (앱 실행/DB 접근 없음)
 */
class WordWidgetReceiver : AppWidgetProvider() {

    companion object {
        const val ACTION_NEXT_WORD = "com.wcjung.engstudy.widget.NEXT_WORD"
        const val EXTRA_WORD_ID = "widget_word_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NEXT_WORD) {
            WidgetUpdateHelper.advanceToNextWord(context)
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetReceiver::class.java))
            onUpdate(context, manager, ids)
            return
        }
        super.onReceive(context, intent)
    }

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
        val current = WidgetUpdateHelper.getCurrentWord(context)

        val views = RemoteViews(context.packageName, R.layout.widget_placeholder).apply {
            setTextViewText(R.id.widget_word, current?.word ?: "EngStudy")
            setTextViewText(R.id.widget_pronunciation, current?.pronunciation ?: "")
            setTextViewText(
                R.id.widget_meaning,
                current?.meaning ?: "앱을 열어 오늘의 단어를 확인하세요"
            )

            // 단어 탭 → 해당 단어 상세로 (캐시 없으면 홈으로)
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                current?.let { putExtra(EXTRA_WORD_ID, it.id) }
            }
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                // 단어가 바뀌면 extra도 바뀌므로 requestCode로 구분해 캐시 충돌 방지
                current?.id ?: 0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_word, launchPendingIntent)
            setOnClickPendingIntent(R.id.widget_pronunciation, launchPendingIntent)
            setOnClickPendingIntent(R.id.widget_meaning, launchPendingIntent)

            // "다음 단어" 탭 → 브로드캐스트로 목록 순환
            val nextIntent = Intent(context, WordWidgetReceiver::class.java).apply {
                action = ACTION_NEXT_WORD
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_next, nextPendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
