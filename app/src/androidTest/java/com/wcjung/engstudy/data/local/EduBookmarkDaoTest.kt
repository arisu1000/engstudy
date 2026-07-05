package com.wcjung.engstudy.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 교육부 단어 북마크 DAO 회귀 테스트 (기본 단어장의 bookmarks/BookmarkDao와 독립된 테이블). */
@RunWith(AndroidJUnit4::class)
class EduBookmarkDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        db.openHelper.writableDatabase.apply {
            (1..3).forEach { id ->
                execSQL(
                    """INSERT INTO edu_words (id, word, meaning, level, part_of_speech, variant1, variant2)
                       VALUES ($id, 'word$id', '뜻$id', '초등', 'noun', '', '')"""
                )
            }
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun toggleBookmarkAtomic_adds_then_removes() = runBlocking {
        val dao = db.eduBookmarkDao()

        dao.toggleBookmarkAtomic(1)
        assertTrue(dao.isBookmarked(1).first())

        dao.toggleBookmarkAtomic(1)
        assertFalse(dao.isBookmarked(1).first())
    }

    @Test
    fun getBookmarkedWords_returns_only_bookmarked_edu_words() = runBlocking {
        val dao = db.eduBookmarkDao()
        dao.toggleBookmarkAtomic(1)
        dao.toggleBookmarkAtomic(3)

        val bookmarked = dao.getBookmarkedWords().first().map { it.id }
        assertEquals(setOf(1, 3), bookmarked.toSet())
        assertEquals(setOf(1, 3), dao.getBookmarkedIds().first().toSet())
    }
}
