package com.example.data.repository

import com.example.data.local.CommunityPostDao
import com.example.data.local.CommunityPostEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for Community Feed study questions and tips.
 * Provides Room database access and reactive data streams.
 */
class CommunityFeedRepository(private val dao: CommunityPostDao) {

    val allPosts: Flow<List<CommunityPostEntity>> = dao.getAllPosts()

    fun getPostsByType(type: String): Flow<List<CommunityPostEntity>> {
        return dao.getPostsByType(type)
    }

    suspend fun insertPost(post: CommunityPostEntity) {
        dao.insertPost(post)
    }

    suspend fun createPost(
        authorName: String,
        authorRole: String,
        postType: String,
        title: String,
        content: String,
        subjectCode: String = "",
        subjectName: String = "",
        tags: String = "#Study"
    ): CommunityPostEntity {
        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.US)
        val formatted = sdf.format(Date())

        val post = CommunityPostEntity(
            authorName = authorName,
            authorRole = authorRole,
            postType = postType,
            title = title,
            content = content,
            subjectCode = subjectCode,
            subjectName = subjectName,
            tags = tags,
            timestamp = System.currentTimeMillis(),
            formattedDate = formatted
        )
        dao.insertPost(post)
        return post
    }

    suspend fun upvotePost(id: String) {
        dao.upvotePost(id)
    }

    suspend fun deletePost(id: String) {
        dao.deletePostById(id)
    }

    suspend fun seedInitialCommunityPostsIfEmpty() {
        val count = dao.getPostCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val hour = 3600000L

            val initialPosts = listOf(
                CommunityPostEntity(
                    id = "post-1",
                    authorName = "Sneha Patel",
                    authorRole = "Teaching Assistant • 4th Year",
                    postType = "TIP",
                    title = "DBMS Indexing Pro-Tip: B+ Tree Clustered vs Secondary Indexes",
                    content = "When optimizing SQL queries for the upcoming Mid-Sem, remember that a table can have only ONE clustered index because data rows are physically ordered by it. For range queries (BETWEEN, <, >), always prefer clustered indexes on sorted keys to avoid random disk I/O seek overhead!",
                    subjectCode = "CS-302",
                    subjectName = "Database Management Systems",
                    tags = "#DBMS #DatabaseTips #ExamPrep",
                    upvotes = 34,
                    answersCount = 8,
                    timestamp = now - (3 * hour),
                    formattedDate = "Today, 7:15 AM",
                    isPinned = true
                ),
                CommunityPostEntity(
                    id = "post-2",
                    authorName = "Vikram Joshi",
                    authorRole = "3rd Year CSE",
                    postType = "QUESTION",
                    title = "How does Raft leader election handle network partitions with split votes?",
                    content = "In our distributed cache capstone project, what happens if a network partition splits 5 nodes into 2 and 3? Will the partition of 2 nodes keep incrementing their term endlessly, or does the majority partition of 3 continue committing logs safely? Looking for practical advice on implementing pre-vote phases.",
                    subjectCode = "CS-308",
                    subjectName = "Software Engineering & Agile",
                    tags = "#DistributedSystems #Raft #CapstoneHelp",
                    upvotes = 19,
                    answersCount = 5,
                    timestamp = now - (6 * hour),
                    formattedDate = "Today, 4:30 AM",
                    isPinned = false
                ),
                CommunityPostEntity(
                    id = "post-3",
                    authorName = "Ananya Roy",
                    authorRole = "Operating Systems CR",
                    postType = "TIP",
                    title = "Memory Management Cheat-Sheet: Page Fault Handling & Belady's Anomaly",
                    content = "Quick revision tip for OS lab exam: Belady's anomaly occurs in FIFO page replacement where increasing page frames leads to MORE page faults! LRU and Optimal algorithms never exhibit Belady's anomaly because they belong to the class of 'Stack Algorithms'. Use this theorem to write proofs during tests.",
                    subjectCode = "CS-304",
                    subjectName = "Operating Systems",
                    tags = "#OperatingSystems #VirtualMemory #RevisionTip",
                    upvotes = 42,
                    answersCount = 12,
                    timestamp = now - (14 * hour),
                    formattedDate = "Yesterday, 8:45 PM",
                    isPinned = true
                ),
                CommunityPostEntity(
                    id = "post-4",
                    authorName = "Kunal Sharma",
                    authorRole = "2nd Year CSE",
                    postType = "QUESTION",
                    title = "Difference between TCP Reno vs TCP Cubic congestion window growth?",
                    content = "Doing the packet sniffer assignment. Why does TCP Cubic use a cubic function instead of Reno's AIMD linear growth? Does Cubic perform better on high-bandwidth, high-latency satellite connections?",
                    subjectCode = "CS-306",
                    subjectName = "Computer Networks",
                    tags = "#Networks #TCP #LabAssignment",
                    upvotes = 11,
                    answersCount = 3,
                    timestamp = now - (20 * hour),
                    formattedDate = "Yesterday, 2:10 PM",
                    isPinned = false
                ),
                CommunityPostEntity(
                    id = "post-5",
                    authorName = "Priya Sen",
                    authorRole = "Algorithm Club Lead",
                    postType = "TIP",
                    title = "Solving Dynamic Programming Problems: The 4-Step Systematic Framework",
                    content = "Whenever you're stuck on a 2D DP problem: 1) Define state dp[i][j] in plain English. 2) Identify the base cases explicitly. 3) Write the state transition recurrence relation. 4) Determine the evaluation order (top-down or bottom-up). Follow this and 90% of interview DP problems become mechanical!",
                    subjectCode = "CS-301",
                    subjectName = "Data Structures & Algorithms",
                    tags = "#DSA #DynamicProgramming #CodingInterview",
                    upvotes = 58,
                    answersCount = 15,
                    timestamp = now - (28 * hour),
                    formattedDate = "Oct 29, 6:00 PM",
                    isPinned = false
                )
            )

            dao.insertPosts(initialPosts)
        }
    }
}
