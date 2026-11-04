package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager CoroutineWorker for periodic or scheduled 24-hour deadline scans.
 * Queries Room database for upcoming projects and assignments and notifies students.
 */
class ProjectAssignment24hWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val forceNotify = inputData.getBoolean("FORCE_NOTIFY", false)
            Log.d(TAG, "ProjectAssignment24hWorker running background check from Room (force: $forceNotify)...")

            val count = ProjectAssignment24hNotificationHelper.checkAndNotifyUpcomingDeadlines(
                context = applicationContext,
                forceNotify = forceNotify
            )

            Log.d(TAG, "ProjectAssignment24hWorker finished successfully. Dispatched $count notification(s).")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ProjectAssignment24hWorker encountered error: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "Project24hWorker"
    }
}
