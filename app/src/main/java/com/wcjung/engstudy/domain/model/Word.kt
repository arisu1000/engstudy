package com.wcjung.engstudy.domain.model

data class Word(
    val id: Int,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val meaningType: MeaningType,
    val partOfSpeech: String,
    val exampleEn: String,
    val exampleKo: String,
    val stage: Stage,
    val domain: Domain,
    val frequencyRank: Int,
    val difficulty: Int,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val notes: String?
)

enum class MeaningType(val key: String) {
    KOREAN("ko"),
    ENGLISH("en");

    companion object {
        fun fromKey(key: String): MeaningType =
            entries.find { it.key == key } ?: KOREAN
    }
}
