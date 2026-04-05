package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.DailyStudyRecord
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    fun getWordsForReview(count: Int = 50): Flow<List<Word>>
    fun getDueReviewCount(): Flow<Int>
    fun getLearnedWordCount(): Flow<Int>
    fun getInProgressWordCount(): Flow<Int>
    suspend fun getProgressForWord(wordId: Int): LearningProgress?
    suspend fun updateProgress(progress: LearningProgress)
    suspend fun markAsKnown(wordId: Int)
    fun getReviewedCountForDay(dayStart: Long, dayEnd: Long): Flow<Int>
    fun getTotalStudyDays(): Flow<Int>
    fun getLearnedCountByStage(): Flow<Map<Int, Int>>
    fun getLearnedCountByDomain(): Flow<Map<String, Int>>
    fun getLearnedWordCountByStage(stage: Int): Flow<Int>
    fun getDailyStudyCounts(sinceTimestamp: Long): Flow<List<DailyStudyRecord>>
    suspend fun excludeWord(wordId: Int)
    suspend fun restoreWord(wordId: Int)
    fun getExcludedWords(): Flow<List<Word>>
    fun getExcludedWordCount(): Flow<Int>
}
