package com.wcjung.engstudy.domain.model

data class LearningProgress(
    val wordId: Int,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedDate: Long? = null,
    val timesCorrect: Int = 0,
    val timesIncorrect: Int = 0,
    val isLearned: Boolean = false
)

data class SpacedRepetitionResult(
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewDate: Long
)
