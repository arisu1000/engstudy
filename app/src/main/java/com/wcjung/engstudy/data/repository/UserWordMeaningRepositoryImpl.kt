package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.UserWordMeaningDao
import com.wcjung.engstudy.data.local.entity.UserWordMeaningEntity
import com.wcjung.engstudy.domain.model.UserWordMeaning
import com.wcjung.engstudy.domain.repository.UserWordMeaningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserWordMeaningRepositoryImpl @Inject constructor(
    private val dao: UserWordMeaningDao
) : UserWordMeaningRepository {

    override fun getForWord(wordId: Int): Flow<List<UserWordMeaning>> =
        dao.getForWord(wordId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addMeaning(wordId: Int, meaning: String) {
        val trimmed = meaning.trim()
        if (trimmed.isEmpty()) return
        dao.insert(UserWordMeaningEntity(wordId = wordId, meaning = trimmed))
    }

    override suspend fun deleteMeaning(id: Long) = dao.deleteById(id)

    override suspend fun setPrimaryMeaning(wordId: Int, meaning: String) {
        val trimmed = meaning.trim()
        if (trimmed.isEmpty()) return
        dao.setPrimary(wordId, trimmed)
    }

    override suspend fun clearPrimaryMeaning(wordId: Int) = dao.clearPrimary(wordId)

    private fun UserWordMeaningEntity.toDomain() = UserWordMeaning(
        id = id,
        wordId = wordId,
        meaning = meaning,
        isPrimary = isPrimary,
        createdAt = createdAt
    )
}
