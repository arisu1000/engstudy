package com.wcjung.engstudy.ui.screen.spelling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.ui.components.ComboEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellingQuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpellingQuizViewModel = hiltViewModel()
) {
    val words by viewModel.words.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val answerState by viewModel.answerState.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val comboCount by viewModel.comboCount.collectAsState()
    val maxCombo by viewModel.maxCombo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (words.isNotEmpty()) {
                        Text("${currentIndex + 1} / ${words.size}")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (words.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / words.size },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isFinished) {
                FinishedContent(viewModel, maxCombo, onNavigateBack)
            } else {
                viewModel.currentWord?.let { word ->
                    // 한국어 뜻 표시
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "다음 뜻에 해당하는 영어 단어를 입력하세요",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = word.meaning,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "(${word.partOfSpeech})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 입력 필드
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { viewModel.updateInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("영어 단어 입력") },
                        singleLine = true,
                        enabled = answerState is AnswerState.Unanswered,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.submitAnswer() }
                        ),
                        isError = answerState is AnswerState.Incorrect
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 결과 표시
                    when (val state = answerState) {
                        is AnswerState.Correct -> {
                            Text(
                                text = "정답입니다!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is AnswerState.Incorrect -> {
                            Text(
                                text = "오답! 정답: ${state.correctAnswer}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is AnswerState.Unanswered -> {}
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (answerState is AnswerState.Unanswered) {
                        Button(
                            onClick = { viewModel.submitAnswer() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = userInput.isNotBlank()
                        ) {
                            Text("확인")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("다음")
                        }
                    }
                }
            }
        }

        // 콤보 이펙트 오버레이
        ComboEffect(
            comboCount = comboCount,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        } // Box
    }
}

@Composable
private fun FinishedContent(
    viewModel: SpellingQuizViewModel,
    maxCombo: Int,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uC2A4\uD3A0\uB9C1 \uD034\uC988 \uC644\uB8CC!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        val total = viewModel.getCorrectCount() + viewModel.getIncorrectCount()
        val score = if (total > 0) viewModel.getCorrectCount() * 100 / total else 0
        Text("\uC810\uC218: $score%", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("\uB9DE\uC74C: ${viewModel.getCorrectCount()} / \uD2C0\uB9BC: ${viewModel.getIncorrectCount()}")

        if (maxCombo >= 3) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\uD83D\uDD25 \uCD5C\uB300 \uCF64\uBCF4: ${maxCombo}\uC5F0\uC18D",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (viewModel.getIncorrectWords().isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("\uD2C0\uB9B0 \uB2E8\uC5B4:", style = MaterialTheme.typography.titleSmall)
            viewModel.getIncorrectWords().forEach { word ->
                Text("${word.word} - ${word.meaning}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("\uB3CC\uC544\uAC00\uAE30")
        }
    }
}
