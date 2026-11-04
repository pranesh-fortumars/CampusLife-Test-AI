package com.example.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver triggered on system boot or custom deadline check alarm
 * to prime the 24-hour deadline background service.
 */
class DeadlineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "DeadlineAlarmReceiver received action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            ACTION_CHECK_24H_DEADLINES -> {
                // Ensure periodic WorkManager checks are active
                DeadlineWorkScheduler.schedule24HourPeriodicChecks(context)
                // Start background service to check Room database immediately
                ProjectAssignmentDeadlineService.startDeadlineScan(context)
            }
        }
    }

    companion object {
        private const val TAG = "DeadlineAlarmReceiver"
        const val ACTION_CHECK_24H_DEADLINES = "com.example.ACTION_CHECK_24H_DEADLINES"
    }
}
