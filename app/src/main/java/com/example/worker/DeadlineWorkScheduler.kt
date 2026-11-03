package com.example.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object DeadlineWorkScheduler {

    private const val UNIQUE_PERIODIC_WORK_NAME = "assignment_deadline_reminders"
    private const val UNIQUE_IMMEDIATE_WORK_NAME = "immediate_assignment_deadline_check"

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
    }
}
