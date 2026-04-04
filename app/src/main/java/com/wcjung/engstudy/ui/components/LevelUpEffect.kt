package com.wcjung.engstudy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Stage 학습 완료 시 표시되는 전체 화면 축하 이펙트.
 *
 * 여러 색상의 원(파티클)이 화면 위에서 떨어지는 간단한 confetti 애니메이션과 함께
 * "Stage N 완료!" 메시지를 표시한다. 3초 후 자동으로 사라지거나 탭하면 즉시 닫힌다.
 */
@Composable
fun LevelUpEffect(
    stageName: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var show by remember(visible) { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            show = true
            delay(3000)
            show = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = show,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    show = false
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            // 파티클 애니메이션
            ConfettiParticles()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83C\uDF89",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$stageName \uC644\uB8CC!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\uCD95\uD558\uD569\uB2C8\uB2E4! \uB2E4\uC74C \uB2E8\uACC4\uB85C \uB098\uC544\uAC00\uC138\uC694",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ConfettiParticles() {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFF6C5CE7),
        Color(0xFFFD79A8)
    )

    val particles = remember {
        List(20) {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f,
                size = Random.nextFloat() * 12f + 6f,
                color = colors[Random.nextInt(colors.size)],
                speed = Random.nextFloat() * 0.3f + 0.2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yPos = (particle.startY + progress * particle.speed) % 1.1f
            drawCircle(
                color = particle.color.copy(alpha = (1f - yPos).coerceIn(0.2f, 0.8f)),
                radius = particle.size,
                center = Offset(
                    x = particle.x * size.width,
                    y = yPos * size.height
                )
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val size: Float,
    val color: Color,
    val speed: Float
)
