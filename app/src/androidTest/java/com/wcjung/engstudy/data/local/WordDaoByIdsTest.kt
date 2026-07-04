package com.wcjung.engstudy.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 오답노트 재도전에서 쓰는 getWordsByIds 회귀 테스트. */
@RunWith(AndroidJUnit4::class)
class WordDaoByIdsTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        db.openHelper.writableDatabase.apply {
            (1..5).forEach { id ->
                execSQL(
                    """INSERT INTO words (id, word, pronunciation, meaning, meaning_type,
                       part_of_speech, example_en, example_ko, stage, domain, frequency_rank, difficulty)
                       VALUES ($id, 'word$id', '', '뜻$id', 'ko', 'noun', '', '', 1, 'GENERAL', $id, 3)"""
                )
            }
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun returns_only_requested_ids() = runBlocking {
        val words = db.wordDao().getWordsByIds(listOf(2, 4))
        assertEquals(setOf(2, 4), words.map { it.id }.toSet())
    }

    @Test
    fun missing_ids_are_ignored() = runBlocking {
        val words = db.wordDao().getWordsByIds(listOf(3, 999))
        assertEquals(listOf(3), words.map { it.id })
    }
}
