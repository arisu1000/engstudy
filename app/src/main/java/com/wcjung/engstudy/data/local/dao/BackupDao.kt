package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.wcjung.engstudy.data.local.entity.BookmarkEntity
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity

/**
 * 백업/복원 전용 DAO — 사용자 데이터 4개 테이블의 전체 덤프와 원자적 교체.
 * 콘텐츠 테이블(words 등)은 앱에 내장되므로 백업 대상이 아니다.
 */
@Dao
interface BackupDao {

    @Query("SELECT * FROM learning_progress")
    suspend fun getAllLearningProgress(): List<LearningProgressEntity>

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM wrong_answers")
    suspend fun getAllWrongAnswers(): List<WrongAnswerEntity>

    @Query("SELECT * FROM known_items")
    suspend fun getAllKnownItems(): List<KnownItemEntity>

    /** FK 검증용 — 백업 파일의 word_id가 현재 DB에 없는 행은 복원에서 걸러낸다. */
    @Query("SELECT id FROM words")
    suspend fun getAllWordIds(): List<Int>

    @Query("DELETE FROM learning_progress")
    suspend fun clearLearningProgress()

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks()

    @Query("DELETE FROM wrong_answers")
    suspend fun clearWrongAnswers()

    @Query("DELETE FROM known_items")
    suspend fun clearKnownItems()

    @Insert
    suspend fun insertLearningProgress(rows: List<LearningProgressEntity>)

    @Insert
    suspend fun insertBookmarks(rows: List<BookmarkEntity>)

    @Insert
    suspend fun insertWrongAnswers(rows: List<WrongAnswerEntity>)

    @Insert
    suspend fun insertKnownItems(rows: List<KnownItemEntity>)

    /** 복원: 기존 사용자 데이터를 백업 내용으로 원자적으로 교체한다. */
    @Transaction
    suspend fun replaceAll(
        learningProgress: List<LearningProgressEntity>,
        bookmarks: List<BookmarkEntity>,
        wrongAnswers: List<WrongAnswerEntity>,
        knownItems: List<KnownItemEntity>
    ) {
        clearLearningProgress()
        clearBookmarks()
        clearWrongAnswers()
        clearKnownItems()
        insertLearningProgress(learningProgress)
        insertBookmarks(bookmarks)
        insertWrongAnswers(wrongAnswers)
        insertKnownItems(knownItems)
    }
}
