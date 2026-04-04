package com.wcjung.engstudy.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    wordRepository: WordRepository,
    learningRepository: LearningRepository
) : ViewModel() {

    val totalWords: StateFlow<Int> = wordRepository.getTotalWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedWords: StateFlow<Int> = learningRepository.getLearnedWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressWords: StateFlow<Int> = learningRepository.getInProgressWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dueReviews: StateFlow<Int> = learningRepository.getDueReviewCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStudyDays: StateFlow<Int> = learningRepository.getTotalStudyDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedByDomain: StateFlow<Map<String, Int>> = learningRepository.getLearnedCountByDomain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
