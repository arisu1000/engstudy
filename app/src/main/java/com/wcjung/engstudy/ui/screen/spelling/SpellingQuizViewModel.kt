package com.wcjung.engstudy.ui.screen.spelling

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellingQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val learningRepository: LearningRepository,
    private val spacedRepetition: CalculateSpacedRepetitionUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.SpellingQuiz>()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _userInput = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput

    private val _answerState = MutableStateFlow<AnswerState>(AnswerState.Unanswered)
    val answerState: StateFlow<AnswerState> = _answerState

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private var correctCount = 0
    private var incorrectCount = 0
    private val incorrectWords = mutableListOf<Word>()

    val currentWord: Word?
        get() = _words.value.getOrNull(_currentIndex.value)

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            val words = wordRepository.getNewWordsForStudy(
                count = 10,
                ageGroup = route.ageGroup,
                domain = route.domain
            ).first()
            _words.value = words
        }
    }

    fun updateInput(input: String) {
        if (_answerState.value is AnswerState.Unanswered) {
            _userInput.value = input
        }
    }

    fun submitAnswer() {
        val word = currentWord ?: return
        if (_answerState.value !is AnswerState.Unanswered) return

        val isCorrect = _userInput.value.trim().equals(word.word, ignoreCase = true)
        _answerState.value = if (isCorrect) AnswerState.Correct else AnswerState.Incorrect(word.word)

        viewModelScope.launch {
            val progress = learningRepository.getProgressForWord(word.id)
                ?: LearningProgress(wordId = word.id)
            val rating = if (isCorrect) SimpleRating.GOOD else SimpleRating.AGAIN
            val quality = spacedRepetition.qualityFromSimpleRating(rating)
            val result = spacedRepetition.calculate(progress, quality)

            if (isCorrect) correctCount++ else {
                incorrectCount++
                incorrectWords.add(word)
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
                    isLearned = result.intervalDays >= 21
                )
            )
        }
    }

    fun nextQuestion() {
        _userInput.value = ""
        _answerState.value = AnswerState.Unanswered
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

sealed interface AnswerState {
    data object Unanswered : AnswerState
    data object Correct : AnswerState
    data class Incorrect(val correctAnswer: String) : AnswerState
}
