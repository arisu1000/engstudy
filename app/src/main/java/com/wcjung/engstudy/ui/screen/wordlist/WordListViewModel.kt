package com.wcjung.engstudy.ui.screen.wordlist

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val learningRepository: LearningRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.WordList>()
    val domain: String? = route.domain
    val stage: Int? = route.stage

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
                stage = stage,
                limit = pageSize,
                offset = currentOffset
            ).collect { words ->
                _words.value = words
                _isLoading.value = false
            }
        }
    }

    val bookmarkedIds: StateFlow<Set<Int>> = bookmarkRepository.getBookmarkedWords()
        .map { words -> words.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleBookmark(wordId: Int) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    fun markAsKnown(wordId: Int) {
        viewModelScope.launch {
            learningRepository.markAsKnown(wordId)
            _words.value = _words.value.filter { it.id != wordId }
        }
    }

    fun markMultipleAsKnown(wordIds: List<Int>) {
        viewModelScope.launch {
            wordIds.forEach { learningRepository.markAsKnown(it) }
            _words.value = _words.value.filter { it.id !in wordIds }
        }
    }
}
