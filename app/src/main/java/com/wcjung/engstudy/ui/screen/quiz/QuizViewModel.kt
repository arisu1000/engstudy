package com.wcjung.engstudy.ui.screen.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating
import com.wcjung.engstudy.domain.usecase.UpdateStreakUseCase
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int,
    val isEnToKo: Boolean
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val learningRepository: LearningRepository,
    private val spacedRepetition: CalculateSpacedRepetitionUseCase,
    private val updateStreak: UpdateStreakUseCase,
    private val wrongAnswerRepository: WrongAnswerRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Quiz>()

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private var correctCount = 0
    private var incorrectCount = 0
    private val incorrectWords = mutableListOf<Word>()

    val currentQuestion: QuizQuestion?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val words = wordRepository.getNewWordsForStudy(
                count = 20,
                stage = route.stage,
                domain = route.domain
            ).first()

            val questions = words.mapIndexed { index, word ->
                val isEnToKo = index % 2 == 0
                val distractors = wordRepository.getRandomWordsInStage(
                    stage = word.stage.level,
                    excludeId = word.id,
                    count = 3
                )
                val distractorTexts = if (isEnToKo) {
                    distractors.map { it.meaning }
                } else {
                    distractors.map { it.word }
                }
                // 정답의 원래 인덱스를 추적하여 중복 의미가 있어도 정확히 식별한다
                val allOptions = distractorTexts + if (isEnToKo) word.meaning else word.word
                val indexed = allOptions.mapIndexed { i, s -> i to s }.shuffled()
                val correctOriginalIndex = distractorTexts.size // 정답은 항상 마지막에 추가됨
                val correctIndex = indexed.indexOfFirst { it.first == correctOriginalIndex }
                val options = indexed.map { it.second }
                QuizQuestion(
                    word = word,
                    options = options,
                    correctIndex = correctIndex,
                    isEnToKo = isEnToKo
                )
            }
            _questions.value = questions
        }
    }

    fun selectAnswer(index: Int) {
        if (_selectedAnswer.value != null) return
        _selectedAnswer.value = index

        val question = currentQuestion ?: return
        val isCorrect = index == question.correctIndex

        viewModelScope.launch {
            val progress = learningRepository.getProgressForWord(question.word.id)
                ?: LearningProgress(wordId = question.word.id)
            val rating = if (isCorrect) SimpleRating.GOOD else SimpleRating.AGAIN
            val quality = spacedRepetition.qualityFromSimpleRating(rating)
            val result = spacedRepetition.calculate(progress, quality)

            if (isCorrect) {
                correctCount++
            } else {
                incorrectCount++
                incorrectWords.add(question.word)
                val userAnswer = question.options[index]
                val correctAnswer = question.options[question.correctIndex]
                wrongAnswerRepository.insertWrongAnswer(
                    wordId = question.word.id,
                    wrongAnswer = userAnswer,
                    correctAnswer = correctAnswer,
                    quizType = "quiz"
                )
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

    fun nextQuestion() {
        _selectedAnswer.value = null
        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= _questions.value.size) {
            _isFinished.value = true
        } else {
            _currentIndex.value = nextIndex
        }
    }

    fun getCorrectCount() = correctCount
    fun getIncorrectCount() = incorrectCount
    fun getIncorrectWords() = incorrectWords.toList()
}
