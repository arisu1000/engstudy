package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.wcjung.engstudy.data.local.entity.WordEntity

/**
 * 사용자 추가 단어의 id 예약 구간 시작값.
 *
 * 사용자 단어는 별도 테이블이 아니라 words 테이블의 id >= 1,000,000 구간에 저장한다.
 * 이렇게 하면 학습/퀴즈/복습/북마크/오답 등 words.id 기반 기능 전체에 자동으로 통합되고,
 * 콘텐츠 갱신 마이그레이션(RefreshWordContentMigration)이 asset id 기준 UPDATE만 하므로
 * 사용자 단어가 유실되지 않는다. build_word_db.py는 이 구간 미만의 id만 생성해야 한다.
 */
const val USER_WORD_ID_START = 1_000_000

fun isUserWordId(id: Int): Boolean = id >= USER_WORD_ID_START

/**
 * 사용자 추가 단어 전용 DAO — known_items/edu_bookmarks와 동일하게
 * 리포지토리 계층 없이 ViewModel에 직접 주입하는 컨벤션을 따른다.
 */
@Dao
interface UserWordDao {

    @Query(
        "SELECT COALESCE(MAX(id) + 1, $USER_WORD_ID_START) FROM words " +
            "WHERE id >= $USER_WORD_ID_START"
    )
    suspend fun getNextUserWordId(): Int

    /** 중복 확인용 — 콘텐츠 단어와 사용자 단어를 모두 대소문자 무시로 조회한다. */
    @Query("SELECT * FROM words WHERE lower(word) = lower(:word) LIMIT 1")
    suspend fun findByText(word: String): WordEntity?

    @Insert
    suspend fun insert(word: WordEntity)

    /** id 예약 구간 조건으로 콘텐츠 단어가 삭제되는 사고를 방지한다. */
    @Query("DELETE FROM words WHERE id = :id AND id >= $USER_WORD_ID_START")
    suspend fun deleteUserWord(id: Int): Int

    /** id 발급과 삽입을 트랜잭션으로 묶어 동시 추가 시 id 충돌을 방지한다. */
    @Transaction
    suspend fun addUserWord(word: WordEntity): Int {
        val id = getNextUserWordId()
        insert(word.copy(id = id))
        return id
    }
}
