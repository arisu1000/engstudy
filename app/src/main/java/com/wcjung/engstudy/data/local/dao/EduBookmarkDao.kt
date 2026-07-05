package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wcjung.engstudy.data.local.entity.EduBookmarkEntity
import com.wcjung.engstudy.data.local.entity.EduWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EduBookmarkDao {

    @Query(
        """
        SELECT w.* FROM edu_words w
        INNER JOIN edu_bookmarks b ON w.id = b.edu_word_id
        ORDER BY b.created_at DESC
        """
    )
    fun getBookmarkedWords(): Flow<List<EduWordEntity>>

    @Query("SELECT edu_word_id FROM edu_bookmarks")
    fun getBookmarkedIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM edu_bookmarks WHERE edu_word_id = :eduWordId)")
    fun isBookmarked(eduWordId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmark(bookmark: EduBookmarkEntity)

    @Query("DELETE FROM edu_bookmarks WHERE edu_word_id = :eduWordId")
    suspend fun removeBookmark(eduWordId: Int)

    /**
     * 원자적 토글: read-then-write 경쟁 조건을 트랜잭션으로 방지한다 (BookmarkDao와 동일 패턴).
     */
    @Transaction
    suspend fun toggleBookmarkAtomic(eduWordId: Int) {
        val exists = isBookmarkedSync(eduWordId)
        if (exists) {
            removeBookmark(eduWordId)
        } else {
            addBookmark(EduBookmarkEntity(eduWordId = eduWordId))
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM edu_bookmarks WHERE edu_word_id = :eduWordId)")
    suspend fun isBookmarkedSync(eduWordId: Int): Boolean
}
