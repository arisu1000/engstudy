package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.wcjung.engstudy.data.local.dao.EduExcludedWordDao
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.repository.EduWordRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.domain.usecase.RecordQuizAnswerUseCase
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

@HiltViewModel
class EduSpellingQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eduWordRepository: EduWordRepository,
    private val wordRepository: WordRepository,
    private val eduExcludedWordDao: EduExcludedWordDao,
    private val recordQuizAnswer: RecordQuizAnswerUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.EduSpellingQuiz>()
    val level: String? = route.level

    private val _words = MutableStateFlow<List<EduWord>>(emptyList())
    val words: StateFlow<List<EduWord>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    private val _answerState = MutableStateFlow<EduSpellingAnswerState>(EduSpellingAnswerState.Unanswered)
    val answerState: StateFlow<EduSpellingAnswerState> = _answerState

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo: StateFlow<Int> = _maxCombo

    private var correctCount = 0
    private var incorrectCount = 0
    private val incorrectWords = mutableListOf<EduWord>()

    val currentWord: EduWord?
        get() = _words.value.getOrNull(_currentIndex.value)

    init {
        loadWords()
    }

    private fun loadWords() {
        launchSafely {
            val wordList = if (level != null) {
                eduWordRepository.getWordsByLevel(level).first()
            } else {
                eduWordRepository.getAllWords().first()
            }
            val excludedIds = eduExcludedWordDao.getExcludedIds().first().toSet()
            _words.value = wordList.filterNot { it.id in excludedIds }.shuffled().take(10)
        }
    }

    fun updateInput(input: String) {
        if (_answerState.value is EduSpellingAnswerState.Unanswered) {
            _userInput.value = input
        }
    }

    fun submitAnswer() {
        val word = currentWord ?: return
        if (_answerState.value !is EduSpellingAnswerState.Unanswered) return

        val isCorrect = _userInput.value.trim().equals(word.word, ignoreCase = true)
        _answerState.value = if (isCorrect) {
            EduSpellingAnswerState.Correct
        } else {
            EduSpellingAnswerState.Incorrect(word.word)
        }

        if (isCorrect) {
            correctCount++
            _comboCount.value++
            if (_comboCount.value > _maxCombo.value) {
                _maxCombo.value = _comboCount.value
            }
        } else {
            incorrectCount++
            _comboCount.value = 0
            incorrectWords.add(word)
        }

        // 교육부 단어도 SM-2 복습 루프에 포함: words 테이블의 동일 단어에 진도 기록.
        // words에 없는 단어(교육부 3,000 중 ~8%)는 복습 추적 없이 넘어간다.
        launchSafely {
            val wordId = wordRepository.getWordIdByText(word.word) ?: return@launchSafely
            recordQuizAnswer(
                wordId = wordId,
                isCorrect = isCorrect,
                quizType = "edu_spelling",
                wrongAnswer = _userInput.value.trim(),
                correctAnswer = word.word
            )
        }
    }

    fun nextQuestion() {
        _userInput.value = ""
        _answerState.value = EduSpellingAnswerState.Unanswered
        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= _words.value.size) {
            _isFinished.value = true
        } else {
            _currentIndex.value = nextIndex
        }
    }

    fun getCorrectCount() = correctCount
    fun getIncorrectCount() = incorrectCount
    fun getIncorrectWords() = incorrectWords.toList()
}

sealed interface EduSpellingAnswerState {
    data object Unanswered : EduSpellingAnswerState
    data object Correct : EduSpellingAnswerState
    data class Incorrect(val correctAnswer: String) : EduSpellingAnswerState
}
