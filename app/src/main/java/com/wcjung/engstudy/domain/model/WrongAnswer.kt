package com.wcjung.engstudy.domain.model

/**
 * 오답 기록의 도메인 모델.
 * data 레이어의 WrongAnswerEntity가 domain 레이어에 노출되지 않도록 분리한다.
 */
data class WrongAnswer(
    val id: Long,
    val wordId: Int,
    val wrongAnswer: String,
    val correctAnswer: String,
    val quizType: String,
    val createdAt: Long
)
