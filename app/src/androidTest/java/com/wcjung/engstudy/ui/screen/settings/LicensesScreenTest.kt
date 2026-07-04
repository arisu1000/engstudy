package com.wcjung.engstudy.ui.screen.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 데이터 출처 화면 회귀 테스트.
 *
 * Tatoeba(CC BY 2.0 FR)는 저작자 표시가 라이선스 의무 사항이므로
 * 이 화면에서 누락되면 안 된다.
 */
@RunWith(AndroidJUnit4::class)
class LicensesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tatoeba_attribution_is_displayed() {
        composeRule.setContent {
            LicensesScreen(onNavigateBack = {})
        }
        composeRule.onNodeWithText("Tatoeba").assertIsDisplayed()
        composeRule.onAllNodesWithText("CC BY 2.0 FR", substring = true)
            .onFirst().assertIsDisplayed()
    }

    @Test
    fun all_required_sources_are_listed() {
        composeRule.setContent {
            LicensesScreen(onNavigateBack = {})
        }
        listOf("kengdic", "교육부 공공데이터", "Semigradsky", "wordfreq").forEach { source ->
            composeRule.onAllNodes(hasScrollAction()).onFirst()
                .performScrollToNode(hasText(source, substring = true))
            composeRule.onAllNodesWithText(source, substring = true)
                .onFirst().assertIsDisplayed()
        }
    }
}
