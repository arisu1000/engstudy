package com.wcjung.engstudy.domain.model

data class Idiom(
    val id: Int,
    val phrase: String,
    val meaning: String,
    val meaningType: MeaningType,
    val type: IdiomType,
    val exampleEn: String,
    val exampleKo: String,
    val difficulty: Int,
    val category: String
)

enum class IdiomType(val key: String, val displayNameKo: String) {
    IDIOM("idiom", "숙어"),
    PHRASAL_VERB("phrasal_verb", "구동사");

    companion object {
        fun fromKey(key: String): IdiomType =
            entries.find { it.key == key } ?: IDIOM
    }
}
