package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wcjung.engstudy.data.local.entity.BookmarkEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN bookmarks b ON w.id = b.word_id
        ORDER BY b.created_at DESC
        """
    )
    fun getBookmarkedWords(): Flow<List<WordEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE word_id = :wordId)")
    fun isBookmarked(wordId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE word_id = :wordId")
    suspend fun removeBookmark(wordId: Int)

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    /**
     * 원자적 토글: read-then-write 경쟁 조건을 트랜잭션으로 방지한다.
     * INSERT OR IGNORE로 중복 삽입을 안전하게 무시하고,
     * 이미 존재하면 삭제한다.
     */
    @Transaction
    suspend fun toggleBookmarkAtomic(wordId: Int) {
        val exists = isBookmarkedSync(wordId)
        if (exists) {
            removeBookmark(wordId)
        } else {
            addBookmark(BookmarkEntity(wordId = wordId))
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE word_id = :wordId)")
    suspend fun isBookmarkedSync(wordId: Int): Boolean
}
