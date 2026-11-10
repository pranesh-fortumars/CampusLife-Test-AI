package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager CoroutineWorker for recurring Pomodoro session reminders
 * driven by the student's weekly study goals in the Room database.
 */
class PomodoroGoalReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val forceNotify = inputData.getBoolean("FORCE_NOTIFY", false)
            Log.d(TAG, "PomodoroGoalReminderWorker executing background goal evaluation (forceNotify=$forceNotify)...")

            val dispatched = PomodoroGoalNotificationHelper.checkAndNotifyGoalReminders(
                context = applicationContext,
                forceNotify = forceNotify
            )

            Log.d(TAG, "PomodoroGoalReminderWorker finished. Dispatched notification: $dispatched")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "PomodoroGoalReminderWorker encountered error: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PomodoroGoalWorker"
    }
}
