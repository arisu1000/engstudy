package com.wcjung.engstudy.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.domain.model.Badge
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.domain.model.StudyStatistics
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.util.BadgeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    wordRepository: WordRepository,
    learningRepository: LearningRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    val totalWords: StateFlow<Int> = wordRepository.getTotalWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedWords: StateFlow<Int> = learningRepository.getLearnedWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressWords: StateFlow<Int> = learningRepository.getInProgressWordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dueReviews: StateFlow<Int> = learningRepository.getDueReviewCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStudyDays: StateFlow<Int> = learningRepository.getTotalStudyDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedByStage: StateFlow<Map<Int, Int>> = learningRepository.getLearnedCountByStage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val streakDays: StateFlow<Int> = userPreferences.streakDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 최근 3개월간 일별 학습 단어 수 (날짜 문자열 -> count) */
    val dailyStudyCounts: StateFlow<Map<String, Int>> = run {
        val threeMonthsAgo = LocalDate.now().minusMonths(3)
        val sinceTimestamp = threeMonthsAgo.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        learningRepository.getDailyStudyCounts(sinceTimestamp)
            .map { list -> list.associate { it.study_date to it.count } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    }

    /** 학습 통계를 종합하여 뱃지 목록을 계산한다 */
    val badges: StateFlow<List<Badge>> = combine(
        learningRepository.getLearnedWordCount(),
        userPreferences.streakDays,
        learningRepository.getLearnedCountByStage(),
        learningRepository.getDueReviewCount(),
        learningRepository.getTotalStudyDays()
    ) { learned, streak, byStageInt, dueReview, studyDays ->
        val byStage = byStageInt.entries.associate { (level, count) ->
            Stage.fromLevel(level) to count
        }
        val stats = StudyStatistics(
            totalWords = 0,
            learnedWords = learned,
            inProgressWords = 0,
            dueReviews = dueReview,
            totalStudyDays = studyDays,
            todayReviewedCount = 0,
            streakDays = streak,
            learnedByStage = byStage,
            learnedByDomain = emptyMap()
        )
        BadgeManager.calculateBadges(stats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
