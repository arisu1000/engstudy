package com.wcjung.engstudy.domain.usecase

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.domain.model.DailyStudyRecord
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.WrongAnswer
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * RecordQuizAnswerUseCase 단위 테스트.
 * 퀴즈 응답이 SM-2 진도·오답 노트·스트릭에 올바르게 반영되는지 검증한다.
 */
class RecordQuizAnswerUseCaseTest {

    private class FakeLearningRepository : LearningRepository {
        val saved = mutableListOf<LearningProgress>()
        var existing: LearningProgress? = null

        override suspend fun getProgressForWord(wordId: Int): LearningProgress? = existing
        override suspend fun updateProgress(progress: LearningProgress) {
            saved.add(progress)
        }

        private fun notUsed(): Nothing = throw AssertionError("이 테스트에서 사용되지 않아야 함")
        override fun getWordsForReview(count: Int): Flow<List<Word>> = notUsed()
        override fun getDueReviewCount(): Flow<Int> = notUsed()
        override fun getLearnedWordCount(): Flow<Int> = notUsed()
        override fun getInProgressWordCount(): Flow<Int> = notUsed()
        override suspend fun markAsKnown(wordId: Int) = notUsed()
        override fun getReviewedCountForDay(dayStart: Long, dayEnd: Long): Flow<Int> = notUsed()
        override fun getTotalStudyDays(): Flow<Int> = notUsed()
        override fun getLearnedCountByStage(): Flow<Map<Int, Int>> = notUsed()
        override fun getLearnedCountByDomain(): Flow<Map<String, Int>> = notUsed()
        override fun getLearnedWordCountByStage(stage: Int): Flow<Int> = notUsed()
        override fun getDailyStudyCounts(sinceTimestamp: Long): Flow<List<DailyStudyRecord>> = notUsed()
        override suspend fun excludeWord(wordId: Int) = notUsed()
        override suspend fun restoreWord(wordId: Int) = notUsed()
        override fun getExcludedWords(): Flow<List<Word>> = notUsed()
        override fun getExcludedWordCount(): Flow<Int> = notUsed()
    }

    private class FakeWrongAnswerRepository : WrongAnswerRepository {
        data class Recorded(val wordId: Int, val wrong: String, val correct: String, val type: String)
        val recorded = mutableListOf<Recorded>()

        override suspend fun insertWrongAnswer(
            wordId: Int, wrongAnswer: String, correctAnswer: String, quizType: String
        ) {
            recorded.add(Recorded(wordId, wrongAnswer, correctAnswer, quizType))
        }

        override fun getRecentWrongAnswers(limit: Int): Flow<List<WrongAnswer>> =
            throw AssertionError("이 테스트에서 사용되지 않아야 함")
        override fun getWrongAnswerCount(): Flow<Int> =
            throw AssertionError("이 테스트에서 사용되지 않아야 함")
    }

    private val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
    private val prefsFile = File(
        System.getProperty("java.io.tmpdir"),
        "record_answer_test_${UUID.randomUUID()}.preferences_pb"
    )
    private val learningRepository = FakeLearningRepository()
    private val wrongAnswerRepository = FakeWrongAnswerRepository()
    private val useCase = RecordQuizAnswerUseCase(
        learningRepository = learningRepository,
        wrongAnswerRepository = wrongAnswerRepository,
        spacedRepetition = CalculateSpacedRepetitionUseCase(),
        updateStreak = UpdateStreakUseCase(
            UserPreferences(PreferenceDataStoreFactory.create(scope = scope) { prefsFile })
        )
    )

    @After
    fun tearDown() {
        scope.cancel()
        prefsFile.delete()
    }

    @Test
    fun correct_answer_updates_progress_without_wrong_note() = runTest {
        useCase(wordId = 7, isCorrect = true, quizType = "edu_quiz")

        val progress = learningRepository.saved.single()
        assertEquals(7, progress.wordId)
        assertEquals(1, progress.timesCorrect)
        assertEquals(0, progress.timesIncorrect)
        assertEquals(1, progress.repetitions)
        assertTrue(wrongAnswerRepository.recorded.isEmpty())
    }

    @Test
    fun wrong_answer_records_wrong_note_and_resets_repetitions() = runTest {
        learningRepository.existing = LearningProgress(
            wordId = 7, repetitions = 4, intervalDays = 10, timesCorrect = 4
        )

        useCase(
            wordId = 7, isCorrect = false, quizType = "quiz",
            wrongAnswer = "잘못된 뜻", correctAnswer = "옳은 뜻"
        )

        val progress = learningRepository.saved.single()
        assertEquals(0, progress.repetitions)
        assertEquals(1, progress.timesIncorrect)
        val note = wrongAnswerRepository.recorded.single()
        assertEquals("잘못된 뜻", note.wrong)
        assertEquals("옳은 뜻", note.correct)
        assertEquals("quiz", note.type)
    }

    @Test
    fun wrong_answer_without_texts_skips_wrong_note() = runTest {
        useCase(wordId = 7, isCorrect = false, quizType = "review")

        assertTrue(wrongAnswerRepository.recorded.isEmpty())
        assertEquals(1, learningRepository.saved.single().timesIncorrect)
    }

    @Test
    fun uses_existing_progress_as_base() = runTest {
        learningRepository.existing = LearningProgress(
            wordId = 7, repetitions = 2, timesCorrect = 2, easeFactor = 2.6f
        )

        useCase(wordId = 7, isCorrect = true, quizType = "quiz")

        val progress = learningRepository.saved.single()
        assertEquals(3, progress.repetitions)
        assertEquals(3, progress.timesCorrect)
    }
}
