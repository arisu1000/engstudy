package com.wcjung.engstudy.ui.screen.excluded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExcludedWordsViewModel @Inject constructor(
    private val learningRepository: LearningRepository
) : ViewModel() {

    val excludedWords: StateFlow<List<Word>> = learningRepository.getExcludedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val excludedCount: StateFlow<Int> = learningRepository.getExcludedWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun restoreWord(wordId: Int) {
        viewModelScope.launch {
            learningRepository.restoreWord(wordId)
        }
    }
}
