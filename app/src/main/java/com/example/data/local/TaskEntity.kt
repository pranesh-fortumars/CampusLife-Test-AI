package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AcademicTask
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val type: String = "ASSIGNMENT", // "ASSIGNMENT", "PROJECT", "LAB", "EXAM_PREP"
    val dueDate: String,             // e.g. "Today", "Tomorrow, Oct 25", "Oct 29"
    val dueTime: String = "11:59 PM",
    val priority: String = "HIGH",   // "URGENT", "HIGH", "MEDIUM", "LOW"
    val weightage: String = "15% Internal",
    val isCompleted: Boolean = false,
    val progressPercent: Int = 0,    // 0 to 100
    val faculty: String = "",
    val note: String = "",
    val dueTimestamp: Long = System.currentTimeMillis() + 86400000L,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AcademicTask {
        return AcademicTask(
            id = id,
            title = title,
            subjectCode = subjectCode,
            subjectName = subjectName,
            type = type,
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            weightage = weightage,
            isCompleted = isCompleted,
            progressPercent = progressPercent,
            faculty = faculty,
            note = note,
            dueTimestamp = dueTimestamp,
            createdAt = createdAt
        )
    }
}
