package com.wcjung.engstudy.ui.screen.challenge

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.ui.components.ComboEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyChallengeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val comboCount by viewModel.comboCount.collectAsState()
    val maxCombo by viewModel.maxCombo.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d"))
                    Text("\uD83C\uDFC6 \uC624\uB298\uC758 \uCC4C\uB9B0\uC9C0 ($today)")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\uB4A4\uB85C")
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
                    ChallengeResultContent(
                        correctCount = viewModel.getCorrectCount(),
                        totalCount = questions.size,
                        elapsedSeconds = viewModel.getElapsedSeconds(),
                        maxCombo = maxCombo,
                        onShare = {
                            val shareText = viewModel.generateShareText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "\uACB0\uACFC \uACF5\uC720\uD558\uAE30")
                            )
                        },
                        onNavigateBack = onNavigateBack
                    )
                } else {
                    viewModel.currentQuestion?.let { question ->
                        // 문제 번호
                        Text(
                            text = "${currentIndex + 1} / ${questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    text = "\uB2E4\uC74C \uB73B\uC5D0 \uD574\uB2F9\uD558\uB294 \uC601\uC5B4 \uB2E8\uC5B4\uB294?",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = question.word.meaning,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "(${question.word.partOfSpeech})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
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
                                Text("\uB2E4\uC74C")
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

@Composable
private fun ChallengeResultContent(
    correctCount: Int,
    totalCount: Int,
    elapsedSeconds: Int,
    maxCombo: Int,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83C\uDFC6 \uCC4C\uB9B0\uC9C0 \uC644\uB8CC!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$correctCount / $totalCount",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\uC815\uB2F5",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "\u23F1 ${elapsedSeconds}\uCD08",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (maxCombo >= 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\uD83D\uDD25 \uCD5C\uB300 \uCF64\uBCF4: ${maxCombo}\uC5F0\uC18D",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("\uACB0\uACFC \uACF5\uC720\uD558\uAE30 \uD83D\uDCE4")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("\uB3CC\uC544\uAC00\uAE30")
        }
    }
}
