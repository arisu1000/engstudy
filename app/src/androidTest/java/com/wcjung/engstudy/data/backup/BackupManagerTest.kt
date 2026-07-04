package com.wcjung.engstudy.data.backup

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** 백업 내보내기 → 복원 왕복 회귀 테스트. */
@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDatabase
    private lateinit var prefsFile: File
    private lateinit var scope: CoroutineScope
    private lateinit var userPreferences: UserPreferences
    private lateinit var manager: BackupManager

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        prefsFile = File(context.cacheDir, "test_prefs_${UUID.randomUUID()}.preferences_pb")
        scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
        userPreferences = UserPreferences(
            PreferenceDataStoreFactory.create(scope = scope) { prefsFile }
        )
        manager = BackupManager(db.backupDao(), userPreferences)

        db.openHelper.writableDatabase.apply {
            (1..3).forEach { id ->
                execSQL(
                    """INSERT INTO words (id, word, pronunciation, meaning, meaning_type,
                       part_of_speech, example_en, example_ko, stage, domain, frequency_rank, difficulty)
                       VALUES ($id, 'word$id', '', '뜻$id', 'ko', 'noun', '', '', 1, 'GENERAL', $id, 3)"""
                )
            }
            execSQL(
                """INSERT INTO learning_progress (word_id, ease_factor, interval_days, repetitions,
                   next_review_date, times_correct, times_incorrect, is_learned, is_excluded)
                   VALUES (1, 2.8, 6, 3, 1000, 5, 1, 0, 0)"""
            )
            execSQL("INSERT INTO bookmarks (word_id, created_at) VALUES (2, 123)")
            execSQL(
                """INSERT INTO wrong_answers (word_id, wrong_answer, correct_answer, quiz_type, created_at)
                   VALUES (3, '오답', '정답', 'MULTIPLE_CHOICE', 456)"""
            )
            execSQL("INSERT INTO known_items (item_id, item_type, marked_at) VALUES (10, 'edu_word', 789)")
        }
    }

    @After
    fun tearDown() {
        db.close()
        scope.cancel()
        prefsFile.delete()
    }

    @Test
    fun export_then_import_restores_all_user_data() = runBlocking {
        userPreferences.setDailyGoal(55)
        userPreferences.updateStreak(7, 999L)

        val json = manager.exportToJson()

        // 데이터를 비우고 설정을 바꾼 뒤 복원
        db.backupDao().replaceAll(emptyList(), emptyList(), emptyList(), emptyList())
        userPreferences.setDailyGoal(20)

        val result = manager.importFromJson(json)

        assertEquals(1, result.progressCount)
        assertEquals(1, result.bookmarkCount)
        assertEquals(1, result.wrongAnswerCount)
        assertEquals(1, result.knownItemCount)
        assertEquals(0, result.skippedCount)

        val progress = db.backupDao().getAllLearningProgress().single()
        assertEquals(1, progress.wordId)
        assertEquals(2.8f, progress.easeFactor, 0.001f)
        assertEquals(3, progress.repetitions)
        assertEquals(2, db.backupDao().getAllBookmarks().single().wordId)
        assertEquals("오답", db.backupDao().getAllWrongAnswers().single().wrongAnswer)
        assertEquals("edu_word", db.backupDao().getAllKnownItems().single().itemType)
        assertEquals(55, userPreferences.dailyGoal.first())
        assertEquals(7, userPreferences.streakDays.first())
    }

    @Test
    fun rows_referencing_unknown_words_are_skipped() = runBlocking {
        val json = manager.exportToJson()
            .replace("\"word_id\":2", "\"word_id\":999999") // 존재하지 않는 단어를 참조하는 북마크

        val result = manager.importFromJson(json)

        assertEquals(1, result.skippedCount)
        assertTrue(db.backupDao().getAllBookmarks().isEmpty())
        assertEquals(1, db.backupDao().getAllLearningProgress().size)
    }

    @Test(expected = Exception::class)
    fun invalid_json_throws(): Unit = runBlocking {
        manager.importFromJson("이건 백업 파일이 아님")
    }
}
