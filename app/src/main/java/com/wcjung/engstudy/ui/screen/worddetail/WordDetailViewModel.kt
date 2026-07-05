package com.wcjung.engstudy.ui.screen.worddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.data.local.dao.UserWordDao
import com.wcjung.engstudy.data.local.dao.isUserWordId
import com.wcjung.engstudy.domain.model.UserWordMeaning
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.WordMeaning
import com.wcjung.engstudy.domain.model.WordExample
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.UserWordMeaningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.ui.navigation.Screen
import com.wcjung.engstudy.util.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val learningRepository: LearningRepository,
    private val userWordMeaningRepository: UserWordMeaningRepository,
    private val userWordDao: UserWordDao,
    val ttsManager: TtsManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.WordDetail>()
    private val wordId = route.wordId

    /** 사용자가 직접 추가한 단어 여부 (id 예약 구간으로 판별) — 삭제 가능 */
    val isUserWord: Boolean = isUserWordId(wordId)

    private val _word = MutableStateFlow<Word?>(null)
    val word: StateFlow<Word?> = _word

    val isBookmarked: StateFlow<Boolean> = bookmarkRepository.isBookmarked(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val meanings: StateFlow<List<WordMeaning>> = wordRepository.getMeaningsForWord(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examples: StateFlow<List<WordExample>> = wordRepository.getExamplesForWord(wordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 사용자가 추가/변경한 의미 (isPrimary=대표 뜻 교체분 포함) */
    val userMeanings: StateFlow<List<UserWordMeaning>> =
        userWordMeaningRepository.getForWord(wordId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshWord()
    }

    private fun refreshWord() {
        launchSafely {
            _word.value = wordRepository.getWordById(wordId)
        }
    }

    /** 대표 뜻을 사용자 입력으로 변경 — 목록/퀴즈/복습에도 반영된다 */
    fun editPrimaryMeaning(meaning: String) {
        launchSafely {
            userWordMeaningRepository.setPrimaryMeaning(wordId, meaning)
            refreshWord()
        }
    }

    /** 대표 뜻 변경 취소, 기본 뜻으로 복원 */
    fun resetPrimaryMeaning() {
        launchSafely {
            userWordMeaningRepository.clearPrimaryMeaning(wordId)
            refreshWord()
        }
    }

    fun addUserMeaning(meaning: String) {
        launchSafely {
            userWordMeaningRepository.addMeaning(wordId, meaning)
        }
    }

    fun deleteUserMeaning(id: Long) {
        launchSafely {
            userWordMeaningRepository.deleteMeaning(id)
        }
    }

    fun toggleBookmark() {
        launchSafely {
            bookmarkRepository.toggleBookmark(wordId)
        }
    }

    private val _isMarkedAsKnown = MutableStateFlow(false)
    val isMarkedAsKnown: StateFlow<Boolean> = _isMarkedAsKnown

    fun markAsKnown() {
        launchSafely {
            learningRepository.markAsKnown(wordId)
            _isMarkedAsKnown.value = true
        }
    }

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted

    /** 사용자 추가 단어 삭제 — 학습 기록·북마크·오답도 FK CASCADE로 함께 삭제된다 */
    fun deleteUserWord() {
        launchSafely {
            if (userWordDao.deleteUserWord(wordId) > 0) {
                _isDeleted.value = true
            }
        }
    }
}
