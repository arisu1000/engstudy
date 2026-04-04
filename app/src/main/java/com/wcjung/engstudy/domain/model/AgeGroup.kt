package com.wcjung.engstudy.domain.model

enum class AgeGroup(val key: String, val displayNameKo: String, val order: Int) {
    ELEMENTARY("ELEMENTARY", "초등학교", 1),
    MIDDLE_SCHOOL("MIDDLE_SCHOOL", "중학교", 2),
    HIGH_SCHOOL("HIGH_SCHOOL", "고등학교", 3),
    COLLEGE("COLLEGE", "대학교", 4),
    PROFESSIONAL("PROFESSIONAL", "전문가/성인", 5);

    companion object {
        fun fromKey(key: String): AgeGroup =
            entries.find { it.key == key } ?: GENERAL

        private val GENERAL = COLLEGE
    }
}
