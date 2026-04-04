package com.wcjung.engstudy.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.Stage
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "레벨 테스트를 받아보세요!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "나에게 맞는 학습 단계를 찾아드립니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToPlacementTest) {
                            Text("테스트 시작")
                        }
                    }
                }
            }

            // 오늘의 단어
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

            // 오늘의 학습 목표
            DailyProgressCard(
                todayCount = todayLearnedCount,
                dailyGoal = dailyGoal
            )

            // 학습 요약 통계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "학습 완료",
                    value = "$learnedWordCount / $totalWordCount",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "복습 예정",
                    value = "$dueReviewCount",
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f)
                )
            }

            // 교육부 3000
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEdu() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "교육부 필수 영단어 3,000",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "2022 개정 교육과정 기본어휘",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        "바로가기 >",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // 단계별 탐색
            Text(
                text = "단계별 단어",
                style = MaterialTheme.typography.titleMedium
            )
            Stage.entries.forEach { stage ->
                StageProgressCard(
                    stage = stage,
                    totalCount = stageWordCounts[stage.level] ?: 0,
                    learnedCount = stageLearnedCounts[stage.level] ?: 0,
                    onClick = { onNavigateToWordList(null, stage.level) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StageProgressCard(
    stage: Stage,
    totalCount: Int,
    learnedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val progress = if (totalCount > 0) learnedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "stageProgress")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stage.displayNameKo,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${stage.cefr} · ${stage.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${numberFormat.format(learnedCount)}/${numberFormat.format(totalCount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun DailyProgressCard(
    todayCount: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (dailyGoal > 0) (todayCount.toFloat() / dailyGoal).coerceAtMost(1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "dailyProgress")
    val isComplete = todayCount >= dailyGoal

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isComplete) "오늘의 학습 완료!" else "오늘의 학습",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isComplete) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                Text(
                    text = "$todayCount / $dailyGoal",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isComplete) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = if (isComplete) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                },
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
