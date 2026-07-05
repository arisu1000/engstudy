package com.wcjung.engstudy.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wcjung.engstudy.domain.model.EduWord

@Composable
fun EduWordCard(
    word: EduWord,
    isKnown: Boolean = false,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (word.variant1.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(${word.variant1}${if (word.variant2.isNotBlank()) ", ${word.variant2}" else ""})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            if (isKnown) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "이미 알아요",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isBookmarked) "즐겨찾기 해제" else "즐겨찾기",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                word.level.displayNameKo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                val uri = Uri.parse("https://en.dict.naver.com/#/search?query=${Uri.encode(word.word)}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }) {
                Text("네이버 사전", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = {
                val uri = Uri.parse("https://dictionary.cambridge.org/dictionary/english/${Uri.encode(word.word)}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }) {
                Text("Cambridge", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
