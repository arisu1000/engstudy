package com.wcjung.engstudy.domain.model

enum class Stage(
    val level: Int,
    val displayNameKo: String,
    val displayNameEn: String,
    val cefr: String,
    val description: String
) {
    FOUNDATION(1, "기초", "Foundation", "A1-A2", "생존 영어"),
    INTERMEDIATE(2, "중급", "Intermediate", "B1", "일상 대화"),
    UPPER_INTERMEDIATE(3, "중상급", "Upper-Inter.", "B2", "뉴스/업무"),
    ADVANCED(4, "고급", "Advanced", "C1", "학술/전문"),
    PROFICIENT(5, "숙련", "Proficient", "C2", "원어민 근접"),
    NEAR_NATIVE(6, "네이티브", "Near-Native", "-", "원어민 수준");

    companion object {
        fun fromLevel(level: Int): Stage =
            entries.find { it.level == level } ?: INTERMEDIATE
    }
}
