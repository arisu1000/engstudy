package com.wcjung.engstudy.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wcjung.engstudy.di.DatabaseModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * v10 → v11 마이그레이션 회귀 테스트.
 *
 * user_word_meanings 테이블이 Room 기대 스키마와 정확히 일치하게 생성되고
 * (runMigrationsAndValidate가 검증), 기존 사용자 데이터는 보존되어야 한다.
 */
@RunWith(AndroidJUnit4::class)
class Migration10To11Test {

    private val testDbName = "migration-10-11-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate10To11_createsUserMeaningTable_andPreservesUserData() {
        helper.createDatabase(testDbName, 10).use { db ->
            db.execSQL(
                """INSERT INTO words (id, word, pronunciation, meaning, meaning_type, part_of_speech,
                    example_en, example_ko, stage, domain, frequency_rank, difficulty)
                    VALUES (1, 'apple', '', '사과', 'ko', 'noun', '', '', 1, 'GENERAL', 1, 1)"""
            )
            db.execSQL(
                """INSERT INTO learning_progress (word_id, ease_factor, interval_days, repetitions,
                    next_review_date, times_correct, times_incorrect, is_learned, is_excluded)
                    VALUES (1, 2.5, 7, 3, 1000, 5, 2, 0, 0)"""
            )
        }

        val db = helper.runMigrationsAndValidate(
            testDbName, 11, true, DatabaseModule.MIGRATION_10_11
        )

        // 새 테이블이 비어 있는 상태로 생성됨
        db.query("SELECT COUNT(*) FROM user_word_meanings").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
        // 삽입 가능 (FK 포함)
        db.execSQL(
            "INSERT INTO user_word_meanings (word_id, meaning, is_primary, created_at) VALUES (1, '능금', 1, 123)"
        )
        db.query("SELECT meaning FROM user_word_meanings WHERE word_id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("능금", c.getString(0))
        }
        // 기존 사용자 데이터 보존
        db.query("SELECT repetitions FROM learning_progress WHERE word_id = 1").use { c ->
            assertTrue("learning_progress 유실", c.moveToFirst())
            assertEquals(3, c.getInt(0))
        }
    }
}
