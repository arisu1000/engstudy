package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongAnswerDao {

    @Query("SELECT * FROM wrong_answers ORDER BY created_at DESC LIMIT :limit")
    fun getRecentWrongAnswers(limit: Int = 100): Flow<List<WrongAnswerEntity>>

    @Query(
        """
        SELECT DISTINCT w.* FROM words w
        INNER JOIN wrong_answers wa ON w.id = wa.word_id
        ORDER BY wa.created_at DESC
        """
    )
    fun getWrongAnswerWords(): Flow<List<WordEntity>>

    @Insert
    suspend fun insertWrongAnswer(entity: WrongAnswerEntity)

    @Query("DELETE FROM wrong_answers WHERE created_at < :olderThan")
    suspend fun deleteOldEntries(olderThan: Long)

    @Query("SELECT COUNT(*) FROM wrong_answers")
    fun getWrongAnswerCount(): Flow<Int>
}
