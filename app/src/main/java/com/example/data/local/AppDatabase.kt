package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CourseEntity::class,
        StudyMaterialEntity::class,
        CommunityPostEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_life_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).populateInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        if (courseDao().getCount() == 0) {
            courseDao().insertAllCourses(
                listOf(
                    CourseEntity(
                        code = "CS201",
                        name = "Data Structures & Algorithms",
                        instructor = "Dr. Sarah Mitchell",
                        room = "Science Complex Hall 3B",
                        dayOfWeek = "Monday, Wednesday",
                        timeSlot = "10:00 AM - 11:30 AM",
                        attendedClasses = 16,
                        totalClasses = 18
                    ),
                    CourseEntity(
                        code = "MATH204",
                        name = "Discrete Mathematics & Graph Theory",
                        instructor = "Prof. Robert Chen",
                        room = "Euler Math Wing Room 104",
                        dayOfWeek = "Tuesday, Thursday",
                        timeSlot = "01:00 PM - 02:30 PM",
                        attendedClasses = 14,
                        totalClasses = 15
                    ),
                    CourseEntity(
                        code = "CS310",
                        name = "Operating Systems & Concurrency",
                        instructor = "Dr. Alan Vance",
                        room = "Turing Lab Room 202",
                        dayOfWeek = "Monday, Friday",
                        timeSlot = "02:45 PM - 04:15 PM",
                        attendedClasses = 11,
                        totalClasses = 15 // Borderline ~73% -> shows warning
                    ),
                    CourseEntity(
                        code = "PHY102",
                        name = "Applied Electromagnetism",
                        instructor = "Dr. Helen Rostova",
                        room = "Physics Auditorium 1",
                        dayOfWeek = "Wednesday",
                        timeSlot = "08:30 AM - 10:00 AM",
                        attendedClasses = 13,
                        totalClasses = 14
                    ),
                    CourseEntity(
                        code = "ENG215",
                        name = "Technical Writing & Research",
                        instructor = "Prof. Elena Ross",
                        room = "Humanities Block Room 405",
                        dayOfWeek = "Thursday",
                        timeSlot = "11:00 AM - 12:30 PM",
                        attendedClasses = 9,
                        totalClasses = 10
                    )
                )
            )
        }

        if (studyMaterialDao().getCount() == 0) {
            studyMaterialDao().insertAllMaterials(
                listOf(
                    StudyMaterialEntity(
                        title = "Trees, Heaps & AVL Balanced Trees Summary",
                        courseCode = "CS201",
                        type = "PDF",
                        fileSize = "2.8 MB",
                        uploader = "Dr. Sarah Mitchell",
                        tags = "data structures, trees, b-tree, exam prep, algorithms",
                        isDownloaded = true
                    ),
                    StudyMaterialEntity(
                        title = "Graph Traversal BFS & Dijkstra Cheat Sheet",
                        courseCode = "CS201",
                        type = "Cheat Sheet",
                        fileSize = "1.1 MB",
                        uploader = "Peer Note Sharer",
                        tags = "graphs, dijkstra, shortest path, formulas"
                    ),
                    StudyMaterialEntity(
                        title = "Discrete Probability & Combinatorics Practice Set",
                        courseCode = "MATH204",
                        type = "Past Paper",
                        fileSize = "3.4 MB",
                        uploader = "Prof. Robert Chen",
                        tags = "math, probability, permutation, midterm solutions"
                    ),
                    StudyMaterialEntity(
                        title = "CPU Scheduling & Deadlock Prevention Slides",
                        courseCode = "CS310",
                        type = "Slides",
                        fileSize = "5.2 MB",
                        uploader = "Dr. Alan Vance",
                        tags = "operating systems, mutex, semaphores, deadlock, lecture"
                    ),
                    StudyMaterialEntity(
                        title = "Maxwell's Equations Formula Quick Sheet",
                        courseCode = "PHY102",
                        type = "Cheat Sheet",
                        fileSize = "890 KB",
                        uploader = "Physics Club",
                        tags = "physics, electromagnetism, formulas, quiz"
                    ),
                    StudyMaterialEntity(
                        title = "IEEE Style Paper Formatting Guide",
                        courseCode = "ENG215",
                        type = "PDF",
                        fileSize = "1.5 MB",
                        uploader = "Prof. Elena Ross",
                        tags = "writing, research paper, citations, bibliography"
                    )
                )
            )
        }

        if (communityPostDao().getCount() == 0) {
            communityPostDao().insertAllPosts(
                listOf(
                    CommunityPostEntity(
                        author = "Devon Miles (Senior)",
                        courseCode = "CS201",
                        title = "Best way to visualize Red-Black tree rotations?",
                        content = "Struggling with the left-right rotation cases for Dr. Mitchell's midterm. Any recommended interactive visualizer or intuitive tip?",
                        tag = "Question",
                        upvotes = 24,
                        repliesCount = 8,
                        timeAgo = "2h ago",
                        isPinned = true
                    ),
                    CommunityPostEntity(
                        author = "Priya Patel",
                        courseCode = "CS310",
                        title = "Pro-tip: Virtual Memory TLB cache hit rates",
                        content = "Remember that 2-level paging requires 3 memory accesses on TLB miss. Don't lose easy points on Friday's quiz!",
                        tag = "Study Tip",
                        upvotes = 39,
                        repliesCount = 5,
                        timeAgo = "4h ago"
                    ),
                    CommunityPostEntity(
                        author = "Marcus Brody",
                        courseCode = "MATH204",
                        title = "Library Group Study session for Discrete Math",
                        content = "Meeting at Science Library 2nd floor quiet room tomorrow at 4 PM to solve the inductive proofs sheet together. All welcome!",
                        tag = "Group Study",
                        upvotes = 17,
                        repliesCount = 12,
                        timeAgo = "6h ago"
                    ),
                    CommunityPostEntity(
                        author = "Sofia Nguyen",
                        courseCode = "CS201",
                        title = "Shared my Handwritten Dynamic Programming cheat sheet!",
                        content = "Uploaded the 0/1 Knapsack, Coin Change, and LCS recurrence formulas to the study materials section. Check it out!",
                        tag = "Notes",
                        upvotes = 56,
                        repliesCount = 15,
                        timeAgo = "1d ago"
                    )
                )
            )
        }

        if (taskDao().getCount() == 0) {
            taskDao().insertAllTasks(
                listOf(
                    TaskEntity(
                        title = "Submit Heap Sort Lab Implementation",
                        courseCode = "CS201",
                        dueDate = "Tomorrow, 11:59 PM",
                        priority = "HIGH",
                        isCompleted = false,
                        details = "Submit GitHub repository link on the university portal with unit test outputs."
                    ),
                    TaskEntity(
                        title = "Read Operating Systems Chapter 7 (Deadlocks)",
                        courseCode = "CS310",
                        dueDate = "Friday, 2:00 PM",
                        priority = "MEDIUM",
                        isCompleted = false,
                        details = "Prepare 3 questions on banker's algorithm for discussion."
                    ),
                    TaskEntity(
                        title = "Discrete Math Assignment #4 Problem Set",
                        courseCode = "MATH204",
                        dueDate = "Nov 06, 5:00 PM",
                        priority = "HIGH",
                        isCompleted = false,
                        details = "Complete induction proofs and pigeonhole principle exercises."
                    ),
                    TaskEntity(
                        title = "Draft Literature Review for Research Paper",
                        courseCode = "ENG215",
                        dueDate = "Nov 10, 11:59 PM",
                        priority = "LOW",
                        isCompleted = false,
                        details = "Cite at least 5 peer-reviewed papers using IEEE format."
                    )
                )
            )
        }
    }
}
