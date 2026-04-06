package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.WordExampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordExampleDao {
    @Query("SELECT * FROM word_examples WHERE word_id = :wordId LIMIT 3")
    fun getExamplesForWord(wordId: Int): Flow<List<WordExampleEntity>>
}
