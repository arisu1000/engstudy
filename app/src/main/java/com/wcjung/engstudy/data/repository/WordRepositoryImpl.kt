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
        stage: Int?,
        domain: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Word>> =
        wordDao.getWordsByFilter(stage, domain, limit, offset)
            .map { entities -> entities.map { it.toDomain() } }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { entities -> entities.map { it.toDomain() } }

    override fun getNewWordsForStudy(
        count: Int,
        stage: Int?,
        domain: String?
    ): Flow<List<Word>> =
        wordDao.getNewWordsForStudy(count, stage, domain)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getWordById(id: Int): Word? =
        wordDao.getWordById(id)?.toDomain()

    override suspend fun getWordOfTheDay(): Word? {
        val seed = LocalDate.now().toEpochDay()
        return wordDao.getWordOfTheDay(seed)?.toDomain()
    }

    override suspend fun getRandomWordsInStage(stage: Int, excludeId: Int, count: Int): List<Word> =
        wordDao.getRandomWordsInStage(stage, excludeId, count).map { it.toDomain() }

    override suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int): List<Word> =
        wordDao.getRandomWordsInDomain(domain, excludeId, count).map { it.toDomain() }

    override fun getTotalWordCount(): Flow<Int> = wordDao.getTotalWordCount()

    override fun getWordCountByStage(stage: Int): Flow<Int> = wordDao.getWordCountByStage(stage)

    override fun getWordCountByDomain(domain: String): Flow<Int> = wordDao.getWordCountByDomain(domain)

    override fun getAllDomains(): Flow<List<String>> = wordDao.getAllDomains()

    override fun getAllStages(): Flow<List<Int>> = wordDao.getAllStages()

    override suspend fun getRandomWordsByStage(stage: Int, count: Int): List<Word> =
        wordDao.getRandomWordsByStage(stage, count).map { it.toDomain() }
}
