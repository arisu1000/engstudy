package com.wcjung.engstudy.ui.screen.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.local.dao.EduBookmarkDao
import com.wcjung.engstudy.data.local.entity.toDomain
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val eduBookmarkDao: EduBookmarkDao,
    val ttsManager: TtsManager
) : ViewModel() {

    val bookmarkedWords: StateFlow<List<Word>> = bookmarkRepository.getBookmarkedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eduBookmarkedWords: StateFlow<List<EduWord>> = eduBookmarkDao.getBookmarkedWords()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBookmark(wordId: Int) {
        launchSafely {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    fun toggleEduBookmark(eduWordId: Int) {
        launchSafely {
            eduBookmarkDao.toggleBookmarkAtomic(eduWordId)
        }
    }

    /** 즐겨찾기 단어를 공유용 텍스트로 반환 */
    fun getShareText(): String {
        return bookmarkedWords.value.joinToString("\n") { word ->
            "${word.word} - ${word.meaning}"
        }
    }
}
