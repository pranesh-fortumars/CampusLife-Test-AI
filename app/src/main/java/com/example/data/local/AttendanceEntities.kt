package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val code: String,
    val name: String,
    val credits: Int,
    val faculty: String,
    val department: String = "Dept. of Computer Science",
    val attendedClasses: Int,
    val totalClasses: Int,
    val targetPercentage: Double = 75.0,
    val nextClassTime: String = "Today, 10:30 AM",
    val room: String = "CS Lab 304, Block B",
    val isLabIncluded: Boolean = true
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey val id: String,
    val subjectCode: String,
    val subjectName: String,
    val date: String,
    val time: String,
    val type: String, // "ATTENDED", "MISSED", "CANCELLED"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
