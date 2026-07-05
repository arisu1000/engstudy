package com.wcjung.engstudy.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 교육부 단어 완전 제외/복원 DAO 회귀 테스트 (기본 단어장의 learning_progress.is_excluded와 독립된 테이블). */
@RunWith(AndroidJUnit4::class)
class EduExcludedWordDaoTest {

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
    fun excludeWord_then_restoreWord_roundtrip() = runBlocking {
        val dao = db.eduExcludedWordDao()

        dao.excludeWord(1)
        dao.excludeWord(2)
        assertEquals(setOf(1, 2), dao.getExcludedIds().first().toSet())
        assertEquals(2, dao.getExcludedCount().first())

        dao.restoreWord(1)
        assertEquals(setOf(2), dao.getExcludedIds().first().toSet())
        assertEquals(1, dao.getExcludedCount().first())
    }

    @Test
    fun excludeWord_is_idempotent_for_duplicate_calls() = runBlocking {
        val dao = db.eduExcludedWordDao()

        dao.excludeWord(1)
        dao.excludeWord(1)

        assertEquals(1, dao.getExcludedCount().first())
    }

    @Test
    fun getExcludedWords_returns_edu_word_entities() = runBlocking {
        val dao = db.eduExcludedWordDao()
        dao.excludeWord(2)

        val excluded = dao.getExcludedWords().first()
        assertEquals(listOf("word2"), excluded.map { it.word })
    }
}
