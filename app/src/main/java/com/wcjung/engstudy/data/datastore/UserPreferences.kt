package com.wcjung.engstudy.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val PREFERRED_STAGE = intPreferencesKey("preferred_stage")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_STUDY_DATE = longPreferencesKey("last_study_date")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAS_COMPLETED_PLACEMENT_TEST = booleanPreferencesKey("has_completed_placement_test")
        val RECOMMENDED_STAGE = intPreferencesKey("recommended_stage")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val ttsSpeed: Flow<Float> = dataStore.data.map { it[Keys.TTS_SPEED] ?: 0.85f }
    val dailyGoal: Flow<Int> = dataStore.data.map { it[Keys.DAILY_GOAL] ?: 20 }
    val notificationHour: Flow<Int> = dataStore.data.map { it[Keys.NOTIFICATION_HOUR] ?: 20 }
    val notificationMinute: Flow<Int> = dataStore.data.map { it[Keys.NOTIFICATION_MINUTE] ?: 0 }
    val notificationEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.NOTIFICATION_ENABLED] ?: false }
    val preferredStage: Flow<Int?> = dataStore.data.map { it[Keys.PREFERRED_STAGE] }
    val streakDays: Flow<Int> = dataStore.data.map { it[Keys.STREAK_DAYS] ?: 0 }
    val lastStudyDate: Flow<Long> = dataStore.data.map { it[Keys.LAST_STUDY_DATE] ?: 0L }
    val themeMode: Flow<String> = dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val hasCompletedPlacementTest: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_COMPLETED_PLACEMENT_TEST] ?: false }
    val recommendedStage: Flow<Int> = dataStore.data.map { it[Keys.RECOMMENDED_STAGE] ?: 1 }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setTtsSpeed(speed: Float) {
        dataStore.edit { it[Keys.TTS_SPEED] = speed }
    }

    suspend fun setDailyGoal(goal: Int) {
        dataStore.edit { it[Keys.DAILY_GOAL] = goal }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.NOTIFICATION_HOUR] = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setPreferredStage(stage: Int?) {
        dataStore.edit {
            if (stage != null) {
                it[Keys.PREFERRED_STAGE] = stage
            } else {
                it.remove(Keys.PREFERRED_STAGE)
            }
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun updateStreak(streakDays: Int, studyDate: Long) {
        dataStore.edit {
            it[Keys.STREAK_DAYS] = streakDays
            it[Keys.LAST_STUDY_DATE] = studyDate
        }
    }

    suspend fun setPlacementTestCompleted() {
        dataStore.edit { it[Keys.HAS_COMPLETED_PLACEMENT_TEST] = true }
    }

    suspend fun setRecommendedStage(stage: Int) {
        dataStore.edit { it[Keys.RECOMMENDED_STAGE] = stage }
    }
}
