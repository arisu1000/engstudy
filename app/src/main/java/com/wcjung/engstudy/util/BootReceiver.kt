package com.wcjung.engstudy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wcjung.engstudy.data.datastore.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = userPreferences.notificationEnabled.first()
                if (enabled) {
                    val hour = userPreferences.notificationHour.first()
                    val minute = userPreferences.notificationMinute.first()
                    notificationHelper.scheduleReminder(hour, minute)
                }
            }
        }
    }
}
