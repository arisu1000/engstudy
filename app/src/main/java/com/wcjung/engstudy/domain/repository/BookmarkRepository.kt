package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarkedWords(): Flow<List<Word>>
    fun isBookmarked(wordId: Int): Flow<Boolean>
    suspend fun toggleBookmark(wordId: Int)
    fun getBookmarkCount(): Flow<Int>
}
