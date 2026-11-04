package com.example.worker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Background Service that notifies students via local notifications
 * 24 hours before a project or assignment deadline, sourced from Room database.
 */
class ProjectAssignmentDeadlineService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val forceNotify = intent?.getBooleanExtra(EXTRA_FORCE_NOTIFY, false) ?: false

        Log.d(TAG, "ProjectAssignmentDeadlineService started (startId: $startId, force: $forceNotify)")

        serviceScope.launch {
            try {
                val notifiedCount = ProjectAssignment24hNotificationHelper.checkAndNotifyUpcomingDeadlines(
                    context = applicationContext,
                    forceNotify = forceNotify
                )
                Log.d(TAG, "Completed 24h deadline scan. Dispatched $notifiedCount local notification(s).")
            } catch (e: Exception) {
                Log.e(TAG, "Error executing 24h deadline notification scan: ${e.message}", e)
            } finally {
                // Background service finishes its job and shuts down cleanly
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "ProjectAssignmentDeadlineService destroyed")
    }

    companion object {
        private const val TAG = "DeadlineService"
        const val ACTION_CHECK_24H_DEADLINES = "com.example.ACTION_CHECK_24H_DEADLINES"
        const val EXTRA_FORCE_NOTIFY = "EXTRA_FORCE_NOTIFY"

        /**
         * Starts the background service to scan the Room database and dispatch 24h alerts.
         */
        fun startDeadlineScan(context: Context, forceNotify: Boolean = false) {
            val intent = Intent(context, ProjectAssignmentDeadlineService::class.java).apply {
                action = ACTION_CHECK_24H_DEADLINES
                putExtra(EXTRA_FORCE_NOTIFY, forceNotify)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start service directly, falling back to WorkManager: ${e.message}")
                DeadlineWorkScheduler.triggerImmediate24HourCheck(context)
            }
        }
    }
}
