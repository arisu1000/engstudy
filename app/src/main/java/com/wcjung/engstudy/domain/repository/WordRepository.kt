package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getWordsByFilter(
        domain: String? = null,
        ageGroup: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Word>>

    fun searchWords(query: String): Flow<List<Word>>

    fun getNewWordsForStudy(
        count: Int = 20,
        ageGroup: String? = null,
        domain: String? = null
    ): Flow<List<Word>>

    suspend fun getWordById(id: Int): Word?

    suspend fun getWordOfTheDay(): Word?

    suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int = 3): List<Word>

    suspend fun getRandomWordsInAgeGroup(ageGroup: String, excludeId: Int, count: Int = 3): List<Word>

    fun getTotalWordCount(): Flow<Int>

    fun getWordCountByDomain(domain: String): Flow<Int>

    fun getAllDomains(): Flow<List<String>>

    fun getAllAgeGroups(): Flow<List<String>>
}
