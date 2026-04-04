package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.WrongAnswerDao
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity
import com.wcjung.engstudy.domain.model.WrongAnswer
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WrongAnswerRepositoryImpl @Inject constructor(
    private val wrongAnswerDao: WrongAnswerDao
) : WrongAnswerRepository {

    override fun getRecentWrongAnswers(limit: Int): Flow<List<WrongAnswer>> =
        wrongAnswerDao.getRecentWrongAnswers(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    private fun WrongAnswerEntity.toDomain() = WrongAnswer(
        id = id,
        wordId = wordId,
        wrongAnswer = wrongAnswer,
        correctAnswer = correctAnswer,
        quizType = quizType,
        createdAt = createdAt
    )

    override suspend fun insertWrongAnswer(
        wordId: Int,
        wrongAnswer: String,
        correctAnswer: String,
        quizType: String
    ) {
        wrongAnswerDao.insertWrongAnswer(
            WrongAnswerEntity(
                wordId = wordId,
                wrongAnswer = wrongAnswer,
                correctAnswer = correctAnswer,
                quizType = quizType
            )
        )
    }

    override fun getWrongAnswerCount(): Flow<Int> =
        wrongAnswerDao.getWrongAnswerCount()
}
