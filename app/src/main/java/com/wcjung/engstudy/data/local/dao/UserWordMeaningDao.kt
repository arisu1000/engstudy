package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.wcjung.engstudy.data.local.entity.UserWordMeaningEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWordMeaningDao {

    @Query("SELECT * FROM user_word_meanings WHERE word_id = :wordId ORDER BY is_primary DESC, created_at ASC")
    fun getForWord(wordId: Int): Flow<List<UserWordMeaningEntity>>

    /** 대표 뜻 override 전체 (words 조회 결과에 합성용). */
    @Query("SELECT * FROM user_word_meanings WHERE is_primary = 1")
    fun observePrimaryOverrides(): Flow<List<UserWordMeaningEntity>>

    @Query("SELECT * FROM user_word_meanings WHERE is_primary = 1")
    suspend fun getPrimaryOverrides(): List<UserWordMeaningEntity>

    @Insert
    suspend fun insert(entity: UserWordMeaningEntity): Long

    @Query("DELETE FROM user_word_meanings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM user_word_meanings WHERE word_id = :wordId AND is_primary = 1")
    suspend fun clearPrimary(wordId: Int)

    /** 대표 뜻 교체: 단어당 primary가 최대 1개가 되도록 기존 것을 지우고 삽입. */
    @Transaction
    suspend fun setPrimary(wordId: Int, meaning: String) {
        clearPrimary(wordId)
        insert(UserWordMeaningEntity(wordId = wordId, meaning = meaning, isPrimary = true))
    }
}
