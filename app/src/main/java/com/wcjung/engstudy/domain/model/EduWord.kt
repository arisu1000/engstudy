package com.wcjung.engstudy.domain.model

data class EduWord(
    val id: Int,
    val word: String,
    val meaning: String,
    val level: EduLevel,
    val partOfSpeech: String,
    val variant1: String,
    val variant2: String
)

enum class EduLevel(
    val key: String,
    val displayNameKo: String,
    val order: Int
) {
    ELEMENTARY("초등", "초등학교", 1),
    MIDDLE_HIGH("중고", "중·고등학교", 2),
    PROFESSIONAL("전문", "전문/선택", 3);

    companion object {
        fun fromKey(key: String): EduLevel =
            entries.find { it.key == key } ?: MIDDLE_HIGH
    }
}
