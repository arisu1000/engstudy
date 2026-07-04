package com.wcjung.engstudy.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wcjung.engstudy.data.local.entity.UserWordMeaningEntity
import com.wcjung.engstudy.data.repository.WordRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 사용자 정의 의미 DAO + 대표 뜻 override 합성 회귀 테스트. */
@RunWith(AndroidJUnit4::class)
class UserWordMeaningDaoTest {

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
                    """INSERT INTO words (id, word, pronunciation, meaning, meaning_type,
                       part_of_speech, example_en, example_ko, stage, domain, frequency_rank, difficulty)
                       VALUES ($id, 'word$id', '', '기본뜻$id', 'ko', 'noun', '', '', 1, 'GENERAL', $id, 3)"""
                )
            }
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun setPrimary_keeps_at_most_one_primary_per_word() = runBlocking {
        val dao = db.userWordMeaningDao()
        dao.setPrimary(1, "첫 수정")
        dao.setPrimary(1, "두 번째 수정")

        val primaries = dao.getPrimaryOverrides().filter { it.wordId == 1 }
        assertEquals(1, primaries.size)
        assertEquals("두 번째 수정", primaries.single().meaning)
    }

    @Test
    fun clearPrimary_removes_override_but_keeps_custom_meanings() = runBlocking {
        val dao = db.userWordMeaningDao()
        dao.setPrimary(1, "수정한 뜻")
        dao.insert(UserWordMeaningEntity(wordId = 1, meaning = "추가한 의미"))

        dao.clearPrimary(1)

        val remaining = dao.getForWord(1).first()
        assertEquals(listOf("추가한 의미"), remaining.map { it.meaning })
    }

    @Test
    fun repository_applies_primary_override_to_word_queries() = runBlocking {
        val repository = WordRepositoryImpl(
            wordDao = db.wordDao(),
            wordMeaningDao = db.wordMeaningDao(),
            wordExampleDao = db.wordExampleDao(),
            userWordMeaningDao = db.userWordMeaningDao()
        )
        db.userWordMeaningDao().setPrimary(2, "내가 고친 뜻")

        // 단건 조회
        assertEquals("내가 고친 뜻", repository.getWordById(2)?.meaning)
        assertEquals("기본뜻1", repository.getWordById(1)?.meaning)
        // 목록 조회 (Flow 합성)
        val searched = repository.searchWords("word").first().associate { it.word to it.meaning }
        assertEquals("내가 고친 뜻", searched["word2"])
        assertEquals("기본뜻3", searched["word3"])
        // id 목록 조회 (오답 재도전 경로)
        val byIds = repository.getWordsByIds(listOf(1, 2)).associate { it.id to it.meaning }
        assertEquals("내가 고친 뜻", byIds[2])
    }
}
