package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase

class AssignmentDeadlineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val pendingTasks = db.taskDao().getPendingTasksList()

            if (pendingTasks.isNotEmpty()) {
                // Prioritize HIGH priority tasks, then earliest
                val prioritizedTask = pendingTasks.firstOrNull { it.priority == "HIGH" } ?: pendingTasks.first()

                // Trigger deadline notification for the prioritized assignment
                TaskNotificationHelper.showDeadlineAlert(applicationContext, prioritizedTask)

                // If multiple assignments are due, post a summary alert
                if (pendingTasks.size > 1) {
                    TaskNotificationHelper.showUpcomingSummaryAlert(
                        applicationContext,
                        pendingTasks.size,
                        prioritizedTask
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
