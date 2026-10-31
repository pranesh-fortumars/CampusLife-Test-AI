package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Standard project tracking statuses: Planned, In Progress, Done.
 */
enum class ProjectStatus(val displayName: String, val code: String) {
    PLANNED("Planned", "PLANNED"),
    IN_PROGRESS("In Progress", "IN_PROGRESS"),
    DONE("Done", "DONE");

    companion object {
        fun fromString(value: String): ProjectStatus {
            return when (value.trim().uppercase()) {
                "PLANNED", "PLANNING" -> PLANNED
                "DONE", "COMPLETED", "FINISHED" -> DONE
                else -> IN_PROGRESS
            }
        }
    }
}

/**
 * Room database entity specifically for Academic Projects and Major Capstones.
 * Tracks deliverables, tech stacks, milestones progress, GitHub links, status (Planned, In Progress, Done),
 * and submission deadlines.
 */
@Entity(tableName = "academic_projects")
data class AcademicProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val description: String = "",
    val techStack: String = "Kotlin, Jetpack Compose, Room",
    val deadlineDate: String = "Nov 15, 2026",
    val dueTimestamp: Long = System.currentTimeMillis() + (14 * 86400000L),
    val milestonesCompleted: Int = 1,
    val totalMilestones: Int = 4,
    val status: String = "IN_PROGRESS", // "PLANNED", "IN_PROGRESS", "DONE"
    val weightage: String = "25% Capstone Project",
    val githubUrl: String = "https://github.com/rahul-dtu/distributed-cache",
    val teamMembers: String = "Rahul Sharma, Sneha Patel, Vikram Joshi",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val normalizedStatus: ProjectStatus
        get() = if (isCompleted) ProjectStatus.DONE else ProjectStatus.fromString(status)

    val displayStatus: String
        get() = normalizedStatus.displayName

    val progressPercentage: Int
        get() = if (totalMilestones <= 0) 100 else ((milestonesCompleted.toDouble() / totalMilestones) * 100).toInt().coerceIn(0, 100)

    val progressFraction: Float
        get() = (progressPercentage / 100f).coerceIn(0f, 1f)

    fun getDaysRemaining(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        val diff = dueTimestamp - currentTimeMillis
        return diff / (1000 * 60 * 60 * 24)
    }

    fun getDeadlineStatus(currentTimeMillis: Long = System.currentTimeMillis()): String {
        if (isCompleted || normalizedStatus == ProjectStatus.DONE) return "Completed"
        val days = getDaysRemaining(currentTimeMillis)
        return when {
            days < 0 -> "Overdue by ${-days}d"
            days == 0L -> "Due today"
            days == 1L -> "Due tomorrow"
            else -> "$days days left"
        }
    }

    fun toggleCompleted(): AcademicProjectEntity {
        val nextCompleted = !isCompleted
        return copy(
            isCompleted = nextCompleted,
            status = if (nextCompleted) ProjectStatus.DONE.code else ProjectStatus.IN_PROGRESS.code,
            milestonesCompleted = if (nextCompleted) totalMilestones else (totalMilestones / 2).coerceAtLeast(1)
        )
    }

    fun withStatus(newStatus: ProjectStatus): AcademicProjectEntity {
        val isDone = newStatus == ProjectStatus.DONE
        return copy(
            status = newStatus.code,
            isCompleted = isDone,
            milestonesCompleted = if (isDone) totalMilestones else milestonesCompleted
        )
    }

    fun withDeadline(newDate: String, newTimestamp: Long): AcademicProjectEntity {
        return copy(
            deadlineDate = newDate,
            dueTimestamp = newTimestamp
        )
    }
}

