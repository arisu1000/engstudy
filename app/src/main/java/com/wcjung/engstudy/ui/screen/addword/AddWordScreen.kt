package com.wcjung.engstudy.ui.screen.addword

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.Stage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    onNavigateBack: () -> Unit,
    onSaved: (Int) -> Unit,
    onNavigateToWordDetail: (Int) -> Unit,
    viewModel: AddWordViewModel = hiltViewModel()
) {
    val word by viewModel.word.collectAsState()
    val meaning by viewModel.meaning.collectAsState()
    val pronunciation by viewModel.pronunciation.collectAsState()
    val exampleEn by viewModel.exampleEn.collectAsState()
    val exampleKo by viewModel.exampleKo.collectAsState()
    val stageLevel by viewModel.stageLevel.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val duplicateWordId by viewModel.duplicateWordId.collectAsState()
    val savedWordId by viewModel.savedWordId.collectAsState()

    LaunchedEffect(savedWordId) {
        savedWordId?.let { onSaved(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("단어 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = word,
                onValueChange = { viewModel.updateWord(it) },
                label = { Text("단어 (영어) *") },
                singleLine = true,
                isError = errorMessage != null,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                duplicateWordId?.let { existingId ->
                    TextButton(onClick = { onNavigateToWordDetail(existingId) }) {
                        Text("기존 단어 보기")
                    }
                }
            }

            // 뜻을 찾아볼 수 있게 외부 사전 링크 제공
            if (word.isNotBlank()) {
                val context = LocalContext.current
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val uri = Uri.parse(
                            "https://en.dict.naver.com/#/search?query=${Uri.encode(word.trim())}"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) { Text("네이버 사전") }
                    TextButton(onClick = {
                        val uri = Uri.parse(
                            "https://dictionary.cambridge.org/dictionary/english/${Uri.encode(word.trim())}"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) { Text("Cambridge") }
                }
            }

            OutlinedTextField(
                value = meaning,
                onValueChange = { viewModel.updateMeaning(it) },
                label = { Text("뜻 (한국어) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pronunciation,
                onValueChange = { viewModel.updatePronunciation(it) },
                label = { Text("발음 (선택)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = exampleEn,
                onValueChange = { viewModel.updateExampleEn(it) },
                label = { Text("예문 영어 (선택)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = exampleKo,
                onValueChange = { viewModel.updateExampleKo(it) },
                label = { Text("예문 한국어 (선택)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "학습 단계",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Stage.entries.forEach { stage ->
                    FilterChip(
                        selected = stageLevel == stage.level,
                        onClick = { viewModel.updateStageLevel(stage.level) },
                        label = { Text(stage.displayNameKo) }
                    )
                }
            }

            Button(
                onClick = { viewModel.save() },
                enabled = word.isNotBlank() && meaning.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("단어장에 추가")
            }

            Text(
                text = "추가한 단어는 단어 목록·검색·퀴즈·복습에 모두 포함됩니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
