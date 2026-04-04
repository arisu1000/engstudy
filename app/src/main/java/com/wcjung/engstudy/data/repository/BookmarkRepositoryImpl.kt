package com.wcjung.engstudy.data.repository

import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.domain.model.Word
import com.wcjung.engstudy.domain.model.toDomain
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarkedWords(): Flow<List<Word>> =
        bookmarkDao.getBookmarkedWords().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun isBookmarked(wordId: Int): Flow<Boolean> =
        bookmarkDao.isBookmarked(wordId)

    override suspend fun toggleBookmark(wordId: Int) {
        bookmarkDao.toggleBookmarkAtomic(wordId)
    }

    override fun getBookmarkCount(): Flow<Int> =
        bookmarkDao.getBookmarkCount()
}
