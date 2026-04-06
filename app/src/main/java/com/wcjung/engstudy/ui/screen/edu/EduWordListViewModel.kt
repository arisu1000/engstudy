package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.data.local.dao.KnownItemDao
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.repository.EduWordRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EduWordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eduWordRepository: EduWordRepository,
    private val knownItemDao: KnownItemDao
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.EduWordList>()
    val level: String? = route.level

    private val _allWords = MutableStateFlow<List<EduWord>>(emptyList())

    private val _hideKnown = MutableStateFlow(false)
    val hideKnown: StateFlow<Boolean> = _hideKnown.asStateFlow()

    val knownIds: StateFlow<Set<Int>> = knownItemDao.getKnownIds(ITEM_TYPE)
        .combine(MutableStateFlow(Unit)) { ids, _ -> ids.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** 필터링된 단어 목록: hideKnown이 true이면 이미 아는 단어를 숨긴다 */
    val words: StateFlow<List<EduWord>> = combine(
        _allWords, knownIds, _hideKnown
    ) { all, known, hide ->
        if (hide) all.filter { it.id !in known } else all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val source = if (level == null) eduWordRepository.getAllWords()
                         else eduWordRepository.getWordsByLevel(level)
            source.collect { allWords ->
                _allWords.value = allWords
                _isLoading.value = false
            }
        }
    }

    fun markAsKnown(wordId: Int) {
        viewModelScope.launch {
            knownItemDao.markAsKnown(KnownItemEntity(itemId = wordId, itemType = ITEM_TYPE))
        }
    }

    fun unmarkKnown(wordId: Int) {
        viewModelScope.launch {
            knownItemDao.unmarkKnown(wordId, ITEM_TYPE)
        }
    }

    fun toggleHideKnown() {
        _hideKnown.value = !_hideKnown.value
    }

    companion object {
        const val ITEM_TYPE = "edu_word"
    }
}
