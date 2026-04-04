package com.wcjung.engstudy.domain.model

data class Word(
    val id: Int,
    val word: String,
    val pronunciation: String,
    val meaningKo: String,
    val partOfSpeech: String,
    val exampleEn: String,
    val exampleKo: String,
    val domain: Domain,
    val ageGroup: AgeGroup,
    val frequencyRank: Int,
    val difficulty: Int,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val notes: String?
)
