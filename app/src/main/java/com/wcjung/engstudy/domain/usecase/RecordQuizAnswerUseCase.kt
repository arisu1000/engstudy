package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating
import javax.inject.Inject

/**
 * 퀴즈 정답/오답을 학습 기록에 반영하는 공통 UseCase.
 *
 * SM-2 진도 갱신 + 오답 노트 기록 + 스트릭 갱신을 한 번에 처리한다.
 * 일반 퀴즈, 스펠링, 듣기, 교육부 퀴즈가 공유한다.
 */
class RecordQuizAnswerUseCase @Inject constructor(
    private val learningRepository: LearningRepository,
    private val wrongAnswerRepository: WrongAnswerRepository,
    private val spacedRepetition: CalculateSpacedRepetitionUseCase,
    private val updateStreak: UpdateStreakUseCase
) {
    /**
     * @param wrongAnswer 오답일 때 사용자가 고른 답 (오답 노트 기록용)
     * @param correctAnswer 오답일 때의 정답 표기
     */
    suspend operator fun invoke(
        wordId: Int,
        isCorrect: Boolean,
        quizType: String,
        wrongAnswer: String? = null,
        correctAnswer: String? = null
    ) {
        val progress = learningRepository.getProgressForWord(wordId)
            ?: LearningProgress(wordId = wordId)
        val rating = if (isCorrect) SimpleRating.GOOD else SimpleRating.AGAIN
        val quality = spacedRepetition.qualityFromSimpleRating(rating)
        val result = spacedRepetition.calculate(progress, quality)

        if (!isCorrect && wrongAnswer != null && correctAnswer != null) {
            wrongAnswerRepository.insertWrongAnswer(wordId, wrongAnswer, correctAnswer, quizType)
        }

        learningRepository.updateProgress(
            progress.copy(
                easeFactor = result.easeFactor,
                intervalDays = result.intervalDays,
                repetitions = result.repetitions,
                nextReviewDate = result.nextReviewDate,
                lastReviewedDate = System.currentTimeMillis(),
                timesCorrect = progress.timesCorrect + if (isCorrect) 1 else 0,
                timesIncorrect = progress.timesIncorrect + if (!isCorrect) 1 else 0,
                isLearned = result.isLearned
            )
        )
        updateStreak()
    }
}
