package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * 기존 progress를 조회한 뒤 upsert하는 원자적 트랜잭션.
     * read-then-write 경쟁 조건을 방지한다.
     */
    @Transaction
    suspend fun upsertProgressForWord(progress: LearningProgressEntity) {
        val existing = getProgressForWord(progress.wordId)
        upsertProgress(progress.copy(id = existing?.id ?: 0))
    }

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
        SELECT w.stage, COUNT(*) as count FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.is_learned = 1
        GROUP BY w.stage
        """
    )
    fun getLearnedCountByStage(): Flow<List<StageCount>>

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
        SELECT COUNT(*) FROM learning_progress lp
        INNER JOIN words w ON lp.word_id = w.id
        WHERE lp.is_learned = 1 AND w.stage = :stage
        """
    )
    fun getLearnedWordCountByStage(stage: Int): Flow<Int>

    @Query(
        """
        INSERT OR REPLACE INTO learning_progress
        (word_id, ease_factor, interval_days, repetitions, next_review_date, last_reviewed_date, times_correct, times_incorrect, is_learned)
        VALUES (:wordId, 2.5, 21, 5, :now, :now, 1, 0, 1)
        """
    )
    suspend fun markAsKnown(wordId: Int, now: Long = System.currentTimeMillis())

    @Query(
        """
        SELECT date(last_reviewed_date / 1000, 'unixepoch', 'localtime') as study_date,
               COUNT(*) as count
        FROM learning_progress
        WHERE last_reviewed_date IS NOT NULL
        AND last_reviewed_date >= :sinceTimestamp
        GROUP BY study_date
        """
    )
    fun getDailyStudyCounts(sinceTimestamp: Long): Flow<List<DailyStudyCount>>
}

data class StageCount(val stage: Int, val count: Int)
data class DomainCount(val domain: String, val count: Int)
data class DailyStudyCount(val study_date: String, val count: Int)
