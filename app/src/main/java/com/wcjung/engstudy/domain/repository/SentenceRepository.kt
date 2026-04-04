package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.ExampleSentence
import kotlinx.coroutines.flow.Flow

interface SentenceRepository {
    fun getAllSentences(): Flow<List<ExampleSentence>>
    fun getByGrammarTopic(topic: String): Flow<List<ExampleSentence>>
    fun getByLevel(level: String): Flow<List<ExampleSentence>>
    fun searchSentences(query: String): Flow<List<ExampleSentence>>
    fun getTotalCount(): Flow<Int>
    fun getCountByTopic(topic: String): Flow<Int>
    fun getAllTopics(): Flow<List<String>>
    suspend fun getRandomSentence(): ExampleSentence?
}
