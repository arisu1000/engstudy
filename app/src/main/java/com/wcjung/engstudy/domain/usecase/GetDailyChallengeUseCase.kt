package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.WordRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 일일 챌린지용 단어를 선택하는 UseCase.
 *
 * 날짜를 시드로 사용하여 결정적 의사난수 순서로 단어를 선택한다.
 * 서버 없이도 같은 날에는 모든 기기에서 동일한 10개 단어가 나온다.
 * 이를 통해 가족(아버지와 아들)이 같은 단어로 점수를 겨룰 수 있다.
 */
class GetDailyChallengeUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend fun getTodayWords(count: Int = 10): List<Word> {
        val seed = LocalDate.now().toEpochDay()
        return wordRepository.getDailyChallengeWords(seed, count)
    }

    /**
     * 챌린지 문제의 오답 보기를 생성한다.
     * 정답 단어를 제외한 무작위 단어 3개를 반환한다.
     */
    suspend fun getDistractors(correctWord: Word, challengeWordIds: List<Int>, count: Int = 3): List<Word> {
        return wordRepository.getRandomWordsExcluding(challengeWordIds, count)
    }
}
