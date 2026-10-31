package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Community Posts (Questions & Study Tips).
 */
@Dao
interface CommunityPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPostEntity>)

    @Query("SELECT * FROM community_posts ORDER BY isPinned DESC, timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE postType = :type ORDER BY timestamp DESC")
    fun getPostsByType(type: String): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE subjectCode = :subjectCode ORDER BY timestamp DESC")
    fun getPostsBySubject(subjectCode: String): Flow<List<CommunityPostEntity>>

    @Query("UPDATE community_posts SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvotePost(id: String)

    @Query("DELETE FROM community_posts WHERE id = :id")
    suspend fun deletePostById(id: String)

    @Query("SELECT COUNT(*) FROM community_posts")
    suspend fun getPostCount(): Int
}
