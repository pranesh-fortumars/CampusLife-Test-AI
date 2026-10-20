package com.example.data.repository

import com.example.data.MockData
import com.example.data.local.CourseGradeEntity
import com.example.data.local.GradeDao
import com.example.model.CourseGrade
import com.example.model.GradeScale
import com.example.model.SemesterGrades
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository layer abstracting GradeDao database access.
 */
class GradeRepository(private val dao: GradeDao) {

    val allGradeEntities: Flow<List<CourseGradeEntity>> = dao.getAllGrades()

    val allGrades: Flow<List<CourseGrade>> = dao.getAllGrades().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getGradesForSemester(semesterNumber: Int): Flow<List<CourseGrade>> {
        return dao.getGradesForSemester(semesterNumber).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertGrade(grade: CourseGrade, semesterNumber: Int, semesterName: String = "Semester $semesterNumber") {
        dao.insertGrade(
            CourseGradeEntity.fromDomain(
                domain = grade,
                semesterNumber = semesterNumber,
                semesterName = semesterName
            )
        )
    }

    suspend fun updateCourseGrade(semesterNumber: Int, courseCode: String, newGrade: String) {
        val existing = dao.getGradeForCourse(courseCode)
        if (existing != null) {
            val updated = existing.copy(
                grade = newGrade.uppercase(),
                gradePoint = GradeScale.getPoint(newGrade)
            )
            dao.updateGrade(updated)
        }
    }

    suspend fun deleteGrade(id: String) {
        dao.deleteGradeById(id)
    }

    suspend fun seedInitialGradesIfEmpty() {
        val count = dao.getGradeCount()
        if (count == 0) {
            val initialEntities = mutableListOf<CourseGradeEntity>()
            MockData.initialSemesterGrades.forEach { sem ->
                sem.courses.forEach { course ->
                    initialEntities.add(
                        CourseGradeEntity(
                            id = course.id,
                            courseCode = course.courseCode,
                            courseName = course.courseName,
                            credits = course.credits,
                            grade = course.grade,
                            gradePoint = course.gradePoint,
                            semesterNumber = sem.semesterNumber,
                            semesterName = sem.semesterName,
                            academicYear = sem.academicYear,
                            isCompleted = course.isCompleted
                        )
                    )
                }
            }
            dao.insertGrades(initialEntities)
        }
    }
}
