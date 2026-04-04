package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.WrongAnswer
import kotlinx.coroutines.flow.Flow

interface WrongAnswerRepository {
    fun getRecentWrongAnswers(limit: Int = 100): Flow<List<WrongAnswer>>
    suspend fun insertWrongAnswer(wordId: Int, wrongAnswer: String, correctAnswer: String, quizType: String)
    fun getWrongAnswerCount(): Flow<Int>
}
