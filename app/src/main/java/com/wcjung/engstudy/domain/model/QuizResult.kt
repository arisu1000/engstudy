package com.wcjung.engstudy.domain.model

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectWords: List<Word>
) {
    val score: Float
        get() = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
}
