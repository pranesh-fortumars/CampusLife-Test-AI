package com.example.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Singleton scheduler managing WorkManager triggers for upcoming task notifications.
 */
object TaskNotificationScheduler {

    const val PERIODIC_WORK_NAME = "campus_life_upcoming_tasks_periodic_worker"
    const val IMMEDIATE_WORK_NAME = "campus_life_upcoming_tasks_immediate_worker"

    /**
     * Schedules periodic background execution (every 4 hours by default, minimum 15 mins for WorkManager)
     * to check for approaching deadlines in the database.
     */
    fun schedulePeriodicTaskReminder(
        context: Context,
        intervalHours: Long = 4
    ) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<UpcomingTasksReminderWorker>(
            intervalHours, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag("upcoming_tasks_tag")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    /**
     * Triggers an immediate one-time WorkManager task to check the Room database
     * and alert the user right away for testing or upon inserting new urgent tasks.
     */
    fun triggerImmediateDueCheck(context: Context) {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<UpcomingTasksReminderWorker>()
            .addTag("immediate_task_check")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
    }

    /**
     * Cancels scheduled periodic reminders.
     */
    fun cancelPeriodicReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }
}
