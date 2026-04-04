package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.WordDao
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.toDomain
import com.wcjung.engstudy.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    override fun getWordsByFilter(
        domain: String?,
        ageGroup: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Word>> =
        wordDao.getWordsByFilter(domain, ageGroup, limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNewWordsForStudy(
        count: Int,
        ageGroup: String?,
        domain: String?
    ): Flow<List<Word>> =
        wordDao.getNewWordsForStudy(count, ageGroup, domain).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getWordById(id: Int): Word? =
        wordDao.getWordById(id)?.toDomain()

    override suspend fun getWordOfTheDay(): Word? {
        val today = LocalDate.now()
        val seed = today.toEpochDay()
        return wordDao.getWordOfTheDay(seed)?.toDomain()
    }

    override suspend fun getRandomWordsInDomain(
        domain: String,
        excludeId: Int,
        count: Int
    ): List<Word> =
        wordDao.getRandomWordsInDomain(domain, excludeId, count).map { it.toDomain() }

    override suspend fun getRandomWordsInAgeGroup(
        ageGroup: String,
        excludeId: Int,
        count: Int
    ): List<Word> =
        wordDao.getRandomWordsInAgeGroup(ageGroup, excludeId, count).map { it.toDomain() }

    override fun getTotalWordCount(): Flow<Int> = wordDao.getTotalWordCount()

    override fun getWordCountByDomain(domain: String): Flow<Int> =
        wordDao.getWordCountByDomain(domain)

    override fun getAllDomains(): Flow<List<String>> = wordDao.getAllDomains()

    override fun getAllAgeGroups(): Flow<List<String>> = wordDao.getAllAgeGroups()
}
