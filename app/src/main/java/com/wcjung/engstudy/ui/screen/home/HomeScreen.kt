package com.wcjung.engstudy.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.ui.theme.StageAdvanced
import com.wcjung.engstudy.ui.theme.StageFoundation
import com.wcjung.engstudy.ui.theme.StageIntermediate
import com.wcjung.engstudy.ui.theme.StageNearNative
import com.wcjung.engstudy.ui.theme.StageProficient
import com.wcjung.engstudy.ui.theme.StageUpperIntermediate
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWordList: (String?, Int?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWordDetail: (Int) -> Unit,
    onNavigateToEdu: () -> Unit = {},
    onNavigateToPlacementTest: () -> Unit = {},
    onNavigateToDailyChallenge: () -> Unit = {},
    onNavigateToIdiom: () -> Unit = {},
    onNavigateToGrammar: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsState()
    val dueReviewCount by viewModel.dueReviewCount.collectAsState()
    val learnedWordCount by viewModel.learnedWordCount.collectAsState()
    val totalWordCount by viewModel.totalWordCount.collectAsState()
    val stageWordCounts by viewModel.stageWordCounts.collectAsState()
    val stageLearnedCounts by viewModel.stageLearnedCounts.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val todayLearnedCount by viewModel.todayLearnedCount.collectAsState()
    val hasCompletedPlacementTest by viewModel.hasCompletedPlacementTest.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("EngStudy") },
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                }
            }
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 레벨 테스트 안내 (미완료 시에만 표시)
            if (!hasCompletedPlacementTest) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "레벨 테스트를 받아보세요!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "나에게 맞는 학습 단계를 찾아드립니다",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Button(onClick = onNavigateToPlacementTest) {
                            Text("시작")
                        }
                    }
                }
            }

            // 오늘의 단어 — 홈에서 유일한 유색 히어로 카드
            wordOfTheDay?.let { word ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWordDetail(word.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "오늘의 단어",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "  ${word.pronunciation}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = word.meaning,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = word.exampleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 오늘 요약: 학습 목표 + 통계 + 챌린지 진입을 카드 1장으로 통합
            TodaySummaryCard(
                todayCount = todayLearnedCount,
                dailyGoal = dailyGoal,
                learnedWordCount = learnedWordCount,
                totalWordCount = totalWordCount,
                dueReviewCount = dueReviewCount,
                onChallengeClick = onNavigateToDailyChallenge
            )

            // 콘텐츠 바로가기: 동일 패턴 카드 3장을 타일 그리드로 압축
            Text(
                text = "콘텐츠",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContentTile(
                    emoji = "🎓",
                    title = "교육부\n3,000",
                    onClick = onNavigateToEdu,
                    modifier = Modifier.weight(1f)
                )
                ContentTile(
                    emoji = "💬",
                    title = "숙어\n구동사",
                    onClick = onNavigateToIdiom,
                    modifier = Modifier.weight(1f)
                )
                ContentTile(
                    emoji = "📖",
                    title = "문법\n예문",
                    onClick = onNavigateToGrammar,
                    modifier = Modifier.weight(1f)
                )
            }

            // 단계별 탐색: 카드 6장 대신 구분선 리스트를 담은 카드 1장.
            // 단어가 없는 단계는 표시하지 않는다 (탭해도 빈 목록만 나옴)
            val visibleStages = Stage.entries.filter { (stageWordCounts[it.level] ?: 0) > 0 }
            if (visibleStages.isNotEmpty()) {
                Text(
                    text = "단계별 단어",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    visibleStages.forEachIndexed { index, stage ->
                        StageProgressRow(
                            stage = stage,
                            totalCount = stageWordCounts[stage.level] ?: 0,
                            learnedCount = stageLearnedCounts[stage.level] ?: 0,
                            onClick = { onNavigateToWordList(null, stage.level) }
                        )
                        if (index < visibleStages.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** 오늘의 학습 목표·누적 통계·일일 챌린지 진입을 한 카드로 묶은 요약 */
@Composable
fun TodaySummaryCard(
    todayCount: Int,
    dailyGoal: Int,
    learnedWordCount: Int,
    totalWordCount: Int,
    dueReviewCount: Int,
    onChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val progress = if (dailyGoal > 0) (todayCount.toFloat() / dailyGoal).coerceAtMost(1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "dailyProgress")
    val isComplete = todayCount >= dailyGoal
    val accentColor = if (isComplete) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isComplete) "오늘의 학습 완료!" else "오늘의 학습",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$todayCount / $dailyGoal",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "학습 완료 ${numberFormat.format(learnedWordCount)}/${numberFormat.format(totalWordCount)} · 복습 예정 $dueReviewCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
            modifier = Modifier
                .clickable { onChallengeClick() }
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "오늘의 챌린지 🏆",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "매일 같은 단어로 가족과 겨루세요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "시작 >",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 교육부/숙어/문법 등 콘텐츠 섹션 진입 타일 */
@Composable
fun ContentTile(
    emoji: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** 단계별 진행 상황 행 — 카드 리스트 대신 구분선 리스트로 컴팩트하게 표시 */
@Composable
fun StageProgressRow(
    stage: Stage,
    totalCount: Int,
    learnedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val progress = if (totalCount > 0) learnedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "stageProgress")
    val stageColor = stage.themeColor()

    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(stageColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append(stage.displayNameKo)
                        if (stage.cefr != "-") append(" · ${stage.cefr}")
                        append(" · ${stage.description}")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${numberFormat.format(learnedCount)}/${numberFormat.format(totalCount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = stageColor,
                trackColor = stageColor.copy(alpha = 0.15f),
            )
        }
    }
}

/** Stage별 브랜드 컬러 매핑 (ui/theme/Color.kt에 정의된 Stage 전용 색상) */
private fun Stage.themeColor(): Color = when (this) {
    Stage.FOUNDATION -> StageFoundation
    Stage.INTERMEDIATE -> StageIntermediate
    Stage.UPPER_INTERMEDIATE -> StageUpperIntermediate
    Stage.ADVANCED -> StageAdvanced
    Stage.PROFICIENT -> StageProficient
    Stage.NEAR_NATIVE -> StageNearNative
}
