package com.wcjung.engstudy.ui.screen.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * GitHub 기여 그래프 스타일의 학습 활동 캘린더.
 * 최근 3개월을 주 단위 열(column)로 표시하며,
 * 각 셀의 색상으로 해당 일자의 학습 단어 수를 나타낸다.
 *
 * @param dailyCounts 날짜 문자열("yyyy-MM-dd") -> 학습 단어 수 맵
 */
@Composable
fun StudyCalendar(
    dailyCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = today.minusMonths(3).with(DayOfWeek.MONDAY)
    val totalDays = startDate.until(today).days + 1

    // 주 단위로 그룹화
    val weeks = mutableListOf<List<LocalDate?>>()
    var currentDate = startDate
    var currentWeek = mutableListOf<LocalDate?>()

    // 첫 주의 시작 요일 앞에 null 삽입
    val startDayOfWeek = startDate.dayOfWeek.value // 1=Mon, 7=Sun
    if (startDayOfWeek > 1) {
        for (i in 1 until startDayOfWeek) {
            currentWeek.add(null)
        }
    }

    // 모든 날짜를 주 단위로 할당
    while (!currentDate.isAfter(today)) {
        currentWeek.add(currentDate)
        if (currentDate.dayOfWeek == DayOfWeek.SUNDAY) {
            weeks.add(currentWeek)
            currentWeek = mutableListOf()
        }
        currentDate = currentDate.plusDays(1)
    }
    if (currentWeek.isNotEmpty()) {
        // 마지막 주의 남은 칸을 null로 채움
        while (currentWeek.size < 7) {
            currentWeek.add(null)
        }
        weeks.add(currentWeek)
    }

    // 월 레이블 계산
    val monthLabels = mutableListOf<Pair<Int, String>>()
    var lastMonth = -1
    weeks.forEachIndexed { index, week ->
        val firstDateInWeek = week.firstOrNull { it != null }
        if (firstDateInWeek != null && firstDateInWeek.monthValue != lastMonth) {
            lastMonth = firstDateInWeek.monthValue
            monthLabels.add(
                index to firstDateInWeek.month.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            )
        }
    }

    // 색상
    val noStudyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val lightGreen = Color(0xFF9BE9A8)
    val mediumGreen = Color(0xFF40C463)
    val darkGreen = Color(0xFF216E39)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "학습 캘린더",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 월 레이블 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // 요일 레이블 공간 확보
                Box(modifier = Modifier.size(width = 20.dp, height = 12.dp))

                monthLabels.forEach { (weekIndex, label) ->
                    val offsetDp = (weekIndex * 14).dp
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = offsetDp.coerceAtMost(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 캘린더 그리드: 행 = 요일 (월~일), 열 = 주
            val dayLabels = listOf("월", "", "수", "", "금", "", "")
            Row(modifier = Modifier.fillMaxWidth()) {
                // 요일 레이블
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.size(width = 20.dp, height = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 주별 열
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            week.forEach { date ->
                                val count = if (date != null) {
                                    dailyCounts[date.toString()] ?: 0
                                } else {
                                    -1 // null 날짜
                                }

                                val cellColor = when {
                                    count < 0 -> Color.Transparent
                                    count == 0 -> noStudyColor
                                    count in 1..10 -> lightGreen
                                    count in 11..30 -> mediumGreen
                                    else -> darkGreen
                                }

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(cellColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 범례
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "적음",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(noStudyColor, lightGreen, mediumGreen, darkGreen).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Text(
                    text = "많음",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
