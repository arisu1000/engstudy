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

/**
 * 검색 관련도 정렬 회귀 테스트.
 *
 * searchWords는 "정확 일치 > 접두 일치 > 부분 일치 > 뜻 일치" 순으로,
 * 같은 순위에서는 빈도순으로 정렬해야 한다.
 */
@RunWith(AndroidJUnit4::class)
class WordSearchRelevanceTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        // 부분 일치 후보 두 개는 빈도 역전 배치(react가 compact보다 희귀)로 빈도 정렬도 검증
        db.openHelper.writableDatabase.apply {
            execSQL(insertWord(1, "act", "행동하다", 500))
            execSQL(insertWord(2, "action", "행동", 100))
            execSQL(insertWord(3, "react", "반응하다", 50))
            execSQL(insertWord(4, "compact", "소형의", 10))
            execSQL(insertWord(5, "performance", "연기(act)", 1))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun insertWord(id: Int, word: String, meaning: String, rank: Int) =
        """INSERT INTO words (id, word, pronunciation, meaning, meaning_type, part_of_speech,
           example_en, example_ko, stage, domain, frequency_rank, difficulty)
           VALUES ($id, '$word', '', '$meaning', 'ko', 'verb', '', '', 1, 'GENERAL', $rank, 3)"""

    @Test
    fun search_orders_exact_prefix_substring_then_meaning() = runBlocking {
        val results = db.wordDao().searchWords("act").first().map { it.word }
        assertEquals(
            listOf("act", "action", "compact", "react", "performance"),
            results
        )
    }

    @Test
    fun exact_match_is_case_insensitive() = runBlocking {
        val results = db.wordDao().searchWords("ACT").first().map { it.word }
        assertEquals("act", results.first())
    }
}
