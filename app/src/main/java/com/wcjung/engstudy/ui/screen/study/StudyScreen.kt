package com.wcjung.engstudy.ui.screen.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wcjung.engstudy.domain.model.AgeGroup
import com.wcjung.engstudy.domain.model.Domain
import com.wcjung.engstudy.ui.components.DomainChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudyScreen(
    onStartFlashCard: (String?, String?) -> Unit,
    onStartQuiz: (String?, String?) -> Unit,
    onStartSpellingQuiz: (String?, String?) -> Unit,
    onNavigateToWordList: (String?, String?) -> Unit
) {
    var selectedDomain by remember { mutableStateOf<Domain?>(null) }
    var selectedAgeGroup by remember { mutableStateOf<AgeGroup?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("학습") })

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 분야 선택
            Text("분야 선택", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Domain.entries.forEach { domain ->
                    DomainChip(
                        domain = domain,
                        selected = selectedDomain == domain,
                        onClick = {
                            selectedDomain = if (selectedDomain == domain) null else domain
                        }
                    )
                }
            }

            // 수준 선택
            Text("수준 선택", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgeGroup.entries.forEach { ageGroup ->
                    androidx.compose.material3.FilterChip(
                        selected = selectedAgeGroup == ageGroup,
                        onClick = {
                            selectedAgeGroup = if (selectedAgeGroup == ageGroup) null else ageGroup
                        },
                        label = { Text(ageGroup.displayNameKo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 학습 모드 선택
            Text("학습 시작", style = MaterialTheme.typography.titleSmall)

            StudyModeCard(
                title = "플래시카드",
                description = "카드를 뒤집으며 단어를 학습합니다",
                icon = Icons.Default.Style,
                onClick = { onStartFlashCard(selectedDomain?.key, selectedAgeGroup?.key) }
            )
            StudyModeCard(
                title = "퀴즈",
                description = "4지선다 문제를 풀며 단어를 확인합니다",
                icon = Icons.Default.Quiz,
                onClick = { onStartQuiz(selectedDomain?.key, selectedAgeGroup?.key) }
            )
            StudyModeCard(
                title = "스펠링",
                description = "뜻을 보고 영어 단어를 직접 입력합니다",
                icon = Icons.Default.Keyboard,
                onClick = { onStartSpellingQuiz(selectedDomain?.key, selectedAgeGroup?.key) }
            )
            StudyModeCard(
                title = "단어 목록",
                description = "선택한 분야/수준의 단어를 탐색합니다",
                icon = Icons.Default.LibraryBooks,
                onClick = { onNavigateToWordList(selectedDomain?.key, selectedAgeGroup?.key) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StudyModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
