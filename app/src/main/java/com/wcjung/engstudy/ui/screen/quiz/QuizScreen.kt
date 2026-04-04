package com.wcjung.engstudy.ui.screen.quiz

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
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
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
                    Text("\uD034\uC988 \uC644\uB8CC!", style = MaterialTheme.typography.headlineMedium)
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
            } else {
                viewModel.currentQuestion?.let { question ->
                    // 문제
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
                                text = if (question.isEnToKo) "다음 영어 단어의 뜻은?" else "다음 뜻에 해당하는 영어 단어는?",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (question.isEnToKo) question.word.word else question.word.meaning,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            if (question.isEnToKo) {
                                Text(
                                    text = question.word.pronunciation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 선택지
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
        } // Box
    }
}
