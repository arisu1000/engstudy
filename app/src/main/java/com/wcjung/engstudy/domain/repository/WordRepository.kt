package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.WordMeaning
import com.wcjung.engstudy.domain.model.WordExample
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getWordsByFilter(
        stage: Int? = null,
        domain: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Word>>

    suspend fun getWordsPage(
        stage: Int?,
        domain: String?,
        showExcluded: Boolean,
        limit: Int,
        offset: Int
    ): List<Word>

    fun searchWords(query: String): Flow<List<Word>>

    fun getNewWordsForStudy(
        count: Int = 20,
        stage: Int? = null,
        domain: String? = null
    ): Flow<List<Word>>

    suspend fun getWordById(id: Int): Word?

    suspend fun getWordOfTheDay(): Word?

    suspend fun getRandomWordsInStage(stage: Int, excludeId: Int, count: Int = 3): List<Word>

    suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int = 3): List<Word>

    fun getTotalWordCount(): Flow<Int>

    fun getWordCountByStage(stage: Int): Flow<Int>

    fun getWordCountByDomain(domain: String): Flow<Int>

    fun getAllDomains(): Flow<List<String>>

    fun getAllStages(): Flow<List<Int>>

    suspend fun getRandomWordsByStage(stage: Int, count: Int = 10): List<Word>

    suspend fun getDailyChallengeWords(seed: Long, count: Int = 10): List<Word>

    suspend fun getRandomWordsExcluding(excludeIds: List<Int>, count: Int): List<Word>

    fun getMeaningsForWord(wordId: Int): Flow<List<WordMeaning>>

    fun getExamplesForWord(wordId: Int): Flow<List<WordExample>>
}
