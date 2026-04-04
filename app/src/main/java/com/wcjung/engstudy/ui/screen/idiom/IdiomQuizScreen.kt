package com.wcjung.engstudy.ui.screen.idiom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.ui.components.ComboEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdiomQuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: IdiomQuizViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val comboCount by viewModel.comboCount.collectAsState()
    val maxCombo by viewModel.maxCombo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (questions.isNotEmpty()) {
                        Text("${currentIndex + 1} / ${questions.size}")
                    } else {
                        Text("숙어/구동사 퀴즈")
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
                if (questions.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isFinished) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("퀴즈 완료!", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        val total = viewModel.getCorrectCount() + viewModel.getIncorrectCount()
                        val score = if (total > 0) viewModel.getCorrectCount() * 100 / total else 0
                        Text("점수: $score%", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("맞음: ${viewModel.getCorrectCount()} / 틀림: ${viewModel.getIncorrectCount()}")

                        if (maxCombo >= 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "최대 콤보: ${maxCombo}연속",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (viewModel.getIncorrectIdioms().isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("틀린 표현:", style = MaterialTheme.typography.titleSmall)
                            viewModel.getIncorrectIdioms().forEach { idiom ->
                                Text("${idiom.phrase} - ${idiom.meaning}")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("돌아가기")
                        }
                    }
                } else {
                    viewModel.currentQuestion?.let { question ->
                        // 뜻을 보여주고 맞는 표현을 고르기
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
                                    text = "다음 뜻에 해당하는 표현은?",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = question.idiom.meaning,
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )
                                if (question.idiom.exampleEn.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = question.idiom.exampleEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 보기
                        question.options.forEachIndexed { index, option ->
                            val isSelected = selectedAnswer == index
                            val isCorrect = index == question.correctIndex
                            val hasAnswered = selectedAnswer != null

                            val containerColor = when {
                                hasAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                hasAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }

                            OutlinedButton(
                                onClick = { viewModel.selectAnswer(index) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                enabled = !hasAnswered,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = containerColor
                                )
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        if (selectedAnswer != null) {
                            Spacer(modifier = Modifier.height(16.dp))
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
        }
    }
}
