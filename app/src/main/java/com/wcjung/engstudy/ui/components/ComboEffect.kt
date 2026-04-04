package com.wcjung.engstudy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 연속 정답(콤보) 달성 시 표시되는 애니메이션 이펙트.
 *
 * - 3 콤보: 작은 바운스 + 칭찬 메시지
 * - 5 콤보: 더 큰 이펙트 + 색상 변경
 * - 10 콤보: 최대 크기 이펙트
 *
 * 콤보 수가 3 미만이면 아무것도 표시하지 않는다.
 */
@Composable
fun ComboEffect(
    comboCount: Int,
    modifier: Modifier = Modifier
) {
    // 콤보 3 이상일 때만 표시
    var showEffect by remember { mutableStateOf(false) }
    var lastShownCombo by remember { mutableStateOf(0) }

    // 새 콤보 달성 시에만 이펙트를 트리거한다
    LaunchedEffect(comboCount) {
        if (comboCount >= 3 && comboCount != lastShownCombo) {
            lastShownCombo = comboCount
            showEffect = true
            delay(1500)
            showEffect = false
        } else if (comboCount < 3) {
            showEffect = false
            lastShownCombo = 0
        }
    }

    val (text, color, scale) = when {
        comboCount >= 10 -> Triple(
            "${comboCount}\uC5F0\uC18D! \uD83D\uDCA5 AMAZING!",
            Color(0xFFFF6B00),
            1.6f
        )
        comboCount >= 5 -> Triple(
            "${comboCount}\uC5F0\uC18D! \uD83D\uDD25",
            Color(0xFFFF9800),
            1.3f
        )
        comboCount >= 3 -> Triple(
            "${comboCount}\uC5F0\uC18D! \uD83D\uDC4D",
            MaterialTheme.colorScheme.primary,
            1.1f
        )
        else -> Triple("", Color.Unspecified, 1f)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (showEffect) scale else 0.8f,
        animationSpec = tween(durationMillis = 300),
        label = "comboScale"
    )

    AnimatedVisibility(
        visible = showEffect && comboCount >= 3,
        modifier = modifier.padding(top = 80.dp),
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        exit = scaleOut(targetScale = 1.5f) + fadeOut()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.scale(animatedScale)
            )
        }
    }
}
