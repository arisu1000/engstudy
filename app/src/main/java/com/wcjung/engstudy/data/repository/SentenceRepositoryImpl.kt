package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.ExampleSentenceDao
import com.wcjung.engstudy.data.local.entity.ExampleSentenceEntity
import com.wcjung.engstudy.domain.model.ExampleSentence
import com.wcjung.engstudy.domain.model.GrammarLevel
import com.wcjung.engstudy.domain.repository.SentenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SentenceRepositoryImpl @Inject constructor(
    private val sentenceDao: ExampleSentenceDao
) : SentenceRepository {

    override fun getAllSentences(): Flow<List<ExampleSentence>> =
        sentenceDao.getAllSentences().map { list -> list.map { it.toDomain() } }

    override fun getByGrammarTopic(topic: String): Flow<List<ExampleSentence>> =
        sentenceDao.getByGrammarTopic(topic).map { list -> list.map { it.toDomain() } }

    override fun getByLevel(level: String): Flow<List<ExampleSentence>> =
        sentenceDao.getByLevel(level).map { list -> list.map { it.toDomain() } }

    override fun searchSentences(query: String): Flow<List<ExampleSentence>> =
        sentenceDao.searchSentences(query).map { list -> list.map { it.toDomain() } }

    override fun getTotalCount(): Flow<Int> = sentenceDao.getTotalCount()

    override fun getCountByTopic(topic: String): Flow<Int> = sentenceDao.getCountByTopic(topic)

    override fun getAllTopics(): Flow<List<String>> = sentenceDao.getAllTopics()

    override suspend fun getRandomSentence(): ExampleSentence? =
        sentenceDao.getRandomSentence()?.toDomain()

    private fun ExampleSentenceEntity.toDomain(): ExampleSentence = ExampleSentence(
        id = id,
        sentenceEn = sentenceEn,
        sentenceKo = sentenceKo,
        grammarTopic = grammarTopic,
        grammarTopicKo = grammarTopicKo,
        level = GrammarLevel.fromKey(level),
        wordCount = wordCount
    )
}
