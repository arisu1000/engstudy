package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.Idiom
import kotlinx.coroutines.flow.Flow

interface IdiomRepository {
    fun getAllIdioms(): Flow<List<Idiom>>
    fun getByType(type: String): Flow<List<Idiom>>
    fun searchIdioms(query: String): Flow<List<Idiom>>
    fun getTotalCount(): Flow<Int>
    fun getCountByType(type: String): Flow<Int>
    suspend fun getIdiomById(id: Int): Idiom?
    suspend fun getRandomIdioms(excludeId: Int, count: Int = 3): List<Idiom>
}
