package com.example.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object DeadlineWorkScheduler {

    const val UNIQUE_PERIODIC_WORK_NAME = "periodic_deadline_checks"
    const val UNIQUE_IMMEDIATE_WORK_NAME = "immediate_deadline_check"
    private const val UNIQUE_24H_PERIODIC_WORK_NAME = "project_assignment_24h_periodic_worker"
    private const val UNIQUE_24H_IMMEDIATE_WORK_NAME = "project_assignment_24h_immediate_worker"

    fun schedulePeriodicDeadlineChecks(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<AssignmentDeadlineWorker>(
            4, TimeUnit.HOURS, // Periodic checks every 4 hours
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        // Also schedule dedicated 24h project/assignment deadline checks
        schedule24HourPeriodicChecks(context)
    }

    /**
     * Schedules periodic background monitoring specifically for project and assignment
     * deadlines 24 hours in advance.
     */
    fun schedule24HourPeriodicChecks(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val request = PeriodicWorkRequestBuilder<ProjectAssignment24hWorker>(
            2, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("24h_project_assignment_reminders")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_24H_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Schedules an exact one-time background trigger primed to fire precisely 24 hours
     * before a specific project or assignment due timestamp.
     */
    fun scheduleExact24HourReminder(context: Context, task: com.example.data.local.TaskEntity) {
        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24L * 60 * 60 * 1000L
        val triggerTargetMs = task.dueTimestamp - twentyFourHoursMs
        val delayMs = maxOf(0L, triggerTargetMs - now)

        val workData = workDataOf("TASK_ID" to task.id, "FORCE_NOTIFY" to true)

        val oneTimeRequest = OneTimeWorkRequestBuilder<ProjectAssignment24hWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .addTag("exact_24h_reminder_${task.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "exact_24h_${task.id}",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    /**
     * Triggers an immediate one-time 24h deadline check against the Room database.
     */
    fun triggerImmediate24HourCheck(context: Context, forceNotify: Boolean = false) {
        val workData = workDataOf("FORCE_NOTIFY" to forceNotify)
        val request = OneTimeWorkRequestBuilder<ProjectAssignment24hWorker>()
            .setInputData(workData)
            .addTag("immediate_24h_deadline_check")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_24H_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun triggerImmediateDeadlineCheck(context: Context) {
        val immediateWorkRequest = OneTimeWorkRequestBuilder<AssignmentDeadlineWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateWorkRequest
        )
    }

    fun cancelDeadlineChecks(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_24H_PERIODIC_WORK_NAME)
    }

    /**
     * Schedules recurring Pomodoro study goal reminders based on weekly goals.
     */
    fun schedulePeriodicPomodoroReminders(context: Context) {
        PomodoroGoalNotificationHelper.createNotificationChannel(context)
        if (PomodoroGoalNotificationHelper.isRemindersEnabled(context)) {
            val interval = PomodoroGoalNotificationHelper.getReminderIntervalHours(context)
            PomodoroGoalNotificationHelper.schedulePeriodicGoalReminders(context, intervalHours = interval)
        }
    }

    /**
     * Triggers an immediate one-time Pomodoro study goal reminder evaluation.
     */
    fun triggerImmediatePomodoroReminder(context: Context, forceNotify: Boolean = true) {
        PomodoroGoalNotificationHelper.triggerImmediateReminder(context, forceNotify = forceNotify)
    }

    fun cancelPomodoroReminders(context: Context) {
        PomodoroGoalNotificationHelper.cancelPeriodicGoalReminders(context)
    }
}
