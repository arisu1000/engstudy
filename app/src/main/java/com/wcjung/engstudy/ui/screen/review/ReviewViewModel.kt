package com.wcjung.engstudy.ui.screen.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    private val spacedRepetition: CalculateSpacedRepetitionUseCase,
    val ttsManager: TtsManager
) : ViewModel() {

    val dueWords: StateFlow<List<Word>> = learningRepository.getWordsForReview(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    val currentWord: Word?
        get() = dueWords.value.getOrNull(_currentIndex.value)

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

            learningRepository.updateProgress(
                progress.copy(
                    easeFactor = result.easeFactor,
                    intervalDays = result.intervalDays,
                    repetitions = result.repetitions,
                    nextReviewDate = result.nextReviewDate,
                    lastReviewedDate = System.currentTimeMillis(),
                    timesCorrect = progress.timesCorrect + if (quality >= 3) 1 else 0,
                    timesIncorrect = progress.timesIncorrect + if (quality < 3) 1 else 0,
                    isLearned = result.intervalDays >= 21
                )
            )

            _isFlipped.value = false
            _currentIndex.value = _currentIndex.value + 1
        }
    }
}
