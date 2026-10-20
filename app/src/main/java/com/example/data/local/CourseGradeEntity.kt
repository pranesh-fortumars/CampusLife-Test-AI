package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CourseGrade
import com.example.model.GradeScale
import java.util.UUID

/**
 * Room database entity representing a course grade in a given semester.
 */
@Entity(tableName = "grades")
data class CourseGradeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val credits: Int = 4,
    val grade: String = "A+",
    val gradePoint: Double = GradeScale.getPoint(grade),
    val semesterNumber: Int = 5,
    val semesterName: String = "Semester 5",
    val academicYear: String = "2025-2026",
    val marksObtained: Double? = null,
    val maxMarks: Double? = 100.0,
    val isCompleted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): CourseGrade {
        return CourseGrade(
            id = id,
            courseCode = courseCode,
            courseName = courseName,
            credits = credits,
            grade = grade,
            gradePoint = gradePoint,
            isCompleted = isCompleted
        )
    }

    companion object {
        fun fromDomain(
            domain: CourseGrade,
            semesterNumber: Int = 5,
            semesterName: String = "Semester 5",
            academicYear: String = "2025-2026"
        ): CourseGradeEntity {
            return CourseGradeEntity(
                id = domain.id,
                courseCode = domain.courseCode,
                courseName = domain.courseName,
                credits = domain.credits,
                grade = domain.grade,
                gradePoint = domain.gradePoint,
                semesterNumber = semesterNumber,
                semesterName = semesterName,
                academicYear = academicYear,
                isCompleted = domain.isCompleted
            )
        }
    }
}
