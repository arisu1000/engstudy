package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.UserWordMeaningDao
import com.wcjung.engstudy.data.local.dao.WordDao
import com.wcjung.engstudy.data.local.dao.WordMeaningDao
import com.wcjung.engstudy.data.local.dao.WordExampleDao
import com.wcjung.engstudy.data.local.entity.WordEntity
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.WordMeaning
import com.wcjung.engstudy.domain.model.WordExample
import com.wcjung.engstudy.domain.model.toDomain
import com.wcjung.engstudy.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val wordMeaningDao: WordMeaningDao,
    private val wordExampleDao: WordExampleDao,
    private val userWordMeaningDao: UserWordMeaningDao
) : WordRepository {

    // ------------------------------------------------------------------
    // 사용자 대표 뜻 override 합성
    //
    // 사용자가 단어 상세에서 대표 뜻을 변경하면(user_word_meanings.is_primary)
    // 목록/검색/퀴즈/복습 등 모든 조회 결과에 반영되어야 한다.
    // 콘텐츠 테이블을 직접 수정하면 콘텐츠 갱신 마이그레이션 때 유실되므로,
    // 조회 시점에 여기서 합성한다.
    // ------------------------------------------------------------------

    private val primaryOverridesFlow: Flow<Map<Int, String>> =
        userWordMeaningDao.observePrimaryOverrides()
            .map { rows -> rows.associate { it.wordId to it.meaning } }

    private suspend fun primaryOverrides(): Map<Int, String> =
        userWordMeaningDao.getPrimaryOverrides().associate { it.wordId to it.meaning }

    private fun Word.withOverride(overrides: Map<Int, String>): Word =
        overrides[id]?.let { copy(meaning = it) } ?: this

    private fun Flow<List<WordEntity>>.toDomainWithOverrides(): Flow<List<Word>> =
        combine(primaryOverridesFlow) { entities, overrides ->
            entities.map { it.toDomain().withOverride(overrides) }
        }

    private suspend fun List<WordEntity>.toDomainWithOverrides(): List<Word> {
        val overrides = primaryOverrides()
        return map { it.toDomain().withOverride(overrides) }
    }

    override fun getWordsByFilter(
        stage: Int?,
        domain: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Word>> =
        wordDao.getWordsByFilter(stage, domain, limit, offset).toDomainWithOverrides()

    override suspend fun getWordsPage(
        stage: Int?,
        domain: String?,
        showExcluded: Boolean,
        limit: Int,
        offset: Int
    ): List<Word> {
        val entities = if (showExcluded) {
            wordDao.getWordsPageShowAll(stage, domain, limit, offset)
        } else {
            wordDao.getWordsPageHideExcluded(stage, domain, limit, offset)
        }
        return entities.toDomainWithOverrides()
    }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).toDomainWithOverrides()

    override fun getNewWordsForStudy(
        count: Int,
        stage: Int?,
        domain: String?
    ): Flow<List<Word>> =
        wordDao.getNewWordsForStudy(count, stage, domain).toDomainWithOverrides()

    override suspend fun getWordById(id: Int): Word? =
        wordDao.getWordById(id)?.toDomain()?.withOverride(primaryOverrides())

    override suspend fun getWordsByIds(ids: List<Int>): List<Word> =
        wordDao.getWordsByIds(ids).toDomainWithOverrides()

    override suspend fun getWordIdByText(word: String): Int? =
        wordDao.getWordIdByText(word)

    override suspend fun getWordOfTheDay(): Word? {
        val seed = LocalDate.now().toEpochDay()
        return wordDao.getWordOfTheDay(seed)?.toDomain()?.withOverride(primaryOverrides())
    }

    override suspend fun getRandomWordsInStage(stage: Int, excludeId: Int, count: Int): List<Word> =
        wordDao.getRandomWordsInStage(stage, excludeId, count).toDomainWithOverrides()

    override suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int): List<Word> =
        wordDao.getRandomWordsInDomain(domain, excludeId, count).toDomainWithOverrides()

    override fun getTotalWordCount(): Flow<Int> = wordDao.getTotalWordCount()

    override fun getWordCountByStage(stage: Int): Flow<Int> = wordDao.getWordCountByStage(stage)

    override fun getWordCountByDomain(domain: String): Flow<Int> = wordDao.getWordCountByDomain(domain)

    override fun getAllDomains(): Flow<List<String>> = wordDao.getAllDomains()

    override fun getAllStages(): Flow<List<Int>> = wordDao.getAllStages()

    override suspend fun getRandomWordsByStage(stage: Int, count: Int): List<Word> =
        wordDao.getRandomWordsByStage(stage, count).toDomainWithOverrides()

    override suspend fun getDailyChallengeWords(seed: Long, count: Int): List<Word> =
        wordDao.getDailyChallengeWords(seed, count).toDomainWithOverrides()

    override suspend fun getRandomWordsExcluding(excludeIds: List<Int>, count: Int): List<Word> =
        wordDao.getRandomWordsExcluding(excludeIds, count).toDomainWithOverrides()

    override fun getMeaningsForWord(wordId: Int): Flow<List<WordMeaning>> =
        wordMeaningDao.getMeaningsForWord(wordId).map { entities -> entities.map { it.toDomain() } }

    override fun getExamplesForWord(wordId: Int): Flow<List<WordExample>> =
        wordExampleDao.getExamplesForWord(wordId).map { entities -> entities.map { it.toDomain() } }
}
