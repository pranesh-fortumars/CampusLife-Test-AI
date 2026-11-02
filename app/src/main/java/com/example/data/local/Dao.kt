package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

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

    @Query("SELECT COUNT(*) FROM academic_calendar_events")
    suspend fun getCount(): Int
}

