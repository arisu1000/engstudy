package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.EduWord
import kotlinx.coroutines.flow.Flow

interface EduWordRepository {
    fun getAllWords(): Flow<List<EduWord>>
    fun getWordsByLevel(level: String): Flow<List<EduWord>>
    fun getWordsByLevelPaged(level: String, limit: Int = 50, offset: Int = 0): Flow<List<EduWord>>
    fun searchWords(query: String): Flow<List<EduWord>>
    fun getTotalCount(): Flow<Int>
    fun getCountByLevel(level: String): Flow<Int>
    suspend fun getWordById(id: Int): EduWord?
    suspend fun getRandomWordsInLevel(level: String, excludeId: Int, count: Int = 3): List<EduWord>
}
