package com.wcjung.engstudy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.ui.navigation.EngStudyNavHost
import com.wcjung.engstudy.ui.theme.EngStudyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    // 알림 권한 요청 런처. 결과는 사용자가 시스템 다이얼로그에서 선택하며,
    // 거부하더라도 앱의 다른 기능은 정상 동작하므로 별도 처리는 하지 않는다.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            EngStudyTheme(darkTheme = darkTheme) {
                EngStudyNavHost()
            }
        }
    }

    /**
     * Android 13(API 33)+ 에서는 알림을 표시하려면 POST_NOTIFICATIONS 런타임 권한이 필요하다.
     * 이 요청이 없으면 학습 리마인더 알림이 예약되더라도 표시 단계에서 조용히 차단된다.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
