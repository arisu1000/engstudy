package com.wcjung.engstudy.ui.screen.flashcard

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
import com.wcjung.engstudy.domain.usecase.UpdateStreakUseCase
import com.wcjung.engstudy.ui.navigation.Screen
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val learningRepository: LearningRepository,
    private val spacedRepetition: CalculateSpacedRepetitionUseCase,
    private val updateStreak: UpdateStreakUseCase,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.FlashCard>()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private var correctCount = 0
    private var incorrectCount = 0

    val currentWord: Word?
        get() = _words.value.getOrNull(_currentIndex.value)

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            val words = wordRepository.getNewWordsForStudy(
                count = 20,
                stage = route.stage,
                domain = route.domain
            ).first()
            _words.value = words
        }
    }

    fun flip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun rate(rating: SimpleRating) {
        val word = currentWord ?: return
        viewModelScope.launch {
            val progress = learningRepository.getProgressForWord(word.id)
                ?: LearningProgress(wordId = word.id)
            val quality = spacedRepetition.qualityFromSimpleRating(rating)
            val result = spacedRepetition.calculate(progress, quality)

            if (quality >= 3) correctCount++ else incorrectCount++

            learningRepository.updateProgress(
                progress.copy(
                    easeFactor = result.easeFactor,
                    intervalDays = result.intervalDays,
                    repetitions = result.repetitions,
                    nextReviewDate = result.nextReviewDate,
                    lastReviewedDate = System.currentTimeMillis(),
                    timesCorrect = progress.timesCorrect + if (quality >= 3) 1 else 0,
                    timesIncorrect = progress.timesIncorrect + if (quality < 3) 1 else 0,
                    isLearned = result.isLearned
                )
            )
            updateStreak()

            moveToNext()
        }
    }

    private fun moveToNext() {
        _isFlipped.value = false
        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= _words.value.size) {
            _isFinished.value = true
        } else {
            _currentIndex.value = nextIndex
        }
    }

    fun markAsKnown() {
        val word = currentWord ?: return
        viewModelScope.launch {
            learningRepository.markAsKnown(word.id)
            correctCount++
            moveToNext()
        }
    }

    fun getCorrectCount() = correctCount
    fun getIncorrectCount() = incorrectCount
}
