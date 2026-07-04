package com.wcjung.engstudy.ui.screen.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.domain.usecase.RecordQuizAnswerUseCase
import com.wcjung.engstudy.ui.navigation.Screen
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import com.wcjung.engstudy.util.launchSafely
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
    private val recordQuizAnswer: RecordQuizAnswerUseCase,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Quiz>()

    /** 듣기 모드: 문제를 TTS로만 제시하고 정답 선택 후 단어를 공개한다. */
    val isListening: Boolean = route.listening

    fun speakCurrentWord() {
        currentQuestion?.let { ttsManager.speak(it.word.word) }
    }

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo: StateFlow<Int> = _maxCombo

    private var correctCount = 0
    private var incorrectCount = 0
    private val incorrectWords = mutableListOf<Word>()

    val currentQuestion: QuizQuestion?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        launchSafely {
            // wordIds 지정 시(오답노트 재도전) 해당 단어로만 출제
            val words = route.wordIds?.takeIf { it.isNotEmpty() }
                ?.let { wordRepository.getWordsByIds(it).shuffled() }
                ?: wordRepository.getNewWordsForStudy(
                    count = 20,
                    stage = route.stage,
                    domain = route.domain
                ).first()

            val questions = words.mapIndexed { index, word ->
                // 듣기 모드는 항상 "단어를 듣고 뜻 고르기" (영→한)
                val isEnToKo = route.listening || index % 2 == 0
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

        if (isCorrect) {
            correctCount++
            _comboCount.value++
            if (_comboCount.value > _maxCombo.value) {
                _maxCombo.value = _comboCount.value
            }
        } else {
            incorrectCount++
            _comboCount.value = 0
            incorrectWords.add(question.word)
        }

        launchSafely {
            recordQuizAnswer(
                wordId = question.word.id,
                isCorrect = isCorrect,
                quizType = if (route.listening) "listening_quiz" else "quiz",
                wrongAnswer = question.options[index],
                correctAnswer = question.options[question.correctIndex]
            )
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
