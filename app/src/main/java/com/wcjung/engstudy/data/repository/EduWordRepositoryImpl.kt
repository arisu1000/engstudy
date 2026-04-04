package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.EduWordDao
import com.wcjung.engstudy.domain.model.EduLevel
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.repository.EduWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EduWordRepositoryImpl @Inject constructor(
    private val eduWordDao: EduWordDao
) : EduWordRepository {

    override fun getAllWords(): Flow<List<EduWord>> =
        eduWordDao.getAllWords().map { list -> list.map { it.toDomain() } }

    override fun getWordsByLevel(level: String): Flow<List<EduWord>> =
        eduWordDao.getWordsByLevel(level).map { list -> list.map { it.toDomain() } }

    override fun getWordsByLevelPaged(level: String, limit: Int, offset: Int): Flow<List<EduWord>> =
        eduWordDao.getWordsByLevelPaged(level, limit, offset).map { list -> list.map { it.toDomain() } }

    override fun searchWords(query: String): Flow<List<EduWord>> =
        eduWordDao.searchWords(query).map { list -> list.map { it.toDomain() } }

    override fun getTotalCount(): Flow<Int> = eduWordDao.getTotalCount()

    override fun getCountByLevel(level: String): Flow<Int> = eduWordDao.getCountByLevel(level)

    override suspend fun getWordById(id: Int): EduWord? =
        eduWordDao.getWordById(id)?.toDomain()

    override suspend fun getRandomWordsInLevel(level: String, excludeId: Int, count: Int): List<EduWord> =
        eduWordDao.getRandomWordsInLevel(level, excludeId, count).map { it.toDomain() }

    private fun com.wcjung.engstudy.data.local.entity.EduWordEntity.toDomain(): EduWord = EduWord(
        id = id,
        word = word,
        meaning = meaning,
        level = EduLevel.fromKey(level),
        partOfSpeech = partOfSpeech,
        variant1 = variant1,
        variant2 = variant2
    )
}
