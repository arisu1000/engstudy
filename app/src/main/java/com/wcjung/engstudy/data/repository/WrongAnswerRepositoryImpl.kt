package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.WrongAnswerDao
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WrongAnswerRepositoryImpl @Inject constructor(
    private val wrongAnswerDao: WrongAnswerDao
) : WrongAnswerRepository {

    override fun getRecentWrongAnswers(limit: Int): Flow<List<WrongAnswerEntity>> =
        wrongAnswerDao.getRecentWrongAnswers(limit)

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
