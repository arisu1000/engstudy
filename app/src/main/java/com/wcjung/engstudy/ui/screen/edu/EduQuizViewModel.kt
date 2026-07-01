package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.repository.EduWordRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

data class EduQuizQuestion(
    val word: EduWord,
    val options: List<String>,
    val correctIndex: Int
)

@HiltViewModel
class EduQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eduWordRepository: EduWordRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.EduQuiz>()
    val level: String? = route.level

    private val _questions = MutableStateFlow<List<EduQuizQuestion>>(emptyList())
    val questions: StateFlow<List<EduQuizQuestion>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo: StateFlow<Int> = _maxCombo.asStateFlow()

    private var correctCount = 0
    private var incorrectCount = 0
    private val incorrectWords = mutableListOf<EduWord>()

    val currentQuestion: EduQuizQuestion?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        launchSafely {
            val wordList = if (level != null) {
                eduWordRepository.getWordsByLevel(level).first()
            } else {
                eduWordRepository.getAllWords().first()
            }

            val selectedWords = wordList.shuffled().take(20)

            val questions = selectedWords.map { word ->
                val effectiveLevel = level ?: word.level.key
                val distractors = eduWordRepository.getRandomWordsInLevel(
                    level = effectiveLevel,
                    excludeId = word.id,
                    count = 3
                )
                val options = (distractors.map { it.word } + word.word).shuffled()
                EduQuizQuestion(
                    word = word,
                    options = options,
                    correctIndex = options.indexOf(word.word)
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
