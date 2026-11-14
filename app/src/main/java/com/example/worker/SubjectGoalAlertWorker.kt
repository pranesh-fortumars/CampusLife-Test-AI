package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager worker that evaluates Room study sessions against each subject's custom
 * weekly goal and dispatches a local notification if the student is falling behind.
 */
class SubjectGoalAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val forceNotify = inputData.getBoolean("FORCE_NOTIFY", false)
            val notified = SubjectGoalNotificationHelper.checkAndNotifyIfBehind(
                context = applicationContext,
                forceNotify = forceNotify
            )
            Log.d(TAG, "SubjectGoalAlertWorker complete (notified=$notified, forceNotify=$forceNotify)")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SubjectGoalAlertWorker failed: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SubjectGoalAlertWorker"
    }
}
