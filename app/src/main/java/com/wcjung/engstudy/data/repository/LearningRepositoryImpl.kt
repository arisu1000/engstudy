package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.LearningProgressDao
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.toDomain
import com.wcjung.engstudy.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LearningRepositoryImpl @Inject constructor(
    private val learningProgressDao: LearningProgressDao
) : LearningRepository {

    override fun getWordsForReview(count: Int): Flow<List<Word>> =
        learningProgressDao.getWordsForReview(
            now = System.currentTimeMillis(),
            count = count
        ).map { entities -> entities.map { it.toDomain() } }

    override fun getDueReviewCount(): Flow<Int> =
        learningProgressDao.getDueReviewCount(System.currentTimeMillis())

    override fun getLearnedWordCount(): Flow<Int> =
        learningProgressDao.getLearnedWordCount()

    override fun getInProgressWordCount(): Flow<Int> =
        learningProgressDao.getInProgressWordCount()

    override suspend fun getProgressForWord(wordId: Int): LearningProgress? =
        learningProgressDao.getProgressForWord(wordId)?.toDomain()

    override suspend fun updateProgress(progress: LearningProgress) {
        val existing = learningProgressDao.getProgressForWord(progress.wordId)
        val entity = LearningProgressEntity(
            id = existing?.id ?: 0,
            wordId = progress.wordId,
            easeFactor = progress.easeFactor,
            intervalDays = progress.intervalDays,
            repetitions = progress.repetitions,
            nextReviewDate = progress.nextReviewDate,
            lastReviewedDate = progress.lastReviewedDate,
            timesCorrect = progress.timesCorrect,
            timesIncorrect = progress.timesIncorrect,
            isLearned = progress.isLearned
        )
        learningProgressDao.upsertProgress(entity)
    }

    override fun getReviewedCountForDay(dayStart: Long, dayEnd: Long): Flow<Int> =
        learningProgressDao.getReviewedCountForDay(dayStart, dayEnd)

    override fun getTotalStudyDays(): Flow<Int> =
        learningProgressDao.getTotalStudyDays()

    override fun getLearnedCountByDomain(): Flow<Map<String, Int>> =
        learningProgressDao.getLearnedCountByDomain().map { list ->
            list.associate { it.domain to it.count }
        }

    override fun getLearnedCountByAgeGroup(): Flow<Map<String, Int>> =
        learningProgressDao.getLearnedCountByAgeGroup().map { list ->
            list.associate { it.age_group to it.count }
        }
}
