package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.ExampleSentenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExampleSentenceDao {

    @Query("SELECT * FROM example_sentences ORDER BY id")
    fun getAllSentences(): Flow<List<ExampleSentenceEntity>>

    @Query("SELECT * FROM example_sentences WHERE grammar_topic_ko = :topic ORDER BY id")
    fun getByGrammarTopic(topic: String): Flow<List<ExampleSentenceEntity>>

    @Query("SELECT * FROM example_sentences WHERE level = :level ORDER BY id")
    fun getByLevel(level: String): Flow<List<ExampleSentenceEntity>>

    @Query("SELECT * FROM example_sentences WHERE sentence_en LIKE '%' || :query || '%' OR sentence_ko LIKE '%' || :query || '%' ORDER BY id")
    fun searchSentences(query: String): Flow<List<ExampleSentenceEntity>>

    @Query("SELECT COUNT(*) FROM example_sentences")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM example_sentences WHERE grammar_topic_ko = :topic")
    fun getCountByTopic(topic: String): Flow<Int>

    @Query("SELECT DISTINCT grammar_topic_ko FROM example_sentences ORDER BY grammar_topic_ko")
    fun getAllTopics(): Flow<List<String>>

    @Query("SELECT * FROM example_sentences ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSentence(): ExampleSentenceEntity?
}
