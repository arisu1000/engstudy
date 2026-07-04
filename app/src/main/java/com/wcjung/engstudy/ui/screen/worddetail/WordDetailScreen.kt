package com.wcjung.engstudy.ui.screen.worddetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val meanings by viewModel.meanings.collectAsState()
    val examples by viewModel.examples.collectAsState()
    val userMeanings by viewModel.userMeanings.collectAsState()
    val hasPrimaryOverride = userMeanings.any { it.isPrimary }
    val customMeanings = userMeanings.filter { !it.isPrimary }

    // 대표 뜻 수정 다이얼로그
    var showEditPrimaryDialog by remember { mutableStateOf(false) }
    if (showEditPrimaryDialog) {
        MeaningInputDialog(
            title = "대표 뜻 수정",
            description = "단어 목록·퀴즈·복습에도 반영됩니다.",
            initialValue = word?.meaning ?: "",
            confirmLabel = "저장",
            onConfirm = { text ->
                viewModel.editPrimaryMeaning(text)
                showEditPrimaryDialog = false
            },
            onDismiss = { showEditPrimaryDialog = false }
        )
    }

    // 내 의미 추가 다이얼로그
    var showAddMeaningDialog by remember { mutableStateOf(false) }
    if (showAddMeaningDialog) {
        MeaningInputDialog(
            title = "내 의미 추가",
            description = "이 단어에 나만의 의미를 추가합니다.",
            initialValue = "",
            confirmLabel = "추가",
            onConfirm = { text ->
                viewModel.addUserMeaning(text)
                showAddMeaningDialog = false
            },
            onDismiss = { showAddMeaningDialog = false }
        )
    }

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
                        // 대표 뜻 (사용자 수정 가능 — 연필 아이콘)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = w.meaning,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showEditPrimaryDialog = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "대표 뜻 수정",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (hasPrimaryOverride) {
                            Text(
                                text = "내가 수정한 뜻 · 기본값으로 되돌리기",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.clickable { viewModel.resetPrimaryMeaning() }
                            )
                        }
                        // 다중 의미: word_meanings 테이블 (빈도순)
                        if (meanings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            meanings.forEachIndexed { index, wm ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = wm.pos,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = wm.meaning,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        // 내가 추가한 의미
                        customMeanings.forEach { um ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "내 의미",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = um.meaning,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.deleteUserMeaning(um.id) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "내 의미 삭제",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        TextButton(onClick = { showAddMeaningDialog = true }) {
                            Text("+ 내 의미 추가")
                        }
                    }
                }

                // 예문: word_examples 테이블 우선, 없으면 example_en/example_ko 폴백
                val displayExamples = if (examples.isNotEmpty()) {
                    examples.map { it.sentenceEn to it.sentenceKo }
                } else if (w.exampleEn.isNotBlank()) {
                    listOf(w.exampleEn to w.exampleKo)
                } else emptyList()

                if (displayExamples.isNotEmpty()) {
                    DetailSection(title = "예문") {
                        displayExamples.forEachIndexed { index, (en, ko) ->
                            if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = en,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.ttsManager.speak(en) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "예문 듣기",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = ko,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

                // 외부 사전 링크
                val context = LocalContext.current
                DetailSection(title = "사전에서 찾기") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse("https://en.dict.naver.com/#/search?query=${Uri.encode(w.word)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("네이버 사전")
                        }
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse("https://dictionary.cambridge.org/dictionary/english/${Uri.encode(w.word)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cambridge")
                        }
                    }
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

/** 의미 입력 다이얼로그 (대표 뜻 수정 / 내 의미 추가 공용). 빈 입력은 확인 버튼 비활성화. */
@Composable
private fun MeaningInputDialog(
    title: String,
    description: String,
    initialValue: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
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
