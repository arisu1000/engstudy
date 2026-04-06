package com.wcjung.engstudy.domain.model

import com.wcjung.engstudy.data.local.entity.WordExampleEntity

data class WordExample(
    val id: Int,
    val wordId: Int,
    val sentenceEn: String,
    val sentenceKo: String,
    val source: String
)

fun WordExampleEntity.toDomain() = WordExample(
    id = id,
    wordId = wordId,
    sentenceEn = sentenceEn,
    sentenceKo = sentenceKo,
    source = source
)
