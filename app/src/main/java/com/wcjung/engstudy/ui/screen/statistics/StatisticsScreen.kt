package com.wcjung.engstudy.ui.screen.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.Badge
import com.wcjung.engstudy.domain.model.Stage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val totalWords by viewModel.totalWords.collectAsState()
    val learnedWords by viewModel.learnedWords.collectAsState()
    val inProgressWords by viewModel.inProgressWords.collectAsState()
    val dueReviews by viewModel.dueReviews.collectAsState()
    val totalStudyDays by viewModel.totalStudyDays.collectAsState()
    val learnedByStage by viewModel.learnedByStage.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val dailyStudyCounts by viewModel.dailyStudyCounts.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\uD559\uC2B5 \uD1B5\uACC4") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\uB4A4\uB85C")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportText = viewModel.generateReport()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "\uB9AC\uD3EC\uD2B8 \uACF5\uC720\uD558\uAE30")
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "\uB9AC\uD3EC\uD2B8 \uACF5\uC720")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 연속 학습일수 (streak)
            StreakCard(streakDays = streakDays)

            // 전체 요약
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("전체 진행률", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (totalWords > 0) learnedWords.toFloat() / totalWords else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$learnedWords / $totalWords 단어 학습 완료 (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 수치 카드들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatNumberCard("학습 중", "$inProgressWords", Modifier.weight(1f))
                StatNumberCard("복습 예정", "$dueReviews", Modifier.weight(1f))
                StatNumberCard("총 학습일", "${totalStudyDays}일", Modifier.weight(1f))
            }

            // 학습 캘린더
            StudyCalendar(dailyCounts = dailyStudyCounts)

            // 업적 뱃지
            Text("업��", style = MaterialTheme.typography.titleMedium)
            BadgeGrid(badges = badges)

            // 단계별 학습 현황
            Text("단계별 학습 현황", style = MaterialTheme.typography.titleMedium)

            Stage.entries.forEach { stage ->
                val learned = learnedByStage[stage.level] ?: 0
                StageProgressItem(
                    stageName = "${stage.displayNameKo} (${stage.cefr})",
                    learnedCount = learned
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** 연속 학습일수를 강조 표시하는 카드 */
@Composable
private fun StreakCard(streakDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "연속 학습",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${streakDays}일 연속 학습",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "매일 학습하면 연속 기록이 올라갑니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** 뱃지를 그리드 형태로 표시한다 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeGrid(badges: List<Badge>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badges.forEach { badge ->
            BadgeItem(badge = badge)
        }
    }
}

@Composable
private fun BadgeItem(badge: Badge) {
    val containerColor = if (badge.isEarned) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (badge.isEarned) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = badgeIcon(badge),
                contentDescription = badge.name,
                modifier = Modifier.size(28.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun badgeIcon(badge: Badge): ImageVector {
    return when (badge.icon) {
        "first_step" -> Icons.Filled.DirectionsWalk
        "streak" -> Icons.Filled.LocalFireDepartment
        "master" -> Icons.Filled.EmojiEvents
        "stage" -> Icons.Filled.School
        else -> if (badge.isEarned) Icons.Filled.Star else Icons.Filled.Lock
    }
}

@Composable
fun StatNumberCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StageProgressItem(stageName: String, learnedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stageName, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${learnedCount}개",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
