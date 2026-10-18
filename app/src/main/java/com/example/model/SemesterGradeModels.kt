package com.example.model

import java.util.Locale
import java.util.UUID

/**
 * Standard grade letter definitions and their corresponding grade point values.
 */
object GradeScale {
    val gradePoints: Map<String, Double> = mapOf(
        "O" to 10.0,
        "A+" to 9.0,
        "A" to 8.0,
        "B+" to 7.0,
        "B" to 6.0,
        "C" to 5.0,
        "P" to 4.0,
        "F" to 0.0
    )

    fun getPoint(grade: String): Double {
        return gradePoints[grade.trim().uppercase()] ?: 8.0
    }

    val availableGrades = listOf("O", "A+", "A", "B+", "B", "C", "P", "F")
}

/**
 * Data model for a course grade in a semester.
 */
data class CourseGrade(
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val credits: Int = 4,
    val grade: String = "A+",
    val gradePoint: Double = GradeScale.getPoint(grade),
    val isCompleted: Boolean = true
) {
    val creditPoints: Double
        get() = credits * gradePoint
}

/**
 * Data model storing grades and course results for an individual academic semester.
 */
data class SemesterGrades(
    val semesterNumber: Int,
    val semesterName: String,
    val academicYear: String,
    val courses: List<CourseGrade> = emptyList(),
    val isCurrentSemester: Boolean = false
) {
    val totalCredits: Int
        get() = courses.sumOf { it.credits }

    val totalEarnedPoints: Double
        get() = courses.sumOf { it.creditPoints }

    val sgpa: Double
        get() = if (totalCredits <= 0) 0.0 else totalEarnedPoints / totalCredits.toDouble()

    val formattedSgpa: String
        get() = String.format(Locale.US, "%.2f", sgpa)
}

/**
 * Data model that calculates and aggregates cumulative GPA (CGPA) across all completed semesters.
 */
data class AcademicGpaSummary(
    val semesters: List<SemesterGrades>
) {
    val completedSemesters: List<SemesterGrades>
        get() = semesters.filter { !it.isCurrentSemester && it.totalCredits > 0 }

    val totalCreditsEarned: Int
        get() = completedSemesters.sumOf { it.totalCredits }

    val totalEarnedPoints: Double
        get() = completedSemesters.sumOf { it.totalEarnedPoints }

    /**
     * Cumulative GPA (CGPA) calculated as Total Earned Grade Points / Total Credits.
     */
    val cumulativeGpa: Double
        get() = if (totalCreditsEarned <= 0) 0.0 else totalEarnedPoints / totalCreditsEarned.toDouble()

    val formattedCgpa: String
        get() = String.format(Locale.US, "%.2f", cumulativeGpa)

    /**
     * Equivalent US 4.0 scale GPA representation.
     */
    val equivalentUsGpa: Double
        get() = (cumulativeGpa / 10.0) * 4.0

    val formattedUsGpa: String
        get() = String.format(Locale.US, "%.2f", equivalentUsGpa)

    /**
     * AICTE official percentage conversion: (CGPA * 9.5)%.
     */
    val percentageEquivalent: Double
        get() = cumulativeGpa * 9.5

    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", percentageEquivalent)

    val highestSgpa: Double
        get() = completedSemesters.maxOfOrNull { it.sgpa } ?: cumulativeGpa

    val lowestSgpa: Double
        get() = completedSemesters.minOfOrNull { it.sgpa } ?: cumulativeGpa

    /**
     * Predict the overall CGPA given projected credits and SGPA for the current/next semester.
     */
    fun calculateProjectedCgpa(activeSemesterCredits: Int, projectedSgpa: Double): Double {
        val totalCombinedCredits = totalCreditsEarned + activeSemesterCredits
        if (totalCombinedCredits <= 0) return cumulativeGpa
        val totalCombinedPoints = totalEarnedPoints + (activeSemesterCredits * projectedSgpa)
        return totalCombinedPoints / totalCombinedCredits.toDouble()
    }

    /**
     * Calculate SGPA required in remaining credits to achieve a target CGPA.
     */
    fun calculateRequiredSgpaForTarget(remainingCredits: Int, targetCgpa: Double): Double {
        val totalFinalCredits = totalCreditsEarned + remainingCredits
        if (remainingCredits <= 0) return targetCgpa
        val targetTotalPoints = totalFinalCredits * targetCgpa
        val pointsNeeded = targetTotalPoints - totalEarnedPoints
        return pointsNeeded / remainingCredits.toDouble()
    }
}
