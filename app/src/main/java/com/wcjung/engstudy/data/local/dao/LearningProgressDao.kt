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

    /**
     * 복습할 단어 - 완전 제외(is_excluded)와 아직 학습 안 된 단어 제외
     */
    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.next_review_date <= :now
        AND lp.is_learned = 0
        AND lp.is_excluded = 0
        ORDER BY lp.next_review_date ASC
        LIMIT :count
        """
    )
    fun getWordsForReview(now: Long = System.currentTimeMillis(), count: Int = 50): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE next_review_date <= :now AND is_learned = 0 AND is_excluded = 0")
    fun getDueReviewCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE is_learned = 1")
    fun getLearnedWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE repetitions > 0 AND is_excluded = 0")
    fun getInProgressWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE is_excluded = 1")
    fun getExcludedWordCount(): Flow<Int>

    @Query("SELECT * FROM learning_progress WHERE word_id = :wordId")
    suspend fun getProgressForWord(wordId: Int): LearningProgressEntity?

    @Upsert
    suspend fun upsertProgress(progress: LearningProgressEntity)

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

    /**
     * "이미 알아요" - 학습 완료 표시 (복습 목록에는 여전히 나올 수 있음)
     */
    @Query(
        """
        INSERT INTO learning_progress
        (word_id, ease_factor, interval_days, repetitions, next_review_date, last_reviewed_date, times_correct, times_incorrect, is_learned, is_excluded)
        VALUES (:wordId, 2.5, 21, 5, :now, :now, 1, 0, 1, 0)
        ON CONFLICT(word_id) DO UPDATE SET
            is_learned = 1,
            interval_days = 21,
            repetitions = MAX(repetitions, 5),
            next_review_date = :now,
            last_reviewed_date = :now
        """
    )
    suspend fun markAsKnown(wordId: Int, now: Long = System.currentTimeMillis())

    /**
     * 완전 제외 - 모든 학습/복습에서 영구적으로 제외
     */
    @Query(
        """
        INSERT INTO learning_progress
        (word_id, ease_factor, interval_days, repetitions, next_review_date, last_reviewed_date, times_correct, times_incorrect, is_learned, is_excluded)
        VALUES (:wordId, 2.5, 21, 5, :now, :now, 1, 0, 1, 1)
        ON CONFLICT(word_id) DO UPDATE SET
            is_excluded = 1,
            is_learned = 1
        """
    )
    suspend fun excludeWord(wordId: Int, now: Long = System.currentTimeMillis())

    /**
     * 완전 제외 해제 - 다시 학습 대상으로 복원
     */
    @Query("UPDATE learning_progress SET is_excluded = 0, is_learned = 0, interval_days = 0, repetitions = 0 WHERE word_id = :wordId")
    suspend fun restoreWord(wordId: Int)

    /**
     * 제외된 단어 목록 조회
     */
    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN learning_progress lp ON w.id = lp.word_id
        WHERE lp.is_excluded = 1
        ORDER BY lp.last_reviewed_date DESC
        """
    )
    fun getExcludedWords(): Flow<List<WordEntity>>

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
