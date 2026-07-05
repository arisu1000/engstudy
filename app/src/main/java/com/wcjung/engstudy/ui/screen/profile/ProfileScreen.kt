package com.wcjung.engstudy.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToStatistics: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWrongAnswers: () -> Unit = {},
    onNavigateToExcludedWords: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("프로필") })

        Column(modifier = Modifier.padding(16.dp)) {
            AppIntroCard()
            ProfileMenuItem(
                icon = Icons.Default.BarChart,
                title = "학습 통계",
                subtitle = "학습 진행 상황을 확인합니다",
                onClick = onNavigateToStatistics
            )
            ProfileMenuItem(
                icon = Icons.Default.Bookmark,
                title = "즐겨찾기",
                subtitle = "저장한 단어를 확인합니다",
                onClick = onNavigateToBookmarks
            )
            ProfileMenuItem(
                icon = Icons.Default.EditNote,
                title = "오답 노트",
                subtitle = "퀴즈에서 틀린 단어를 확인합니다",
                onClick = onNavigateToWrongAnswers
            )
            ProfileMenuItem(
                icon = Icons.Default.RemoveCircle,
                title = "제외된 단어",
                subtitle = "학습에서 완전히 제외한 단어를 관리합니다",
                onClick = onNavigateToExcludedWords
            )
            ProfileMenuItem(
                icon = Icons.Default.Search,
                title = "검색",
                subtitle = "영어 또는 한국어로 단어를 검색합니다",
                onClick = onNavigateToSearch
            )
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "설정",
                subtitle = "앱 설정을 변경합니다",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun AppIntroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EngStudy, 제대로 만든 단어장",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "21,000단어, 오프라인으로 제대로 끝내는 영어 단어장",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppIntroFeature(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                text = "단어 12,068개 · 교육부 필수 3,000개 · 숙어 1,092개 · 예문 5,108개, 인터넷 없이 학습"
            )
            AppIntroFeature(
                icon = Icons.Default.Psychology,
                text = "실제 자주 쓰는 단어 순 Stage 1~6 + SM-2 간격반복으로 최적의 복습 타이밍 안내"
            )
            AppIntroFeature(
                icon = Icons.Default.VerifiedUser,
                text = "교육부 검수 데이터와 교차 검증하고 AI로 다듬은, 믿을 수 있는 뜻풀이"
            )
            AppIntroFeature(
                icon = Icons.Default.Celebration,
                text = "콤보·배지·스트릭과 일일 챌린지로 가족·친구와 즐겁게 경쟁"
            )
        }
    }
}

@Composable
private fun AppIntroFeature(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
