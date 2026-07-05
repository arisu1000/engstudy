package com.wcjung.engstudy.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wcjung.engstudy.data.local.dao.USER_WORD_ID_START
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 사용자 추가 단어 회귀 테스트 — words 테이블의 id 예약 구간(>= USER_WORD_ID_START)에
 * 저장되어 콘텐츠 단어와 공존하고, 일일 챌린지 등 기기 간 결정성이 필요한 기능에서는
 * 제외되는지 검증한다.
 */
@RunWith(AndroidJUnit4::class)
class UserWordDaoTest {

    private lateinit var db: AppDatabase

    private fun userWordTemplate(word: String, meaning: String) = WordEntity(
        id = 0,
        word = word,
        meaning = meaning,
        stage = 1,
        frequencyRank = 0
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        // 콘텐츠 단어 3개 (id 예약 구간 미만)
        db.openHelper.writableDatabase.apply {
            (1..3).forEach { id ->
                execSQL(
                    """INSERT INTO words (id, word, pronunciation, meaning, meaning_type,
                       part_of_speech, example_en, example_ko, stage, domain, frequency_rank, difficulty)
                       VALUES ($id, 'content$id', '', '뜻$id', 'ko', 'noun', '', '', 1, 'GENERAL', $id, 3)"""
                )
            }
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addUserWord_assigns_ids_from_reserved_range_sequentially() = runBlocking {
        val dao = db.userWordDao()

        val firstId = dao.addUserWord(userWordTemplate("serendipity", "뜻밖의 발견"))
        val secondId = dao.addUserWord(userWordTemplate("ephemeral", "덧없는"))

        assertEquals(USER_WORD_ID_START, firstId)
        assertEquals(USER_WORD_ID_START + 1, secondId)
        assertEquals("serendipity", db.wordDao().getWordById(firstId)?.word)
    }

    @Test
    fun findByText_matches_content_and_user_words_case_insensitively() = runBlocking {
        val dao = db.userWordDao()
        dao.addUserWord(userWordTemplate("Serendipity", "뜻밖의 발견"))

        assertNotNull(dao.findByText("CONTENT1"))
        assertNotNull(dao.findByText("serendipity"))
        assertNull(dao.findByText("nonexistent"))
    }

    @Test
    fun deleteUserWord_removes_user_word_but_never_content_words() = runBlocking {
        val dao = db.userWordDao()
        val userId = dao.addUserWord(userWordTemplate("ephemeral", "덧없는"))

        assertEquals(1, dao.deleteUserWord(userId))
        assertNull(db.wordDao().getWordById(userId))

        // 콘텐츠 단어 id로는 삭제가 거부된다 (id 예약 구간 가드)
        assertEquals(0, dao.deleteUserWord(1))
        assertNotNull(db.wordDao().getWordById(1))
    }

    @Test
    fun deleteUserWord_cascades_learning_progress() = runBlocking {
        val dao = db.userWordDao()
        val userId = dao.addUserWord(userWordTemplate("ephemeral", "덧없는"))
        db.learningProgressDao().upsertProgress(LearningProgressEntity(wordId = userId))

        dao.deleteUserWord(userId)

        assertNull(db.learningProgressDao().getProgressForWord(userId))
    }

    @Test
    fun dailyChallenge_excludes_user_words_for_cross_device_determinism() = runBlocking {
        db.userWordDao().addUserWord(userWordTemplate("serendipity", "뜻밖의 발견"))

        val challengeIds = db.wordDao().getDailyChallengeWords(seed = 12345L, count = 10).map { it.id }

        assertEquals(3, challengeIds.size)
        assertTrue(challengeIds.all { it < USER_WORD_ID_START })
    }

    @Test
    fun backup_replaceAll_restores_user_words_with_their_progress() = runBlocking {
        val userWordDao = db.userWordDao()
        val backupDao = db.backupDao()
        val userId = userWordDao.addUserWord(userWordTemplate("serendipity", "뜻밖의 발견"))
        db.learningProgressDao().upsertProgress(LearningProgressEntity(wordId = userId))

        // 내보내기 스냅샷
        val exportedUserWords = backupDao.getAllUserWords()
        val exportedProgress = backupDao.getAllLearningProgress()
        assertEquals(listOf(userId), exportedUserWords.map { it.id })

        // 로컬 데이터를 비운 뒤 복원
        userWordDao.deleteUserWord(userId)
        backupDao.replaceAll(
            learningProgress = exportedProgress,
            bookmarks = emptyList(),
            wrongAnswers = emptyList(),
            knownItems = emptyList(),
            userWords = exportedUserWords
        )

        assertEquals("serendipity", db.wordDao().getWordById(userId)?.word)
        assertNotNull(db.learningProgressDao().getProgressForWord(userId))
    }
}
