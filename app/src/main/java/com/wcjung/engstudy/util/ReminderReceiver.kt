package com.wcjung.engstudy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wcjung.engstudy.domain.repository.LearningRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var learningRepository: LearningRepository

    override fun onReceive(context: Context, intent: Intent) {
        // 복습 예정 개수를 조회해 구체적인 문구로 알림을 표시한다.
        // DB 조회는 비동기이므로 goAsync로 브로드캐스트 수명을 연장한다.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dueCount = runCatching { learningRepository.getDueReviewCount().first() }
                    .getOrDefault(0)
                notificationHelper.showReminderNotification(dueCount)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
