package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room Database for the CampusLife application.
 * Manages entities for subjects, attendance records, academic tasks, and semester grades.
 */
@Database(
    entities = [
        SubjectEntity::class,
        AttendanceRecordEntity::class,
        TaskEntity::class,
        CourseGradeEntity::class,
        AcademicProjectEntity::class,
        AcademicCalendarEventEntity::class,
        CourseScheduleEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao
    abstract fun taskDao(): TaskDao
    abstract fun gradeDao(): GradeDao
    abstract fun projectDao(): AcademicProjectDao
    abstract fun academicCalendarDao(): AcademicCalendarDao
    abstract fun courseScheduleDao(): CourseScheduleDao

    companion object {
        const val DATABASE_NAME = "campuslife_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton database builder logic.
         * Creates a single instance of AppDatabase across the application lifetime,
         * using thread-safe double-checked locking.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Alias for getDatabase to provide convenient singleton access.
         */
        fun getInstance(context: Context): AppDatabase = getDatabase(context)

        /**
         * Singleton provider function that supplies the TaskDao directly.
         * Used for dependency injection into TaskViewModel or repositories.
         */
        fun provideTaskDao(context: Context): TaskDao {
            return getDatabase(context).taskDao()
        }

        /**
         * Singleton provider function that supplies the AttendanceDao directly.
         */
        fun provideAttendanceDao(context: Context): AttendanceDao {
            return getDatabase(context).attendanceDao()
        }

        /**
         * Singleton provider function that supplies the GradeDao directly.
         */
        fun provideGradeDao(context: Context): GradeDao {
            return getDatabase(context).gradeDao()
        }

        /**
         * Singleton provider function that supplies the AcademicProjectDao directly.
         */
        fun provideProjectDao(context: Context): AcademicProjectDao {
            return getDatabase(context).projectDao()
        }

        /**
         * Singleton provider function that supplies the AcademicCalendarDao directly.
         */
         fun provideAcademicCalendarDao(context: Context): AcademicCalendarDao {
             return getDatabase(context).academicCalendarDao()
         }

        /**
         * Singleton provider function that supplies the CourseScheduleDao directly.
         */
        fun provideCourseScheduleDao(context: Context): CourseScheduleDao {
            return getDatabase(context).courseScheduleDao()
        }
    }
}

/**
 * CampusLifeDatabase typealias for AppDatabase to support canonical domain naming.
 */
typealias CampusLifeDatabase = AppDatabase

/**
 * Singleton database provider object for dependency injection across ViewModels.
 */
object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase = AppDatabase.getDatabase(context)
    fun provideTaskDao(context: Context): TaskDao = AppDatabase.provideTaskDao(context)
    fun provideAttendanceDao(context: Context): AttendanceDao = AppDatabase.provideAttendanceDao(context)
    fun provideGradeDao(context: Context): GradeDao = AppDatabase.provideGradeDao(context)
    fun provideProjectDao(context: Context): AcademicProjectDao = AppDatabase.provideProjectDao(context)
    fun provideAcademicCalendarDao(context: Context): AcademicCalendarDao = AppDatabase.provideAcademicCalendarDao(context)
    fun provideCourseScheduleDao(context: Context): CourseScheduleDao = AppDatabase.provideCourseScheduleDao(context)
}
