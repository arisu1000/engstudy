package com.wcjung.engstudy.domain.model

/** 사용자가 직접 추가/변경한 단어 의미. isPrimary면 대표 뜻을 대체한다. */
data class UserWordMeaning(
    val id: Long,
    val wordId: Int,
    val meaning: String,
    val isPrimary: Boolean,
    val createdAt: Long
)
