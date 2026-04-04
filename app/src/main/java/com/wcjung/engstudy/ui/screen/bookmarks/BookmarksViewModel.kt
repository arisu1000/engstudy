package com.wcjung.engstudy.ui.screen.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    val ttsManager: TtsManager
) : ViewModel() {

    val bookmarkedWords: StateFlow<List<Word>> = bookmarkRepository.getBookmarkedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBookmark(wordId: Int) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    /** 즐겨찾기 단어를 공유용 텍스트로 반환 */
    fun getShareText(): String {
        return bookmarkedWords.value.joinToString("\n") { word ->
            "${word.word} - ${word.meaning}"
        }
    }
}
