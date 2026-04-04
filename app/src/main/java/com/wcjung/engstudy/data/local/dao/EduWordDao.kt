package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.EduWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EduWordDao {

    @Query("SELECT * FROM edu_words ORDER BY id ASC")
    fun getAllWords(): Flow<List<EduWordEntity>>

    @Query("SELECT * FROM edu_words WHERE id = :id")
    suspend fun getWordById(id: Int): EduWordEntity?

    @Query("SELECT * FROM edu_words WHERE level = :level ORDER BY id ASC")
    fun getWordsByLevel(level: String): Flow<List<EduWordEntity>>

    @Query(
        """
        SELECT * FROM edu_words
        WHERE word LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT 50
        """
    )
    fun searchWords(query: String): Flow<List<EduWordEntity>>

    @Query("SELECT COUNT(*) FROM edu_words")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM edu_words WHERE level = :level")
    fun getCountByLevel(level: String): Flow<Int>

    @Query("SELECT DISTINCT level FROM edu_words ORDER BY level")
    fun getAllLevels(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM edu_words
        WHERE level = :level AND id != :excludeId
        ORDER BY RANDOM() LIMIT :count
        """
    )
    suspend fun getRandomWordsInLevel(level: String, excludeId: Int, count: Int = 3): List<EduWordEntity>

    @Query(
        """
        SELECT * FROM edu_words
        WHERE level = :level
        ORDER BY id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getWordsByLevelPaged(level: String, limit: Int = 50, offset: Int = 0): Flow<List<EduWordEntity>>
}
