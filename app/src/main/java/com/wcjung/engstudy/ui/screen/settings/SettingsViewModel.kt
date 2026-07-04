package com.wcjung.engstudy.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcjung.engstudy.data.backup.BackupManager
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.wcjung.engstudy.util.launchSafely
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userPreferences: UserPreferences,
    private val notificationHelper: NotificationHelper,
    private val backupManager: BackupManager
) : ViewModel() {

    /** 백업/복원 결과 안내 메시지 (스낵바 표시 후 clearBackupMessage로 소비) */
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage

    val darkMode: StateFlow<Boolean> = userPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val ttsSpeed: StateFlow<Float> = userPreferences.ttsSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.85f)

    val dailyGoal: StateFlow<Int> = userPreferences.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val themeMode: StateFlow<String> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val notificationEnabled: StateFlow<Boolean> = userPreferences.notificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean) {
        launchSafely { userPreferences.setDarkMode(enabled) }
    }

    fun setThemeMode(mode: String) {
        launchSafely { userPreferences.setThemeMode(mode) }
    }

    fun setTtsSpeed(speed: Float) {
        launchSafely { userPreferences.setTtsSpeed(speed) }
    }

    fun setDailyGoal(goal: Int) {
        launchSafely { userPreferences.setDailyGoal(goal) }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        launchSafely {
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

    /** 학습 데이터를 사용자가 고른 파일(SAF)로 내보낸다. */
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val jsonText = backupManager.exportToJson()
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri, "wt")?.use {
                        it.write(jsonText.toByteArray(Charsets.UTF_8))
                    } ?: error("파일을 열 수 없습니다")
                }
            }.onSuccess {
                _backupMessage.value = "백업 파일을 저장했습니다"
            }.onFailure {
                _backupMessage.value = "백업 실패: ${it.message}"
            }
        }
    }

    /** 백업 파일을 읽어 복원한다. 기존 학습 데이터는 교체된다. */
    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val raw = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    } ?: error("파일을 열 수 없습니다")
                }
                backupManager.importFromJson(raw)
            }.onSuccess { result ->
                // 복원된 알림 설정에 맞춰 리마인더 재예약
                if (userPreferences.notificationEnabled.first()) {
                    notificationHelper.scheduleReminder(
                        userPreferences.notificationHour.first(),
                        userPreferences.notificationMinute.first()
                    )
                } else {
                    notificationHelper.cancelReminder()
                }
                val skipped = if (result.skippedCount > 0) " (${result.skippedCount}건 건너뜀)" else ""
                _backupMessage.value =
                    "복원 완료: 학습 진도 ${result.progressCount}건, 북마크 ${result.bookmarkCount}건, " +
                        "오답 ${result.wrongAnswerCount}건$skipped"
            }.onFailure {
                _backupMessage.value = "복원 실패: 올바른 백업 파일인지 확인해 주세요"
            }
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }
}
