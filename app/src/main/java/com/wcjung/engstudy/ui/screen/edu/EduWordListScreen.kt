package com.wcjung.engstudy.ui.screen.edu

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wcjung.engstudy.domain.model.EduLevel
import com.wcjung.engstudy.ui.components.EduWordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduWordListScreen(
    onNavigateBack: () -> Unit,
    viewModel: EduWordListViewModel = hiltViewModel()
) {
    val words by viewModel.words.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val knownIds by viewModel.knownIds.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val hideKnown by viewModel.hideKnown.collectAsState()
    val levelName = viewModel.level?.let { EduLevel.fromKey(it).displayNameKo } ?: "전체"

    var isSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectMode) {
                        Text("${selectedIds.size}개 선택됨")
                    } else {
                        Text("교육부 - $levelName (${words.size}단어)")
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
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
                        IconButton(onClick = { viewModel.toggleHideKnown() }) {
                            Icon(
                                if (hideKnown) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (hideKnown) "아는 단어 표시" else "아는 단어 숨기기"
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
                            colors = ButtonDefaults.buttonColors(
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
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(words, key = { it.id }) { word ->
                    val isKnown = knownIds.contains(word.id)
                    val isBookmarked = bookmarkedIds.contains(word.id)

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
                                EduWordCard(
                                    word = word,
                                    isKnown = isKnown,
                                    isBookmarked = isBookmarked,
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
                            EduWordCard(
                                word = word,
                                isKnown = isKnown,
                                isBookmarked = isBookmarked,
                                onToggleBookmark = { viewModel.toggleBookmark(word.id) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
