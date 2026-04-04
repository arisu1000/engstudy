package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.IdiomDao
import com.wcjung.engstudy.data.local.entity.IdiomEntity
import com.wcjung.engstudy.domain.model.Idiom
import com.wcjung.engstudy.domain.model.IdiomType
import com.wcjung.engstudy.domain.model.MeaningType
import com.wcjung.engstudy.domain.repository.IdiomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IdiomRepositoryImpl @Inject constructor(
    private val idiomDao: IdiomDao
) : IdiomRepository {

    override fun getAllIdioms(): Flow<List<Idiom>> =
        idiomDao.getAllIdioms().map { list -> list.map { it.toDomain() } }

    override fun getByType(type: String): Flow<List<Idiom>> =
        idiomDao.getByType(type).map { list -> list.map { it.toDomain() } }

    override fun searchIdioms(query: String): Flow<List<Idiom>> =
        idiomDao.searchIdioms(query).map { list -> list.map { it.toDomain() } }

    override fun getTotalCount(): Flow<Int> = idiomDao.getTotalCount()

    override fun getCountByType(type: String): Flow<Int> = idiomDao.getCountByType(type)

    override suspend fun getIdiomById(id: Int): Idiom? =
        idiomDao.getIdiomById(id)?.toDomain()

    override suspend fun getRandomIdioms(excludeId: Int, count: Int): List<Idiom> =
        idiomDao.getRandomIdioms(excludeId, count).map { it.toDomain() }

    private fun IdiomEntity.toDomain(): Idiom = Idiom(
        id = id,
        phrase = phrase,
        meaning = meaning,
        meaningType = MeaningType.fromKey(meaningType),
        type = IdiomType.fromKey(type),
        exampleEn = exampleEn,
        exampleKo = exampleKo,
        difficulty = difficulty,
        category = category
    )
}
