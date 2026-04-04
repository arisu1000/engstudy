package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * 학습 진행 시 연속 학습일(streak)을 갱신하는 UseCase.
 *
 * 오늘 처음 학습하면 streak을 증가시키고,
 * 하루 이상 건너뛰었으면 streak을 1로 초기화한다.
 * 같은 날 중복 호출 시에는 아무 일도 하지 않는다.
 */
class UpdateStreakUseCase @Inject constructor(
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke() {
        val lastStudyDate = userPreferences.lastStudyDate.first()
        val currentStreak = userPreferences.streakDays.first()

        val todayStart = todayStartMillis()
        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L

        val newStreak = when {
            // 오늘 이미 학습한 경우 - 변경 없음
            lastStudyDate >= todayStart -> return
            // 어제 학습한 경우 - streak 증가
            lastStudyDate >= yesterdayStart -> currentStreak + 1
            // 그 외 (처음이거나 하루 이상 건너뜀) - 1로 초기화
            else -> 1
        }

        userPreferences.updateStreak(newStreak, System.currentTimeMillis())
    }

    private fun todayStartMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
