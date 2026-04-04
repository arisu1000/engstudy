package com.wcjung.engstudy.ui.screen.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.usecase.GetDailyChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int
)

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val getDailyChallenge: GetDailyChallengeUseCase
) : ViewModel() {

    private val _questions = MutableStateFlow<List<ChallengeQuestion>>(emptyList())
    val questions: StateFlow<List<ChallengeQuestion>> = _questions.asStateFlow()

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
    private var startTimeMillis = 0L

    val currentQuestion: ChallengeQuestion?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadChallenge()
    }

    private fun loadChallenge() {
        viewModelScope.launch {
            val words = getDailyChallenge.getTodayWords()
            if (words.isEmpty()) return@launch

            val wordIds = words.map { it.id }

            val questions = words.map { word ->
                val distractors = getDailyChallenge.getDistractors(word, wordIds, 3)
                val allOptions = distractors.map { it.word } + word.word
                val indexed = allOptions.mapIndexed { i, s -> i to s }.shuffled()
                val correctOriginalIndex = allOptions.size - 1
                val correctIndex = indexed.indexOfFirst { it.first == correctOriginalIndex }
                val options = indexed.map { it.second }
                ChallengeQuestion(
                    word = word,
                    options = options,
                    correctIndex = correctIndex
                )
            }
            _questions.value = questions
            startTimeMillis = System.currentTimeMillis()
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
            _comboCount.value = 0
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

    fun getElapsedSeconds(): Int {
        return ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
    }

    /**
     * 결과 공유용 텍스트를 생성한다.
     * 날짜 기반 시드를 사용하므로 같은 날 같은 문제를 푼 가족과 결과를 비교할 수 있다.
     */
    fun generateShareText(): String {
        val total = _questions.value.size
        val seconds = getElapsedSeconds()
        val today = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return buildString {
            appendLine("\uD83D\uDCDA EngStudy \uC77C\uC77C \uCC4C\uB9B0\uC9C0")
            appendLine("\uD83D\uDCC5 ${today.format(formatter)}")
            appendLine("\u2705 $correctCount/$total \uC815\uB2F5")
            appendLine("\u23F1 ${seconds}\uCD08")
            if (_maxCombo.value >= 3) {
                appendLine("\uD83D\uDD25 \uCD5C\uB300 \uCF64\uBCF4: ${_maxCombo.value}\uC5F0\uC18D")
            }
            append("#EngStudy #\uC77C\uC77C\uCC4C\uB9B0\uC9C0")
        }
    }
}
