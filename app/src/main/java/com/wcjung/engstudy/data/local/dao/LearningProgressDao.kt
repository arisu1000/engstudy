package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningProgressDao {

    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.next_review_date <= :now
        ORDER BY lp.next_review_date ASC
        LIMIT :count
        """
    )
    fun getWordsForReview(now: Long = System.currentTimeMillis(), count: Int = 50): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE next_review_date <= :now")
    fun getDueReviewCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE is_learned = 1")
    fun getLearnedWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE repetitions > 0")
    fun getInProgressWordCount(): Flow<Int>

    @Query("SELECT * FROM learning_progress WHERE word_id = :wordId")
    suspend fun getProgressForWord(wordId: Int): LearningProgressEntity?

    @Upsert
    suspend fun upsertProgress(progress: LearningProgressEntity)

    @Query(
        """
        SELECT COUNT(*) FROM learning_progress
        WHERE last_reviewed_date >= :dayStart AND last_reviewed_date < :dayEnd
        """
    )
    fun getReviewedCountForDay(dayStart: Long, dayEnd: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(DISTINCT date(last_reviewed_date / 1000, 'unixepoch', 'localtime'))
        FROM learning_progress
        WHERE last_reviewed_date IS NOT NULL
        """
    )
    fun getTotalStudyDays(): Flow<Int>

    @Query(
        """
        SELECT w.domain, COUNT(*) as count FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.is_learned = 1
        GROUP BY w.domain
        """
    )
    fun getLearnedCountByDomain(): Flow<List<DomainCount>>

    @Query(
        """
        SELECT w.age_group, COUNT(*) as count FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.is_learned = 1
        GROUP BY w.age_group
        """
    )
    fun getLearnedCountByAgeGroup(): Flow<List<AgeGroupCount>>
}

data class DomainCount(val domain: String, val count: Int)
data class AgeGroupCount(val age_group: String, val count: Int)
