package com.wcjung.engstudy.ui.screen.worddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val word by viewModel.word.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val isMarkedAsKnown by viewModel.isMarkedAsKnown.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word?.word ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "즐겨찾기 해제" else "즐겨찾기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        word?.let { w ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 단어 헤더
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = w.word,
                                style = MaterialTheme.typography.headlineLarge
                            )
                            IconButton(onClick = { viewModel.ttsManager.speak(w.word) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "발음 듣기")
                            }
                        }
                        Text(
                            text = w.pronunciation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = w.partOfSpeech,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = w.meaning,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 예문
                DetailSection(title = "예문") {
                    Text(text = w.exampleEn, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.ttsManager.speak(w.exampleEn) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "예문 듣기",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = w.exampleKo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 유의어
                if (w.synonyms.isNotEmpty()) {
                    DetailSection(title = "유의어") {
                        Text(
                            text = w.synonyms.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 반의어
                if (w.antonyms.isNotEmpty()) {
                    DetailSection(title = "반의어") {
                        Text(
                            text = w.antonyms.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 추가 정보
                w.notes?.let { notes ->
                    DetailSection(title = "참고") {
                        Text(text = notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // "이미 알아요" 버튼
                Button(
                    onClick = { viewModel.markAsKnown() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isMarkedAsKnown,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(if (isMarkedAsKnown) "학습 완료됨" else "이미 알아요")
                }

                // 메타 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(label = "분야", value = w.domain.displayNameKo)
                    InfoChip(label = "단계", value = w.stage.displayNameKo)
                    InfoChip(label = "빈도", value = "#${w.frequencyRank}")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
