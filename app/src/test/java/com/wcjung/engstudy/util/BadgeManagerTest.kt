package com.wcjung.engstudy.util

import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.domain.model.StudyStatistics
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BadgeManager 뱃지 판정 로직 테스트.
 *
 * 뱃지 획득 임계값(첫 걸음, 연속 학습, 단어 마스터, 스테이지 완료) 경계를
 * 성공/경계/실패 케이스로 검증한다.
 */
class BadgeManagerTest {

    private fun stats(
        learnedWords: Int = 0,
        streakDays: Int = 0,
        learnedByStage: Map<Stage, Int> = emptyMap()
    ) = StudyStatistics(
        totalWords = 12068,
        learnedWords = learnedWords,
        inProgressWords = 0,
        dueReviews = 0,
        totalStudyDays = 0,
        todayReviewedCount = 0,
        streakDays = streakDays,
        learnedByStage = learnedByStage,
        learnedByDomain = emptyMap()
    )

    private fun earned(stats: StudyStatistics, id: String): Boolean =
        BadgeManager.calculateBadges(stats).first { it.id == id }.isEarned

    @Test
    fun `첫 걸음 뱃지는 단어 0개면 미획득`() {
        assertFalse(earned(stats(learnedWords = 0), "first_step"))
    }

    @Test
    fun `첫 걸음 뱃지는 단어 1개부터 획득`() {
        assertTrue(earned(stats(learnedWords = 1), "first_step"))
    }

    @Test
    fun `10일 연속 뱃지는 9일이면 미획득 10일이면 획득`() {
        assertFalse(earned(stats(streakDays = 9), "streak_10"))
        assertTrue(earned(stats(streakDays = 10), "streak_10"))
    }

    @Test
    fun `100단어 마스터 뱃지 경계값 검증`() {
        assertFalse(earned(stats(learnedWords = 99), "master_100"))
        assertTrue(earned(stats(learnedWords = 100), "master_100"))
    }

    @Test
    fun `1000단어 마스터는 999면 미획득`() {
        assertFalse(earned(stats(learnedWords = 999), "master_1000"))
        assertTrue(earned(stats(learnedWords = 1000), "master_1000"))
    }

    @Test
    fun `스테이지 완료 뱃지는 해당 단계 전체 학습 시에만 획득`() {
        // FOUNDATION 기준 총 단어 수는 800 (BadgeManager.getTotalWordsForStage)
        assertFalse(
            earned(stats(learnedByStage = mapOf(Stage.FOUNDATION to 799)), "stage_1")
        )
        assertTrue(
            earned(stats(learnedByStage = mapOf(Stage.FOUNDATION to 800)), "stage_1")
        )
    }

    @Test
    fun `스테이지 학습 0개면 완료 뱃지 미획득 (0 이상 조건 방어)`() {
        // learnedInStage > 0 조건 때문에 0개는 항상 미획득이어야 한다
        assertFalse(
            earned(stats(learnedByStage = mapOf(Stage.FOUNDATION to 0)), "stage_1")
        )
    }

    @Test
    fun `아무것도 학습하지 않으면 모든 뱃지가 미획득`() {
        val badges = BadgeManager.calculateBadges(stats())
        assertTrue(badges.isNotEmpty())
        assertTrue(badges.none { it.isEarned })
    }
}
