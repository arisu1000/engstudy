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
class EduFlashCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eduWordRepository: EduWordRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.EduFlashCard>()
    val level: String? = route.level

    private val _words = MutableStateFlow<List<EduWord>>(emptyList())
    val words: StateFlow<List<EduWord>> = _words.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _showMeaning = MutableStateFlow(false)
    val showMeaning: StateFlow<Boolean> = _showMeaning.asStateFlow()

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            val wordList = if (level != null) {
                eduWordRepository.getWordsByLevel(level).first()
            } else {
                eduWordRepository.getAllWords().first()
            }
            _words.value = wordList.shuffled().take(20)
        }
    }

    fun toggleMeaning() {
        _showMeaning.value = !_showMeaning.value
    }

    fun previousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _showMeaning.value = false
        }
    }

    fun nextCard() {
        if (_currentIndex.value < _words.value.size - 1) {
            _currentIndex.value += 1
            _showMeaning.value = false
        }
    }

    val currentWord: EduWord?
        get() = _words.value.getOrNull(_currentIndex.value)
}
