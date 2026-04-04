package com.wcjung.engstudy.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val learningRepository: LearningRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _wordOfTheDay = MutableStateFlow<Word?>(null)
    val wordOfTheDay: StateFlow<Word?> = _wordOfTheDay

    val dueReviewCount: StateFlow<Int> = learningRepository.getDueReviewCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedWordCount: StateFlow<Int> = learningRepository.getLearnedWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWordCount: StateFlow<Int> = wordRepository.getTotalWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 단계별 총 단어 수 (stage level -> count) */
    val stageWordCounts: StateFlow<Map<Int, Int>> = combine(
        Stage.entries.map { stage -> wordRepository.getWordCountByStage(stage.level) }
    ) { counts ->
        Stage.entries.mapIndexed { index, stage -> stage.level to counts[index] }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** 단계별 학습 완료 단어 수 (stage level -> learned count) */
    val stageLearnedCounts: StateFlow<Map<Int, Int>> = combine(
        Stage.entries.map { stage -> learningRepository.getLearnedWordCountByStage(stage.level) }
    ) { counts ->
        Stage.entries.mapIndexed { index, stage -> stage.level to counts[index] }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** 일일 학습 목표 */
    val dailyGoal: StateFlow<Int> = userPreferences.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    /** 오늘 학습(복습)한 단어 수 */
    val todayLearnedCount: StateFlow<Int> = run {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        learningRepository.getReviewedCountForDay(dayStart, dayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    /** 레벨 테스트 완료 여부 */
    /** 초기값 false: DataStore 로딩 전에 레벨 테스트 배너가 잠깐 보이는 것이 숨기는 것보다 안전하다 */
    val hasCompletedPlacementTest: StateFlow<Boolean> = userPreferences.hasCompletedPlacementTest
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadWordOfTheDay()
    }

    private fun loadWordOfTheDay() {
        viewModelScope.launch {
            _wordOfTheDay.value = wordRepository.getWordOfTheDay()
        }
    }
}
