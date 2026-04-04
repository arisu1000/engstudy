package com.wcjung.engstudy.ui.screen.edu

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.EduLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordList: (String?) -> Unit,
    onNavigateToFlashCard: (String?) -> Unit = {},
    onNavigateToQuiz: (String?) -> Unit = {},
    viewModel: EduHomeViewModel = hiltViewModel()
) {
    val totalCount by viewModel.totalCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("교육부 필수 영단어 3,000") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "2022 개정 교육과정",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "교육부 지정 기본어휘 목록",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "총 ${totalCount}개 단어",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "레벨 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 전체 보기
            item {
                LevelCard(
                    title = "전체 보기",
                    description = "초등 + 중고등 + 전문 전체 3,000단어",
                    count = totalCount,
                    onClick = { onNavigateToWordList(null) },
                    onFlashCard = { onNavigateToFlashCard(null) },
                    onQuiz = { onNavigateToQuiz(null) }
                )
            }

            // 레벨별 카드
            items(EduLevel.entries.toList()) { level ->
                val count by viewModel.levelCounts[level]!!.collectAsState()
                LevelCard(
                    title = level.displayNameKo,
                    description = when (level) {
                        EduLevel.ELEMENTARY -> "초등학교 권장 기본 어휘"
                        EduLevel.MIDDLE_HIGH -> "중학교·고등학교 공통 어휘"
                        EduLevel.PROFESSIONAL -> "선택과목/전문 어휘"
                    },
                    count = count,
                    onClick = { onNavigateToWordList(level.key) },
                    onFlashCard = { onNavigateToFlashCard(level.key) },
                    onQuiz = { onNavigateToQuiz(level.key) }
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    title: String,
    description: String,
    count: Int,
    onClick: () -> Unit,
    onFlashCard: () -> Unit,
    onQuiz: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    "${count}단어",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onFlashCard,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("플래시카드")
                }
                FilledTonalButton(
                    onClick = onQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("퀴즈")
                }
            }
        }
    }
}
