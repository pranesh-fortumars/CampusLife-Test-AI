package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY code ASC")
    suspend fun getAllCoursesList(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR instructor LIKE '%' || :query || '%' OR room LIKE '%' || :query || '%' ORDER BY code ASC")
    fun searchCourses(query: String): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCount(): Int
}

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials ORDER BY id DESC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE title LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchMaterials(query: String): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMaterials(materials: List<StudyMaterialEntity>)

    @Update
    suspend fun updateMaterial(material: StudyMaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: StudyMaterialEntity)

    @Query("SELECT COUNT(*) FROM study_materials")
    suspend fun getCount(): Int
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY isPinned DESC, id DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY isPinned DESC, id DESC")
    fun searchPosts(query: String): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPosts(posts: List<CommunityPostEntity>)

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Query("UPDATE community_posts SET upvotes = upvotes + 1 WHERE id = :postId")
    suspend fun upvotePost(postId: Long)

    @Delete
    suspend fun deletePost(post: CommunityPostEntity)

    @Query("SELECT COUNT(*) FROM community_posts")
    suspend fun getCount(): Int
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%' ORDER BY isCompleted ASC, id DESC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getCount(): Int

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY id ASC")
    suspend fun getPendingTasksList(): List<TaskEntity>
}


@Dao
interface AcademicCalendarDao {
    @Query("SELECT * FROM academic_calendar_events ORDER BY daysRemaining ASC, id ASC")
    fun getAllEvents(): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE type = :type ORDER BY daysRemaining ASC, id ASC")
    fun getEventsByType(type: String): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' OR locationOrVenue LIKE '%' || :query || '%' ORDER BY daysRemaining ASC")
    fun searchEvents(query: String): Flow<List<AcademicCalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AcademicCalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<AcademicCalendarEventEntity>)

    @Update
    suspend fun updateEvent(event: AcademicCalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: AcademicCalendarEventEntity)

    @Query("UPDATE academic_calendar_events SET isReminderSet = :isReminderSet WHERE id = :id")
    suspend fun updateReminder(id: Long, isReminderSet: Boolean)

    @Query("SELECT * FROM academic_calendar_events WHERE type = 'EXAM' AND daysRemaining >= 0 ORDER BY daysRemaining ASC, targetTimestampMs ASC LIMIT 1")
    fun getNextExam(): Flow<AcademicCalendarEventEntity?>

    @Query("SELECT * FROM academic_calendar_events WHERE type = 'EXAM' ORDER BY daysRemaining ASC, id ASC")
    fun getAllExams(): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT COUNT(*) FROM academic_calendar_events")
    suspend fun getCount(): Int
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM academic_semesters ORDER BY startTimestampMs DESC")
    fun getAllSemesters(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM academic_semesters WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentSemester(): Flow<SemesterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: SemesterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSemesters(semesters: List<SemesterEntity>)

    @Update
    suspend fun updateSemester(semester: SemesterEntity)

    @Delete
    suspend fun deleteSemester(semester: SemesterEntity)

    @Query("UPDATE academic_semesters SET isCurrent = 0")
    suspend fun clearCurrentSemester()

    @Query("UPDATE academic_semesters SET isCurrent = 1 WHERE id = :semesterId")
    suspend fun markSemesterAsCurrent(semesterId: Long)

    @Transaction
    suspend fun setCurrentSemester(semesterId: Long) {
        clearCurrentSemester()
        markSemesterAsCurrent(semesterId)
    }

    @Query("SELECT COUNT(*) FROM academic_semesters")
    suspend fun getCount(): Int
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestampMs DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE tag = :tag ORDER BY timestampMs DESC")
    fun getSessionsByTag(tag: String): Flow<List<StudySessionEntity>>

    @Query("SELECT DISTINCT tag FROM study_sessions")
    fun getDistinctTags(): Flow<List<String>>

    @Query("SELECT courseCode, SUM(durationMinutes) as totalMinutes, COUNT(*) as sessionCount FROM study_sessions GROUP BY courseCode ORDER BY totalMinutes DESC")
    fun getStudyStatsPerSubject(): Flow<List<SubjectStudyStats>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE mode = 'FOCUS'")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE courseCode = :courseCode")
    fun getStudyMinutesForCourse(courseCode: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<StudySessionEntity>)

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getCount(): Int

    @Query("SELECT * FROM study_sessions WHERE timestampMs >= :sinceMs ORDER BY timestampMs DESC")
    suspend fun getSessionsSince(sinceMs: Long): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions ORDER BY timestampMs DESC")
    suspend fun getAllSessionsList(): List<StudySessionEntity>
}

@Dao
interface SubjectMasteryDao {
    @Query("SELECT * FROM subject_mastery_records ORDER BY courseCode ASC, weekNumber ASC")
    fun getAllMasteryRecords(): Flow<List<SubjectMasteryEntity>>

    @Query("SELECT * FROM subject_mastery_records WHERE courseCode = :courseCode ORDER BY weekNumber ASC")
    fun getMasteryRecordsForCourse(courseCode: String): Flow<List<SubjectMasteryEntity>>

    @Query("SELECT * FROM subject_mastery_records ORDER BY courseCode ASC, weekNumber ASC")
    suspend fun getAllMasteryRecordsList(): List<SubjectMasteryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasteryRecord(record: SubjectMasteryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMasteryRecords(records: List<SubjectMasteryEntity>)

    @Query("SELECT COUNT(*) FROM subject_mastery_records")
    suspend fun getCount(): Int

    @Query("UPDATE subject_mastery_records SET masteryPercentage = :newMastery, studyHoursInWeek = :hours WHERE courseCode = :courseCode AND weekNumber = :weekNumber")
    suspend fun updateMasteryScore(courseCode: String, weekNumber: Int, newMastery: Double, hours: Double)

    @Query("DELETE FROM subject_mastery_records WHERE courseCode = :courseCode")
    suspend fun deleteMasteryForCourse(courseCode: String)
}

@Dao
interface WeeklyDayNoteDao {
    @Query("SELECT * FROM weekly_day_notes ORDER BY updatedTimestampMs DESC")
    fun getAllNotes(): Flow<List<WeeklyDayNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(dayNote: WeeklyDayNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<WeeklyDayNoteEntity>)

    @Query("DELETE FROM weekly_day_notes WHERE dayKey = :dayKey")
    suspend fun deleteNoteForDay(dayKey: String)

    @Query("SELECT COUNT(*) FROM weekly_day_notes")
    suspend fun getCount(): Int
}




