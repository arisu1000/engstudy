package com.wcjung.engstudy.domain.model

data class ExampleSentence(
    val id: Int,
    val sentenceEn: String,
    val sentenceKo: String,
    val grammarTopic: String,
    val grammarTopicKo: String,
    val level: GrammarLevel,
    val wordCount: Int
)

enum class GrammarLevel(val key: String, val displayNameKo: String) {
    BEGINNER("초급", "초급"),
    INTERMEDIATE("중급", "중급"),
    ADVANCED("고급", "고급");

    companion object {
        fun fromKey(key: String): GrammarLevel =
            entries.find { it.key == key } ?: BEGINNER
    }
}
