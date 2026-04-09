package com.wcjung.engstudy.ui.screen.wordlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.Domain
import com.wcjung.engstudy.domain.model.Stage
import com.wcjung.engstudy.ui.components.WordCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    onNavigateToWordDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WordListViewModel = hiltViewModel()
) {
    val words by viewModel.words.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val showExcluded by viewModel.showExcluded.collectAsState()

    var isSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    val lazyListState = rememberLazyListState()

    // 스크롤 끝 근처에 도달하면 추가 로드
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 5
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { viewModel.loadMore() }
    }

    val title = buildString {
        viewModel.domain?.let { append(Domain.fromKey(it).displayNameKo) }
        if (viewModel.domain != null && viewModel.stage != null) append(" · ")
        viewModel.stage?.let { append(Stage.fromLevel(it).displayNameKo) }
        if (isEmpty()) append("전체 단어")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectMode) {
                        Text("${selectedIds.size}개 선택됨")
                    } else {
                        Text(title)
                    }
                },
                navigationIcon = {
                    if (isSelectMode) {
                        IconButton(onClick = {
                            isSelectMode = false
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "선택 취소")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                },
                actions = {
                    if (isSelectMode) {
                        IconButton(onClick = {
                            selectedIds = if (selectedIds.size == words.size) {
                                emptySet()
                            } else {
                                words.map { it.id }.toSet()
                            }
                        }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "전체 선택")
                        }
                    } else {
                        // 제외 단어 표시 토글
                        IconButton(onClick = { viewModel.toggleShowExcluded() }) {
                            Icon(
                                if (showExcluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showExcluded) "제외 단어 숨기기" else "제외 단어 보기"
                            )
                        }
                        IconButton(onClick = { isSelectMode = true }) {
                            Icon(Icons.Default.Checklist, contentDescription = "선택 모드")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isSelectMode && selectedIds.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.markMultipleAsKnown(selectedIds.toList())
                                selectedIds = emptySet()
                                isSelectMode = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("이미 알아요")
                        }
                        Button(
                            onClick = {
                                viewModel.excludeMultiple(selectedIds.toList())
                                selectedIds = emptySet()
                                isSelectMode = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.RemoveCircle,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("완전 제외")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    if (isSelectMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(word.id),
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + word.id
                                    } else {
                                        selectedIds - word.id
                                    }
                                }
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                WordCard(
                                    word = word,
                                    isBookmarked = bookmarkedIds.contains(word.id),
                                    onTap = {
                                        selectedIds = if (selectedIds.contains(word.id)) {
                                            selectedIds - word.id
                                        } else {
                                            selectedIds + word.id
                                        }
                                    },
                                    onSpeak = { viewModel.ttsManager.speak(word.word) },
                                    onToggleBookmark = { viewModel.toggleBookmark(word.id) }
                                )
                            }
                        }
                    } else {
                        val dismissState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.markAsKnown(word.id)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart ->
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    label = "swipeBg"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "이미 알아요",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        ) {
                            WordCard(
                                word = word,
                                isBookmarked = bookmarkedIds.contains(word.id),
                                onTap = { onNavigateToWordDetail(word.id) },
                                onSpeak = { viewModel.ttsManager.speak(word.word) },
                                onToggleBookmark = { viewModel.toggleBookmark(word.id) }
                            )
                        }
                    }
                }

                // 추가 로드 중 인디케이터
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
