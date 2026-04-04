package com.wcjung.engstudy.domain.model

data class StudyStatistics(
    val totalWords: Int,
    val learnedWords: Int,
    val inProgressWords: Int,
    val dueReviews: Int,
    val totalStudyDays: Int,
    val todayReviewedCount: Int,
    val streakDays: Int,
    val learnedByStage: Map<Stage, Int>,
    val learnedByDomain: Map<Domain, Int>
)
