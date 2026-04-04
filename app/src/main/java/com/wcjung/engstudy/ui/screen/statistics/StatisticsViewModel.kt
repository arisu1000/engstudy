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
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val learningRepository: LearningRepository,
    private val userPreferences: UserPreferences
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
            .map { list -> list.associate { it.studyDate to it.count } }
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

    /**
     * 학습 리포트 텍스트를 생성한다.
     * 카카오톡, 메시지 등 메신저에서 보기 좋도록 이모지와 함께 포맷한다.
     */
    fun generateReport(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

        val todayReviewed = todayReviewedCount.value
        val learned = learnedWords.value
        val total = totalWords.value
        val streak = streakDays.value
        val earnedBadges = badges.value.count { it.isEarned }
        val stageMap = learnedByStage.value

        // 오늘의 학습 정답률은 단순 추정: 학습한 단어가 있으면 표시
        return buildString {
            appendLine("\uD83D\uDCDA EngStudy \uD559\uC2B5 \uB9AC\uD3EC\uD2B8")
            appendLine("\uD83D\uDCC5 ${today.format(formatter)} ($dayOfWeek)")
            appendLine()
            appendLine("\uD83D\uDCCA \uC624\uB298\uC758 \uD559\uC2B5")
            appendLine("\u2022 \uBCF5\uC2B5\uD55C \uB2E8\uC5B4: ${todayReviewed}\uAC1C")
            appendLine()
            if (streak > 0) {
                appendLine("\uD83D\uDD25 \uC5F0\uC18D \uD559\uC2B5: ${streak}\uC77C\uC9F8")
                appendLine()
            }
            appendLine("\uD83D\uDCC8 \uC804\uCCB4 \uC9C4\uB3C4")
            Stage.entries.forEach { stage ->
                val stageLearned = stageMap[stage.level] ?: 0
                val stageTotal = stageWordCounts.value[stage.level] ?: 0
                if (stageTotal > 0) {
                    val pct = if (stageTotal > 0) stageLearned * 100 / stageTotal else 0
                    val check = if (stageLearned >= stageTotal) " \u2705" else " (${pct}%)"
                    appendLine("\u2022 ${stage.displayNameKo}: ${numberFormat.format(stageLearned)}/${numberFormat.format(stageTotal)}$check")
                }
            }
            appendLine()
            if (earnedBadges > 0) {
                appendLine("\uD83C\uDFC6 \uD68D\uB4DD\uD55C \uBC43\uC9C0: ${earnedBadges}\uAC1C")
                appendLine()
            }
            append("#EngStudy #\uC601\uC5B4\uB2E8\uC5B4 #\uB9E4\uC77C\uD559\uC2B5")
        }
    }

    /** 단계별 총 단어 수 (리포트 생성용) */
    val stageWordCounts: StateFlow<Map<Int, Int>> = combine(
        Stage.entries.map { stage -> wordRepository.getWordCountByStage(stage.level) }
    ) { counts ->
        Stage.entries.mapIndexed { index, stage -> stage.level to counts[index] }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** 오늘 복습한 단어 수 */
    val todayReviewedCount: StateFlow<Int> = run {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        learningRepository.getReviewedCountForDay(dayStart, dayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }
}
