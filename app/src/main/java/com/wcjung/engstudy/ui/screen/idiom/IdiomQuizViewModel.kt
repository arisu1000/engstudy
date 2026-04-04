package com.wcjung.engstudy.ui.screen.idiom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.Idiom
import com.wcjung.engstudy.domain.repository.IdiomRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IdiomQuizQuestion(
    val idiom: Idiom,
    val options: List<String>,
    val correctIndex: Int
)

@HiltViewModel
class IdiomQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val idiomRepository: IdiomRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.IdiomQuiz>()
    val type: String? = route.type

    private val _questions = MutableStateFlow<List<IdiomQuizQuestion>>(emptyList())
    val questions: StateFlow<List<IdiomQuizQuestion>> = _questions.asStateFlow()

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
    private val incorrectIdioms = mutableListOf<Idiom>()

    val currentQuestion: IdiomQuizQuestion?
        get() = _questions.value.getOrNull(_currentIndex.value)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val idiomList = if (type != null) {
                idiomRepository.getByType(type).first()
            } else {
                idiomRepository.getAllIdioms().first()
            }

            val selectedIdioms = idiomList.shuffled().take(20)

            val questions = selectedIdioms.map { idiom ->
                val distractors = idiomRepository.getRandomIdioms(
                    excludeId = idiom.id,
                    count = 3
                )
                val options = (distractors.map { it.phrase } + idiom.phrase).shuffled()
                IdiomQuizQuestion(
                    idiom = idiom,
                    options = options,
                    correctIndex = options.indexOf(idiom.phrase)
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
            incorrectIdioms.add(question.idiom)
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
    fun getIncorrectIdioms() = incorrectIdioms.toList()
}
