package com.wcjung.engstudy.ui.screen.worddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.ui.navigation.Screen
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val learningRepository: LearningRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.WordDetail>()
    private val wordId = route.wordId

    private val _word = MutableStateFlow<Word?>(null)
    val word: StateFlow<Word?> = _word

    val isBookmarked: StateFlow<Boolean> = bookmarkRepository.isBookmarked(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            _word.value = wordRepository.getWordById(wordId)
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    private val _isMarkedAsKnown = MutableStateFlow(false)
    val isMarkedAsKnown: StateFlow<Boolean> = _isMarkedAsKnown

    fun markAsKnown() {
        viewModelScope.launch {
            learningRepository.markAsKnown(wordId)
            _isMarkedAsKnown.value = true
        }
    }
}
