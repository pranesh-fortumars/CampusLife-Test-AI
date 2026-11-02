package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: String,
    val timeSlot: String,
    val attendedClasses: Int = 12,
    val totalClasses: Int = 14,
    val targetPercent: Double = 75.0
) {
    val attendancePercentage: Double
        get() = if (totalClasses == 0) 100.0 else (attendedClasses.toDouble() / totalClasses) * 100.0

    // Number of classes student can safely miss and still keep >= 75%
    val safeBunks: Int
        get() {
            var bunks = 0
            while (true) {
                val nextTotal = totalClasses + bunks + 1
                val nextPercent = (attendedClasses.toDouble() / nextTotal) * 100.0
                if (nextPercent >= targetPercent) {
                    bunks++
                } else {
                    break
                }
            }
            return bunks
        }
}

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val type: String, // PDF, Slides, Cheat Sheet, Past Paper
    val fileSize: String,
    val uploader: String,
    val tags: String,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val author: String,
    val courseCode: String,
    val title: String,
    val content: String,
    val tag: String, // Question, Tip, Group Study, Notes
    val upvotes: Int = 0,
    val repliesCount: Int = 0,
    val timeAgo: String = "Just now",
    val isPinned: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val dueDate: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val details: String = ""
)
