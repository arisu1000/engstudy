package com.wcjung.engstudy.domain.model

import com.wcjung.engstudy.data.local.entity.WordMeaningEntity

data class WordMeaning(
    val id: Int,
    val wordId: Int,
    val meaning: String,
    val pos: String,
    val senseOrder: Int
)

fun WordMeaningEntity.toDomain() = WordMeaning(
    id = id,
    wordId = wordId,
    meaning = meaning,
    pos = pos,
    senseOrder = senseOrder
)
