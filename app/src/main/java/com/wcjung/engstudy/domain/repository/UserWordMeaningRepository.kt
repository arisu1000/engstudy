package com.wcjung.engstudy.domain.repository

import com.wcjung.engstudy.domain.model.UserWordMeaning
import kotlinx.coroutines.flow.Flow

/** 사용자 정의 단어 의미 (추가한 "내 의미" + 대표 뜻 변경). */
interface UserWordMeaningRepository {
    fun getForWord(wordId: Int): Flow<List<UserWordMeaning>>

    /** "내 의미" 추가 (대표 뜻은 바꾸지 않음) */
    suspend fun addMeaning(wordId: Int, meaning: String)

    suspend fun deleteMeaning(id: Long)

    /** 대표 뜻을 사용자 입력으로 교체 (목록/퀴즈/복습 전역 반영) */
    suspend fun setPrimaryMeaning(wordId: Int, meaning: String)

    /** 대표 뜻 변경을 취소하고 기본 뜻으로 복원 */
    suspend fun clearPrimaryMeaning(wordId: Int)
}
