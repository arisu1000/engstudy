package com.wcjung.engstudy.ui.screen.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.ui.components.WordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onNavigateToWordDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val words by viewModel.bookmarkedWords.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("즐겨찾기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (words.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = viewModel.getShareText()
                            if (text.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "EngStudy 즐겨찾기 단어장")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "단어장 공유"))
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "공유")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "즐겨찾기한 단어가 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    WordCard(
                        word = word,
                        isBookmarked = true,
                        onTap = { onNavigateToWordDetail(word.id) },
                        onSpeak = { viewModel.ttsManager.speak(word.word) },
                        onToggleBookmark = { viewModel.toggleBookmark(word.id) }
                    )
                }
            }
        }
    }
}
