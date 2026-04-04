package com.wcjung.engstudy.ui.screen.wordlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.BookmarkRepository
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
class WordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val bookmarkRepository: BookmarkRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.WordList>()
    val domain: String? = route.domain
    val ageGroup: String? = route.ageGroup

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentOffset = 0
    private val pageSize = 50

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            wordRepository.getWordsByFilter(
                domain = domain,
                ageGroup = ageGroup,
                limit = pageSize,
                offset = currentOffset
            ).collect { words ->
                _words.value = words
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(wordId: Int) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    fun isBookmarked(wordId: Int) = bookmarkRepository.isBookmarked(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
