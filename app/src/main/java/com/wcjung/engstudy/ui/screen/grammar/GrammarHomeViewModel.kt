package com.wcjung.engstudy.ui.screen.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.domain.repository.SentenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicInfo(
    val topicKo: String,
    val topic: String,
    val count: Int
)

@HiltViewModel
class GrammarHomeViewModel @Inject constructor(
    private val sentenceRepository: SentenceRepository
) : ViewModel() {

    val totalCount: StateFlow<Int> = sentenceRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _topics = MutableStateFlow<List<TopicInfo>>(emptyList())
    val topics: StateFlow<List<TopicInfo>> = _topics.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTopics()
    }

    /**
     * 모든 grammar_topic_ko 값을 가져온 뒤 각 topic의 실제 grammar_topic(영문 키)과 개수를 조회한다.
     * topic 목록이 적으므로(약 20개) 순차 조회해도 성능 문제 없다.
     */
    private fun loadTopics() {
        viewModelScope.launch {
            val topicNames = sentenceRepository.getAllTopics().first()
            val result = mutableListOf<TopicInfo>()
            for (name in topicNames) {
                val sentences = sentenceRepository.getByGrammarTopic(name).first()
                val count = sentences.size
                val topicKey = sentences.firstOrNull()?.grammarTopic ?: name
                result.add(TopicInfo(topicKo = name, topic = topicKey, count = count))
            }
            _topics.value = result
            _isLoading.value = false
        }
    }
}
