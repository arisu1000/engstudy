package com.wcjung.engstudy.ui.screen.grammar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wcjung.engstudy.domain.model.ExampleSentence
import com.wcjung.engstudy.domain.repository.SentenceRepository
import com.wcjung.engstudy.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrammarListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sentenceRepository: SentenceRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.GrammarList>()
    val topic: String? = route.topic

    private val _sentences = MutableStateFlow<List<ExampleSentence>>(emptyList())
    val sentences: StateFlow<List<ExampleSentence>> = _sentences.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSentences()
    }

    private fun loadSentences() {
        viewModelScope.launch {
            val flow = if (topic != null) {
                sentenceRepository.getByGrammarTopic(topic)
            } else {
                sentenceRepository.getAllSentences()
            }
            flow.collect { list ->
                _sentences.value = list
                _isLoading.value = false
            }
        }
    }
}
