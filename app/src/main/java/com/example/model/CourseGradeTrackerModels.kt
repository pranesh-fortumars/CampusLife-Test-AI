package com.example.model

import java.util.Locale
import java.util.UUID

/**
 * Category of academic assessment within a course.
 */
enum class AssessmentType(val displayName: String, val iconEmoji: String, val defaultWeight: Double) {
    ASSIGNMENT("Assignment", "📝", 15.0),
    EXAM("Mid-Term Exam", "📄", 25.0),
    LAB("Lab Practicum", "🧪", 20.0),
    QUIZ("Quiz / Test", "⚡", 10.0),
    PROJECT("Course Project", "💻", 20.0),
    FINAL_EXAM("End-Term Final", "🎓", 40.0)
}

/**
 * Individual assessment item with weight percentage and marks.
 */
data class CourseAssessmentItem(
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String = "",
    val title: String,
    val type: AssessmentType = AssessmentType.ASSIGNMENT,
    val weightPercentage: Double = 15.0, // e.g. 15% of final grade
    val scoreEarned: Double = 18.0,
    val maxScore: Double = 20.0,
    val isCompleted: Boolean = true,
    val dueDate: String = "Graded"
) {
    val percentage: Double
        get() = if (maxScore > 0) (scoreEarned / maxScore) * 100.0 else 0.0

    val weightedContribution: Double
        get() = if (maxScore > 0) (scoreEarned / maxScore) * weightPercentage else 0.0

    val formattedScore: String
        get() = "${String.format(Locale.US, "%.1f", scoreEarned)} / ${String.format(Locale.US, "%.1f", maxScore)}"

    val formattedPercentage: String
        get() = "${String.format(Locale.US, "%.1f", percentage)}%"

    val formattedWeight: String
        get() = "${String.format(Locale.US, "%.0f", weightPercentage)}%"
}

/**
 * Aggregated grade and weighted GPA summary for a specific course class.
 */
data class ClassGradeSummary(
    val courseCode: String,
    val courseName: String,
    val credits: Int = 4,
    val faculty: String = "",
    val assessments: List<CourseAssessmentItem> = emptyList()
) {
    val completedAssessments: List<CourseAssessmentItem>
        get() = assessments.filter { it.isCompleted }

    val pendingAssessments: List<CourseAssessmentItem>
        get() = assessments.filter { !it.isCompleted }

    val totalWeightGraded: Double
        get() = completedAssessments.sumOf { it.weightPercentage }

    val totalWeightPending: Double
        get() = pendingAssessments.sumOf { it.weightPercentage }

    val totalWeightAll: Double
        get() = assessments.sumOf { it.weightPercentage }

    val earnedWeightedPoints: Double
        get() = completedAssessments.sumOf { it.weightedContribution }

    /**
     * Current percentage score based on graded assessments so far.
     */
    val currentPercentage: Double
        get() = if (totalWeightGraded > 0) (earnedWeightedPoints / totalWeightGraded) * 100.0 else 0.0

    /**
     * Current GPA on a 10.0 scale for this specific class.
     */
    val currentClassGpa: Double
        get() = when {
            currentPercentage >= 90.0 -> 10.0
            currentPercentage >= 80.0 -> 9.0
            currentPercentage >= 70.0 -> 8.0
            currentPercentage >= 60.0 -> 7.0
            currentPercentage >= 50.0 -> 6.0
            currentPercentage >= 40.0 -> 5.0
            else -> 0.0
        }

    val currentGradeLetter: String
        get() = when {
            currentPercentage >= 90.0 -> "O"
            currentPercentage >= 80.0 -> "A+"
            currentPercentage >= 70.0 -> "A"
            currentPercentage >= 60.0 -> "B+"
            currentPercentage >= 50.0 -> "B"
            currentPercentage >= 40.0 -> "C"
            else -> "F"
        }

    val honorsStanding: String
        get() = when {
            currentClassGpa >= 10.0 -> "Outstanding (O)"
            currentClassGpa >= 9.0 -> "Excellent (A+)"
            currentClassGpa >= 8.0 -> "Very Good (A)"
            currentClassGpa >= 7.0 -> "Good (B+)"
            currentClassGpa >= 6.0 -> "Above Average (B)"
            else -> "Passing Grade"
        }

    val formattedGpa: String
        get() = String.format(Locale.US, "%.2f", currentClassGpa)

    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", currentPercentage)

    /**
     * Calculates required score percentage on remaining pending weight to achieve target grade (e.g. 90% for O).
     */
    fun requiredScoreForTarget(targetPercentage: Double): Double? {
        if (totalWeightPending <= 0.0) return null
        val pointsNeeded = targetPercentage - earnedWeightedPoints
        val requiredPct = (pointsNeeded / totalWeightPending) * 100.0
        return requiredPct.coerceIn(0.0, 100.0)
    }
}

/**
 * Initial sample datasets for realistic class grade tracking.
 */
object CourseGradeMockData {
    val defaultClassesWithAssessments: List<ClassGradeSummary> = listOf(
        ClassGradeSummary(
            courseCode = "CS-302",
            courseName = "Database Management Systems",
            credits = 4,
            faculty = "Dr. Ananya Roy",
            assessments = listOf(
                CourseAssessmentItem(
                    title = "Assignment 1: Relational Algebra & SQL",
                    type = AssessmentType.ASSIGNMENT,
                    weightPercentage = 10.0,
                    scoreEarned = 19.0,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Mid-Term Examination (Theory)",
                    type = AssessmentType.EXAM,
                    weightPercentage = 25.0,
                    scoreEarned = 23.5,
                    maxScore = 25.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Lab Practicum: Query Tuning & B+ Trees",
                    type = AssessmentType.LAB,
                    weightPercentage = 15.0,
                    scoreEarned = 28.0,
                    maxScore = 30.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Surprise Quiz: Normalization (3NF/BCNF)",
                    type = AssessmentType.QUIZ,
                    weightPercentage = 10.0,
                    scoreEarned = 9.5,
                    maxScore = 10.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "End-Term Comprehensive Exam",
                    type = AssessmentType.FINAL_EXAM,
                    weightPercentage = 40.0,
                    scoreEarned = 0.0,
                    maxScore = 100.0,
                    isCompleted = false,
                    dueDate = "Nov 28, 2026"
                )
            )
        ),
        ClassGradeSummary(
            courseCode = "CS-301",
            courseName = "Distributed Systems & Cloud",
            credits = 4,
            faculty = "Prof. K. Muralidhar",
            assessments = listOf(
                CourseAssessmentItem(
                    title = "Assignment 1: Vector Clocks & RPC",
                    type = AssessmentType.ASSIGNMENT,
                    weightPercentage = 15.0,
                    scoreEarned = 17.5,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Mid-Term Examination",
                    type = AssessmentType.EXAM,
                    weightPercentage = 25.0,
                    scoreEarned = 22.0,
                    maxScore = 25.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Raft Consensus Cluster Project",
                    type = AssessmentType.PROJECT,
                    weightPercentage = 20.0,
                    scoreEarned = 19.0,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "End-Term Final Exam",
                    type = AssessmentType.FINAL_EXAM,
                    weightPercentage = 40.0,
                    scoreEarned = 0.0,
                    maxScore = 100.0,
                    isCompleted = false,
                    dueDate = "Dec 04, 2026"
                )
            )
        ),
        ClassGradeSummary(
            courseCode = "CS-304",
            courseName = "Operating Systems",
            credits = 4,
            faculty = "Dr. R. Venkat",
            assessments = listOf(
                CourseAssessmentItem(
                    title = "Assignment 1: Virtual Memory & Paging",
                    type = AssessmentType.ASSIGNMENT,
                    weightPercentage = 15.0,
                    scoreEarned = 18.0,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Mid-Term Exam",
                    type = AssessmentType.EXAM,
                    weightPercentage = 25.0,
                    scoreEarned = 21.0,
                    maxScore = 25.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Kernel Syscall Lab Practicum",
                    type = AssessmentType.LAB,
                    weightPercentage = 20.0,
                    scoreEarned = 19.5,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Final Exam",
                    type = AssessmentType.FINAL_EXAM,
                    weightPercentage = 40.0,
                    scoreEarned = 0.0,
                    maxScore = 100.0,
                    isCompleted = false,
                    dueDate = "Dec 08, 2026"
                )
            )
        ),
        ClassGradeSummary(
            courseCode = "CS-306",
            courseName = "Computer Networks",
            credits = 3,
            faculty = "Prof. Neha Gupta",
            assessments = listOf(
                CourseAssessmentItem(
                    title = "Socket Programming Lab",
                    type = AssessmentType.LAB,
                    weightPercentage = 20.0,
                    scoreEarned = 18.0,
                    maxScore = 20.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "Mid-Term Exam",
                    type = AssessmentType.EXAM,
                    weightPercentage = 30.0,
                    scoreEarned = 26.0,
                    maxScore = 30.0,
                    isCompleted = true,
                    dueDate = "Graded"
                ),
                CourseAssessmentItem(
                    title = "End-Term Final",
                    type = AssessmentType.FINAL_EXAM,
                    weightPercentage = 50.0,
                    scoreEarned = 0.0,
                    maxScore = 100.0,
                    isCompleted = false,
                    dueDate = "Dec 12, 2026"
                )
            )
        )
    )
}
