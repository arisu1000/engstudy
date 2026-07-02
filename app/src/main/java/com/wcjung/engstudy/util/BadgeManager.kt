package com.wcjung.engstudy.util

import com.wcjung.engstudy.domain.model.Badge
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.domain.model.StudyStatistics

/**
 * 학습 통계를 기반으로 달성한 뱃지 목록을 계산한다.
 * 뱃지 종류: 학습 시작, 연속 학습, 단어 마스터, 스테이지 완료
 */
object BadgeManager {

    /**
     * @param stageTotals 스테이지별 실제 총 단어 수(DB 기준). 제공되면 스테이지 완료
     *   판정에 사용하고, 값이 없거나 0인 스테이지는 [getTotalWordsForStage] 근사치로 대체한다.
     */
    fun calculateBadges(
        stats: StudyStatistics,
        stageTotals: Map<Stage, Int> = emptyMap()
    ): List<Badge> {
        val badges = mutableListOf<Badge>()

        // 학습 시작 뱃지
        badges += Badge(
            id = "first_step",
            name = "첫 걸음",
            description = "첫 단어 학습 완료",
            icon = "first_step",
            isEarned = stats.learnedWords >= 1
        )

        // 연속 학습 뱃지
        badges += Badge(
            id = "streak_10",
            name = "10일 연속",
            description = "10일 연속 학습 달성",
            icon = "streak",
            isEarned = stats.streakDays >= 10
        )
        badges += Badge(
            id = "streak_30",
            name = "30일 연속",
            description = "30일 연속 학습 달성",
            icon = "streak",
            isEarned = stats.streakDays >= 30
        )

        // 단어 마스터 뱃지
        badges += Badge(
            id = "master_100",
            name = "100단어 마스터",
            description = "100단어 학습 완료",
            icon = "master",
            isEarned = stats.learnedWords >= 100
        )
        badges += Badge(
            id = "master_500",
            name = "500단어 마스터",
            description = "500단어 학습 완료",
            icon = "master",
            isEarned = stats.learnedWords >= 500
        )
        badges += Badge(
            id = "master_1000",
            name = "1000단어 마스터",
            description = "1000단어 학습 완료",
            icon = "master",
            isEarned = stats.learnedWords >= 1000
        )

        // 스테이지 완료 뱃지
        Stage.entries.forEach { stage ->
            val learnedInStage = stats.learnedByStage[stage] ?: 0
            val totalInStage = stageTotals[stage]?.takeIf { it > 0 } ?: getTotalWordsForStage(stage)
            badges += Badge(
                id = "stage_${stage.level}",
                name = "Stage ${stage.level} 완료",
                description = "${stage.displayNameKo} 단계 전체 학습",
                icon = "stage",
                isEarned = learnedInStage > 0 && learnedInStage >= totalInStage
            )
        }

        return badges
    }

    /**
     * 스테이지별 총 단어 수 근사치(폴백).
     * 정확한 판정을 위해서는 [calculateBadges]에 실제 DB 기준 stageTotals를 전달한다.
     * 이 값은 stageTotals가 비어 있거나 해당 스테이지 값이 없을 때만 사용된다.
     */
    private fun getTotalWordsForStage(stage: Stage): Int {
        return when (stage) {
            Stage.FOUNDATION -> 800
            Stage.INTERMEDIATE -> 1000
            Stage.UPPER_INTERMEDIATE -> 1000
            Stage.ADVANCED -> 1000
            Stage.PROFICIENT -> 700
            Stage.NEAR_NATIVE -> 500
        }
    }
}
