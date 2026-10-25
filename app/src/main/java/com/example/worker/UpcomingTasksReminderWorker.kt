package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity

/**
 * WorkManager CoroutineWorker triggered periodically or on-demand to check the Room database
 * for tasks with upcoming due dates and post local notifications to remind the student.
 */
class UpcomingTasksReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val taskDao = database.taskDao()

            // Fetch pending tasks from SQLite via Room DAO
            val pendingTasks = taskDao.getPendingTasksList()

            val now = System.currentTimeMillis()
            val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L

            // Filter for upcoming tasks (due within 3 days, or explicitly marked Today/Tomorrow/Urgent)
            val upcomingTasks: List<TaskEntity> = pendingTasks.filter { task ->
                val isSoonText = task.dueDate.contains("Today", ignoreCase = true) ||
                        task.dueDate.contains("Tomorrow", ignoreCase = true)
                val isUrgent = task.priority.equals("URGENT", ignoreCase = true)
                val isDueWithinThreeDays = task.dueTimestamp in now..(now + threeDaysInMillis)

                !task.isCompleted && (isSoonText || isUrgent || isDueWithinThreeDays)
            }.sortedBy { it.dueTimestamp }

            if (upcomingTasks.isNotEmpty()) {
                TaskNotificationHelper.postUpcomingTasksNotification(
                    context = applicationContext,
                    upcomingTasks = upcomingTasks
                )
            }

            Result.success()
        } catch (e: Exception) {
            // Fail gracefully or retry on transient database errors
            Result.retry()
        }
    }
}
