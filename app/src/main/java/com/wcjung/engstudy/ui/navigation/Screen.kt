package com.wcjung.engstudy.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Study : Screen
    @Serializable data object Review : Screen
    @Serializable data object Profile : Screen
    @Serializable data class WordList(val domain: String? = null, val stage: Int? = null) : Screen
    @Serializable data class WordDetail(val wordId: Int) : Screen
    @Serializable data class FlashCard(val domain: String? = null, val stage: Int? = null) : Screen
    @Serializable data class Quiz(
        val domain: String? = null,
        val stage: Int? = null,
        // 지정 시 stage/domain 대신 이 단어들로 출제 (오답노트 재도전 등)
        val wordIds: List<Int>? = null,
        // 듣기 모드: 발음을 듣고 뜻을 고른다 (문제 텍스트는 정답 후 공개)
        val listening: Boolean = false
    ) : Screen
    @Serializable data class SpellingQuiz(val domain: String? = null, val stage: Int? = null) : Screen
    @Serializable data object Bookmarks : Screen
    @Serializable data object Search : Screen
    @Serializable data object Statistics : Screen
    @Serializable data object Settings : Screen
    @Serializable data object EduHome : Screen
    @Serializable data class EduWordList(val level: String? = null) : Screen
    @Serializable data object WrongAnswers : Screen
    @Serializable data class EduFlashCard(val level: String? = null) : Screen
    @Serializable data class EduQuiz(val level: String? = null) : Screen
    @Serializable data object PlacementTest : Screen
    @Serializable data object DailyChallenge : Screen
    @Serializable data object IdiomHome : Screen
    @Serializable data class IdiomList(val type: String? = null) : Screen
    @Serializable data class IdiomQuiz(val type: String? = null) : Screen
    @Serializable data object GrammarHome : Screen
    @Serializable data class GrammarList(val topic: String? = null) : Screen
    @Serializable data object ExcludedWords : Screen
    @Serializable data object Licenses : Screen
}
