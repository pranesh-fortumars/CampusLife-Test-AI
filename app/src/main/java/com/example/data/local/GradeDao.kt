package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for course grades in Room.
 */
@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY semesterNumber ASC, courseCode ASC")
    fun getAllGrades(): Flow<List<CourseGradeEntity>>

    @Query("SELECT * FROM grades WHERE semesterNumber = :semesterNumber ORDER BY courseCode ASC")
    fun getGradesForSemester(semesterNumber: Int): Flow<List<CourseGradeEntity>>

    @Query("SELECT * FROM grades WHERE courseCode = :courseCode LIMIT 1")
    suspend fun getGradeForCourse(courseCode: String): CourseGradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: CourseGradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<CourseGradeEntity>)

    @Update
    suspend fun updateGrade(grade: CourseGradeEntity)

    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteGradeById(id: String)

    @Query("DELETE FROM grades WHERE semesterNumber = :semesterNumber")
    suspend fun deleteGradesForSemester(semesterNumber: Int)

    @Query("SELECT COUNT(*) FROM grades")
    suspend fun getGradeCount(): Int
}
