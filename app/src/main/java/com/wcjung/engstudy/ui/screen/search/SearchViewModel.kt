package com.wcjung.engstudy.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val bookmarkRepository: BookmarkRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val searchResults: StateFlow<List<Word>> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.length >= 2) wordRepository.searchWords(q) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(query: String) {
        _query.value = query
    }

    fun isBookmarked(wordId: Int) = bookmarkRepository.isBookmarked(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleBookmark(wordId: Int) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }
}
