package com.wcjung.engstudy.domain.model

enum class Domain(val key: String, val displayNameKo: String) {
    DAILY_LIFE("DAILY_LIFE", "일상생활"),
    BUSINESS("BUSINESS", "비즈니스"),
    SCIENCE("SCIENCE", "과학"),
    TECHNOLOGY("TECHNOLOGY", "기술"),
    MEDICINE("MEDICINE", "의학"),
    LAW("LAW", "법률"),
    EDUCATION("EDUCATION", "교육"),
    ARTS("ARTS", "예술"),
    SPORTS("SPORTS", "스포츠"),
    TRAVEL("TRAVEL", "여행"),
    FOOD("FOOD", "음식"),
    GENERAL("GENERAL", "일반");

    companion object {
        fun fromKey(key: String): Domain =
            entries.find { it.key == key } ?: GENERAL
    }
}
