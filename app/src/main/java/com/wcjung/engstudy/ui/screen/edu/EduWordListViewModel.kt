package com.wcjung.engstudy.ui.screen.edu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.EduWord
import com.wcjung.engstudy.domain.repository.EduWordRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EduWordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eduWordRepository: EduWordRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.EduWordList>()
    val level: String? = route.level

    private val _words = MutableStateFlow<List<EduWord>>(emptyList())
    val words: StateFlow<List<EduWord>> = _words.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 50

    init {
        if (level == null) {
            viewModelScope.launch {
                eduWordRepository.getAllWords().collect { allWords ->
                    _words.value = allWords
                    _isLoading.value = false
                }
            }
        } else {
            loadMore()
        }
    }

    fun loadMore() {
        if (level == null) return
        viewModelScope.launch {
            val newWords = eduWordRepository.getWordsByLevelPaged(level, pageSize, currentOffset)
                .first()
            _words.value = _words.value + newWords
            currentOffset += newWords.size
            _isLoading.value = false
        }
    }
}
