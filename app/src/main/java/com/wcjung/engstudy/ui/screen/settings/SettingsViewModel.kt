package com.wcjung.engstudy.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val darkMode: StateFlow<Boolean> = userPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val ttsSpeed: StateFlow<Float> = userPreferences.ttsSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.85f)

    val dailyGoal: StateFlow<Int> = userPreferences.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val notificationEnabled: StateFlow<Boolean> = userPreferences.notificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setDarkMode(enabled) }
    }

    fun setTtsSpeed(speed: Float) {
        viewModelScope.launch { userPreferences.setTtsSpeed(speed) }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { userPreferences.setDailyGoal(goal) }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationEnabled(enabled)
            if (enabled) {
                val hour = userPreferences.notificationHour.first()
                val minute = userPreferences.notificationMinute.first()
                notificationHelper.scheduleReminder(hour, minute)
            } else {
                notificationHelper.cancelReminder()
            }
        }
    }
}
