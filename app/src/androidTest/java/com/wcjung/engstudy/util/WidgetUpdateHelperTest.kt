package com.wcjung.engstudy.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 위젯 단어 순환 캐시 회귀 테스트. */
@RunWith(AndroidJUnit4::class)
class WidgetUpdateHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val words = listOf(
        WidgetWord(1, "apple", "/ˈæpəl/", "사과"),
        WidgetWord(2, "banana", "/bəˈnænə/", "바나나"),
        WidgetWord(3, "cherry", "/ˈtʃeri/", "체리"),
    )

    @Before
    fun setUp() {
        context.getSharedPreferences("word_widget_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun empty_cache_returns_null() {
        assertNull(WidgetUpdateHelper.getCurrentWord(context))
    }

    @Test
    fun update_starts_from_first_word() {
        WidgetUpdateHelper.updateWidgetWords(context, words)
        assertEquals("apple", WidgetUpdateHelper.getCurrentWord(context)?.word)
    }

    @Test
    fun advance_cycles_through_and_wraps_around() {
        WidgetUpdateHelper.updateWidgetWords(context, words)
        WidgetUpdateHelper.advanceToNextWord(context)
        assertEquals("banana", WidgetUpdateHelper.getCurrentWord(context)?.word)
        WidgetUpdateHelper.advanceToNextWord(context)
        assertEquals("cherry", WidgetUpdateHelper.getCurrentWord(context)?.word)
        WidgetUpdateHelper.advanceToNextWord(context)
        assertEquals("apple", WidgetUpdateHelper.getCurrentWord(context)?.word)
    }

    @Test
    fun update_resets_index_to_first() {
        WidgetUpdateHelper.updateWidgetWords(context, words)
        WidgetUpdateHelper.advanceToNextWord(context)
        WidgetUpdateHelper.updateWidgetWords(context, words)
        assertEquals("apple", WidgetUpdateHelper.getCurrentWord(context)?.word)
    }

    @Test
    fun corrupted_cache_is_treated_as_empty() {
        context.getSharedPreferences("word_widget_prefs", Context.MODE_PRIVATE)
            .edit().putString("widget_words_json", "not json").commit()
        assertNull(WidgetUpdateHelper.getCurrentWord(context))
    }
}
