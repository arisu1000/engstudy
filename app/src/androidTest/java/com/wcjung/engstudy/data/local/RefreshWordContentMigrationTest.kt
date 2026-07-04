package com.wcjung.engstudy.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * v9 → v10 콘텐츠 갱신 마이그레이션 회귀 테스트.
 *
 * 시나리오: v9 기기 DB에 옛 kengdic 뜻과 사용자 학습 데이터를 심은 뒤 마이그레이션을 실행하면
 * - words.meaning은 asset DB의 개선된 뜻으로 갱신되고
 * - learning_progress / bookmarks / wrong_answers는 그대로 보존되어야 한다.
 */
@RunWith(AndroidJUnit4::class)
class RefreshWordContentMigrationTest {

    private val testDbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate9To10_updatesMeanings_andPreservesUserData() {
        // Arrange: v9 스키마 DB에 옛 뜻(가나다순 kengdic)과 사용자 데이터 삽입
        helper.createDatabase(testDbName, 9).use { db ->
            db.execSQL(
                """INSERT INTO words (id, word, pronunciation, meaning, meaning_type, part_of_speech,
                    example_en, example_ko, stage, domain, frequency_rank, difficulty)
                    VALUES (1, 'the', '', '관사(옛뜻)', 'ko', 'article', '', '', 1, 'GENERAL', 1, 1)"""
            )
            // white: asset에서 교육부 뜻 "흰"으로 갱신되어야 하는 대표 케이스
            db.execSQL(
                """INSERT INTO words (id, word, pronunciation, meaning, meaning_type, part_of_speech,
                    example_en, example_ko, stage, domain, frequency_rank, difficulty)
                    VALUES (999999, 'white', '', '공백의, 눈이 많은, 반공산주의의', 'ko', 'adjective',
                    '', '', 1, 'GENERAL', 2, 1)"""
            )
            db.execSQL("INSERT INTO word_meanings (word_id, meaning, pos, meaning_type, sense_order, source) VALUES (1, '옛의미', 'noun', 'ko', 0, 'kengdic')")
            db.execSQL("INSERT INTO word_examples (word_id, sentence_en, sentence_ko, source) VALUES (1, 'old example', '옛예문', 'tatoeba')")
            // 사용자 데이터
            db.execSQL(
                """INSERT INTO learning_progress (word_id, ease_factor, interval_days, repetitions,
                    next_review_date, last_reviewed_date, times_correct, times_incorrect,
                    is_learned, is_excluded)
                    VALUES (1, 2.5, 7, 3, 1000, 900, 5, 2, 0, 0)"""
            )
            db.execSQL("INSERT INTO bookmarks (word_id, created_at) VALUES (1, 12345)")
            db.execSQL(
                """INSERT INTO wrong_answers (word_id, wrong_answer, correct_answer, quiz_type, created_at)
                    VALUES (1, '오답', '정답', 'QUIZ', 12345)"""
            )
        }

        // Act
        val db = helper.runMigrationsAndValidate(
            testDbName, 10, true,
            RefreshWordContentMigration(
                InstrumentationRegistry.getInstrumentation().targetContext
            ),
        )

        // Assert: 뜻이 asset 내용으로 갱신됨
        db.query("SELECT meaning FROM words WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertNotEquals("관사(옛뜻)", c.getString(0))
        }
        // 기기에만 있던 id(asset에 없는 id 999999)는 그대로 남는다 (삭제 없음 보장)
        db.query("SELECT COUNT(*) FROM words WHERE id = 999999").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
        // asset의 white(실제 id)는 교육부 뜻 "흰"
        db.query("SELECT meaning FROM words WHERE word = 'white' AND id != 999999").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("흰", c.getString(0))
        }
        // word_meanings가 asset 내용으로 교체됨 (옛의미 제거, 대량 삽입)
        db.query("SELECT COUNT(*) FROM word_meanings WHERE meaning = '옛의미'").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM word_meanings").use { c ->
            c.moveToFirst()
            assertTrue("word_meanings가 비어 있음", c.getInt(0) > 10000)
        }
        // word_examples도 asset 내용으로 교체됨 (LLM 예문 커버리지 99.8% 반영)
        db.query("SELECT COUNT(*) FROM word_examples WHERE sentence_ko = '옛예문'").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
        db.query("SELECT COUNT(DISTINCT word_id) FROM word_examples").use { c ->
            c.moveToFirst()
            assertTrue("word_examples 커버리지 부족", c.getInt(0) > 11000)
        }
        // 사용자 데이터 보존
        db.query(
            "SELECT repetitions, interval_days, times_correct FROM learning_progress WHERE word_id = 1"
        ).use { c ->
            assertTrue("learning_progress 유실", c.moveToFirst())
            assertEquals(3, c.getInt(0))
            assertEquals(7, c.getInt(1))
            assertEquals(5, c.getInt(2))
        }
        db.query("SELECT COUNT(*) FROM bookmarks WHERE word_id = 1").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM wrong_answers WHERE word_id = 1").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
    }
}
