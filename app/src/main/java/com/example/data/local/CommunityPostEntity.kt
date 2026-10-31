package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity representing a community feed post (Study Question or Study Tip).
 */
@Entity(
    tableName = "community_posts",
    indices = [
        Index(value = ["postType"]),
        Index(value = ["timestamp"]),
        Index(value = ["subjectCode"])
    ]
)
data class CommunityPostEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorRole: String = "3rd Year CSE",
    val postType: String = "QUESTION", // "QUESTION" or "TIP"
    val title: String,
    val content: String,
    val subjectCode: String = "",
    val subjectName: String = "",
    val tags: String = "#Study #Academics",
    val upvotes: Int = 0,
    val answersCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "Just now",
    val isPinned: Boolean = false
) {
    val isQuestion: Boolean
        get() = postType.equals("QUESTION", ignoreCase = true)

    val isTip: Boolean
        get() = postType.equals("TIP", ignoreCase = true)
}
