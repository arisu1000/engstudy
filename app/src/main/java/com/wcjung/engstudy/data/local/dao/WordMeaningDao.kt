package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.WordMeaningEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordMeaningDao {
    @Query("SELECT * FROM word_meanings WHERE word_id = :wordId ORDER BY sense_order ASC")
    fun getMeaningsForWord(wordId: Int): Flow<List<WordMeaningEntity>>
}
