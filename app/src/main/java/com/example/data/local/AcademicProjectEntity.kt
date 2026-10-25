package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity specifically for Academic Projects and Major Capstones.
 * Tracks deliverables, tech stacks, milestones progress, GitHub links, and submission deadlines.
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
    val status: String = "IN_PROGRESS", // "PLANNING", "IN_PROGRESS", "REVIEW", "COMPLETED"
    val weightage: String = "25% Capstone Project",
    val githubUrl: String = "https://github.com/rahul-dtu/distributed-cache",
    val teamMembers: String = "Rahul Sharma, Sneha Patel, Vikram Joshi",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Int
        get() = if (totalMilestones <= 0) 100 else ((milestonesCompleted.toDouble() / totalMilestones) * 100).toInt().coerceIn(0, 100)

    val progressFraction: Float
        get() = (progressPercentage / 100f).coerceIn(0f, 1f)

    fun toggleCompleted(): AcademicProjectEntity {
        val nextCompleted = !isCompleted
        return copy(
            isCompleted = nextCompleted,
            status = if (nextCompleted) "COMPLETED" else "IN_PROGRESS",
            milestonesCompleted = if (nextCompleted) totalMilestones else (totalMilestones / 2).coerceAtLeast(1)
        )
    }
}
