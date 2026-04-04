package com.wcjung.engstudy.ui.screen.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashCardScreen(
    onNavigateBack: () -> Unit,
    viewModel: FlashCardViewModel = hiltViewModel()
) {
    val words by viewModel.words.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (words.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { if (words.isNotEmpty()) (currentIndex + 1).toFloat() / words.size else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isFinished) {
                // 결과 화면
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("학습 완료!", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("맞음: ${viewModel.getCorrectCount()}", style = MaterialTheme.typography.bodyLarge)
                    Text("틀림: ${viewModel.getIncorrectCount()}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onNavigateBack) {
                        Text("돌아가기")
                    }
                }
            } else {
                viewModel.currentWord?.let { word ->
                    val rotation by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(400),
                        label = "cardFlip"
                    )

                    // 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable { viewModel.flip() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // 앞면: 영어 단어
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.displayLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = word.pronunciation,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    IconButton(onClick = { viewModel.ttsManager.speak(word.word) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "발음 듣기")
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "탭하여 뒤집기",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                // 뒷면: 한국어 뜻
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .graphicsLayer { rotationY = 180f }
                                ) {
                                    Text(
                                        text = word.meaning,
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = word.partOfSpeech,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = word.exampleEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = word.exampleKo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "이미 알아요" 버튼 (앞면에서도 표시)
                    if (!isFlipped) {
                        TextButton(
                            onClick = { viewModel.markAsKnown() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("이미 알아요")
                        }
                    }

                    // 평가 버튼 (뒷면일 때만 표시)
                    if (isFlipped) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rate(SimpleRating.AGAIN) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) { Text("다시") }
                            OutlinedButton(
                                onClick = { viewModel.rate(SimpleRating.HARD) },
                                modifier = Modifier.weight(1f)
                            ) { Text("어려움") }
                            Button(
                                onClick = { viewModel.rate(SimpleRating.GOOD) },
                                modifier = Modifier.weight(1f)
                            ) { Text("보통") }
                            Button(
                                onClick = { viewModel.rate(SimpleRating.EASY) },
                                modifier = Modifier.weight(1f)
                            ) { Text("쉬움") }
                        }
                        TextButton(
                            onClick = { viewModel.markAsKnown() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("이미 알아요")
                        }
                    }
                }
            }
        }
    }
}
