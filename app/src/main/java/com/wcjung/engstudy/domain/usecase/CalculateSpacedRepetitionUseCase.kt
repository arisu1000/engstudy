package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.SpacedRepetitionResult
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * SM-2 간격반복 알고리즘 구현.
 *
 * quality 등급:
 * 0 = 완전히 기억 못함
 * 1 = 틀렸지만 답을 보니 기억남
 * 2 = 틀렸지만 답이 익숙함
 * 3 = 맞았지만 매우 어려움
 * 4 = 맞았지만 약간 망설임
 * 5 = 완벽하게 기억
 */
class CalculateSpacedRepetitionUseCase @Inject constructor() {

    fun calculate(progress: LearningProgress, quality: Int): SpacedRepetitionResult {
        require(quality in 0..5) { "Quality must be between 0 and 5" }

        val newEaseFactor = maxOf(
            1.3f,
            progress.easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        )

        val (newInterval, newRepetitions) = if (quality >= 3) {
            val interval = when (progress.repetitions) {
                0 -> 1
                1 -> 6
                else -> (progress.intervalDays * newEaseFactor).roundToInt()
            }
            interval to (progress.repetitions + 1)
        } else {
            1 to 0
        }

        return SpacedRepetitionResult(
            easeFactor = newEaseFactor,
            intervalDays = newInterval,
            repetitions = newRepetitions,
            nextReviewDate = System.currentTimeMillis() + (newInterval * 86_400_000L),
            isLearned = newInterval >= 21
        )
    }

    /**
     * 간소화된 4단계 평가를 SM-2 quality로 변환.
     */
    fun qualityFromSimpleRating(rating: SimpleRating): Int = when (rating) {
        SimpleRating.AGAIN -> 1
        SimpleRating.HARD -> 3
        SimpleRating.GOOD -> 4
        SimpleRating.EASY -> 5
    }

    enum class SimpleRating { AGAIN, HARD, GOOD, EASY }
}
