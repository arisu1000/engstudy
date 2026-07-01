package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.domain.model.Domain
import com.wcjung.engstudy.domain.model.MeaningType
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.WordExample
import com.wcjung.engstudy.domain.model.WordMeaning
import com.wcjung.engstudy.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * GetDailyChallengeUseCase 테스트.
 *
 * 핵심 계약: 오늘 날짜의 epoch day를 시드로 사용해야 한다.
 * 이 시드가 결정적이어야 서버 없이도 같은 날 모든 기기에서 동일한 단어가 나온다.
 */
class GetDailyChallengeUseCaseTest {

    private fun word(id: Int) = Word(
        id = id,
        word = "word$id",
        pronunciation = "",
        meaning = "의미$id",
        meaningType = MeaningType.KOREAN,
        partOfSpeech = "noun",
        exampleEn = "",
        exampleKo = "",
        stage = Stage.FOUNDATION,
        domain = Domain.GENERAL,
        frequencyRank = id,
        difficulty = 1,
        synonyms = emptyList(),
        antonyms = emptyList(),
        notes = null
    )

    /** getDailyChallengeWords / getRandomWordsExcluding 호출 인자를 기록하는 테스트 더블. */
    private class FakeWordRepository(
        private val challengeResult: List<Word> = emptyList(),
        private val distractorResult: List<Word> = emptyList()
    ) : WordRepository {
        var lastSeed: Long? = null
        var lastCount: Int? = null
        var lastExcludeIds: List<Int>? = null

        override suspend fun getDailyChallengeWords(seed: Long, count: Int): List<Word> {
            lastSeed = seed
            lastCount = count
            return challengeResult
        }

        override suspend fun getRandomWordsExcluding(excludeIds: List<Int>, count: Int): List<Word> {
            lastExcludeIds = excludeIds
            return distractorResult
        }

        // 이 UseCase가 사용하지 않는 나머지 계약은 테스트 대상이 아니다.
        override fun getWordsByFilter(stage: Int?, domain: String?, limit: Int, offset: Int): Flow<List<Word>> = notUsed()
        override suspend fun getWordsPage(stage: Int?, domain: String?, showExcluded: Boolean, limit: Int, offset: Int): List<Word> = notUsed()
        override fun searchWords(query: String): Flow<List<Word>> = notUsed()
        override fun getNewWordsForStudy(count: Int, stage: Int?, domain: String?): Flow<List<Word>> = notUsed()
        override suspend fun getWordById(id: Int): Word? = notUsed()
        override suspend fun getWordOfTheDay(): Word? = notUsed()
        override suspend fun getRandomWordsInStage(stage: Int, excludeId: Int, count: Int): List<Word> = notUsed()
        override suspend fun getRandomWordsInDomain(domain: String, excludeId: Int, count: Int): List<Word> = notUsed()
        override fun getTotalWordCount(): Flow<Int> = notUsed()
        override fun getWordCountByStage(stage: Int): Flow<Int> = notUsed()
        override fun getWordCountByDomain(domain: String): Flow<Int> = notUsed()
        override fun getAllDomains(): Flow<List<String>> = notUsed()
        override fun getAllStages(): Flow<List<Int>> = notUsed()
        override suspend fun getRandomWordsByStage(stage: Int, count: Int): List<Word> = notUsed()
        override fun getMeaningsForWord(wordId: Int): Flow<List<WordMeaning>> = notUsed()
        override fun getExamplesForWord(wordId: Int): Flow<List<WordExample>> = notUsed()

        private fun notUsed(): Nothing = throw NotImplementedError("테스트에서 사용하지 않는 메서드")
    }

    @Test
    fun `오늘 단어 조회 시 오늘 날짜의 epoch day를 시드로 사용한다`() = runTest {
        val repo = FakeWordRepository()
        val useCase = GetDailyChallengeUseCase(repo)

        useCase.getTodayWords()

        assertEquals(LocalDate.now().toEpochDay(), repo.lastSeed)
    }

    @Test
    fun `요청한 개수가 리포지토리로 그대로 전달된다`() = runTest {
        val repo = FakeWordRepository()
        val useCase = GetDailyChallengeUseCase(repo)

        useCase.getTodayWords(count = 5)

        assertEquals(5, repo.lastCount)
    }

    @Test
    fun `기본 개수는 10개다`() = runTest {
        val repo = FakeWordRepository()
        val useCase = GetDailyChallengeUseCase(repo)

        useCase.getTodayWords()

        assertEquals(10, repo.lastCount)
    }

    @Test
    fun `리포지토리가 반환한 단어 목록을 그대로 돌려준다`() = runTest {
        val expected = listOf(word(1), word(2), word(3))
        val useCase = GetDailyChallengeUseCase(FakeWordRepository(challengeResult = expected))

        assertEquals(expected, useCase.getTodayWords())
    }

    @Test
    fun `오답 보기 생성 시 챌린지 단어 id를 제외 목록으로 전달한다`() = runTest {
        val repo = FakeWordRepository(distractorResult = listOf(word(7), word(8), word(9)))
        val useCase = GetDailyChallengeUseCase(repo)

        val distractors = useCase.getDistractors(word(1), challengeWordIds = listOf(1, 2, 3))

        assertEquals(listOf(1, 2, 3), repo.lastExcludeIds)
        assertEquals(3, distractors.size)
    }
}
