package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY frequency_rank ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Int): WordEntity?

    @Query(
        """
        SELECT * FROM words
        WHERE (:stage IS NULL OR stage = :stage)
        AND (:domain IS NULL OR domain = :domain)
        ORDER BY frequency_rank ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getWordsByFilter(
        stage: Int? = null,
        domain: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<WordEntity>>

    @Query(
        """
        SELECT * FROM words
        WHERE word LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        ORDER BY frequency_rank ASC
        LIMIT 50
        """
    )
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query(
        """
        SELECT * FROM words
        WHERE id NOT IN (SELECT word_id FROM learning_progress WHERE is_learned = 1)
        AND (:stage IS NULL OR stage = :stage)
        AND (:domain IS NULL OR domain = :domain)
        ORDER BY frequency_rank ASC
        LIMIT :count
        """
    )
    fun getNewWordsForStudy(
        count: Int = 20,
        stage: Int? = null,
        domain: String? = null
    ): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE stage = :stage")
    fun getWordCountByStage(stage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE domain = :domain")
    fun getWordCountByDomain(domain: String): Flow<Int>

    @Query("SELECT * FROM words ORDER BY id ASC LIMIT 1 OFFSET (ABS(:dateSeed) % (SELECT COUNT(*) FROM words))")
    suspend fun getWordOfTheDay(dateSeed: Long): WordEntity?

    @Query(
        """
        SELECT * FROM words
        WHERE stage = :stage AND id != :excludeId
        ORDER BY RANDOM() LIMIT :count
        """
    )
    suspend fun getRandomWordsInStage(stage: Int, excludeId: Int, count: Int = 3): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE domain = :domain AND id != :excludeId
        ORDER BY RANDOM() LIMIT :count
        """
    )
    suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int = 3): List<WordEntity>

    @Query("SELECT DISTINCT domain FROM words ORDER BY domain")
    fun getAllDomains(): Flow<List<String>>

    @Query("SELECT DISTINCT stage FROM words ORDER BY stage")
    fun getAllStages(): Flow<List<Int>>

    @Query(
        """
        SELECT * FROM words
        WHERE stage = :stage
        ORDER BY RANDOM()
        LIMIT :count
        """
    )
    suspend fun getRandomWordsByStage(stage: Int, count: Int = 10): List<WordEntity>
}
