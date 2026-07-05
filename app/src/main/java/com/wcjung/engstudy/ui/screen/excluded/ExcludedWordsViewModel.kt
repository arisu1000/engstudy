package com.wcjung.engstudy.ui.screen.excluded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.local.dao.EduExcludedWordDao
import com.wcjung.engstudy.data.local.entity.toDomain
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

@HiltViewModel
class ExcludedWordsViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    private val eduExcludedWordDao: EduExcludedWordDao
) : ViewModel() {

    val excludedWords: StateFlow<List<Word>> = learningRepository.getExcludedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val excludedCount: StateFlow<Int> = learningRepository.getExcludedWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val eduExcludedWords: StateFlow<List<EduWord>> = eduExcludedWordDao.getExcludedWords()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eduExcludedCount: StateFlow<Int> = eduExcludedWordDao.getExcludedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun restoreWord(wordId: Int) {
        launchSafely {
            learningRepository.restoreWord(wordId)
        }
    }

    fun restoreEduWord(eduWordId: Int) {
        launchSafely {
            eduExcludedWordDao.restoreWord(eduWordId)
        }
    }
}
