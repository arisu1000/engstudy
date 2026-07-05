package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.EduExcludedWordEntity
import com.wcjung.engstudy.data.local.entity.EduWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EduExcludedWordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun excludeWord(entity: EduExcludedWordEntity)

    suspend fun excludeWord(eduWordId: Int) = excludeWord(EduExcludedWordEntity(eduWordId = eduWordId))

    @Query("DELETE FROM edu_excluded_words WHERE edu_word_id = :eduWordId")
    suspend fun restoreWord(eduWordId: Int)

    @Query("SELECT edu_word_id FROM edu_excluded_words")
    fun getExcludedIds(): Flow<List<Int>>

    @Query(
        """
        SELECT w.* FROM edu_words w
        INNER JOIN edu_excluded_words e ON w.id = e.edu_word_id
        ORDER BY e.excluded_at DESC
        """
    )
    fun getExcludedWords(): Flow<List<EduWordEntity>>

    @Query("SELECT COUNT(*) FROM edu_excluded_words")
    fun getExcludedCount(): Flow<Int>
}
