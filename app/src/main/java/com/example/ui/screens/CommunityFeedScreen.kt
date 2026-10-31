package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CommunityPostEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

/**
 * Community Feed UI Screen for students to post study-related questions or tips,
 * with local persistence via Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val posts by viewModel.communityPosts.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "QUESTION", "TIP"
    var selectedSubjectFilter by remember { mutableStateOf("ALL") } // "ALL" or subject code
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, selectedTypeFilter, selectedSubjectFilter, searchQuery) {
        posts.filter { post ->
            val matchesType = when (selectedTypeFilter) {
                "QUESTION" -> post.isQuestion
                "TIP" -> post.isTip
                else -> true
            }
            val matchesSubject = when (selectedSubjectFilter) {
                "ALL" -> true
                else -> post.subjectCode.equals(selectedSubjectFilter, ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                    post.title.contains(searchQuery, ignoreCase = true) ||
                    post.content.contains(searchQuery, ignoreCase = true) ||
                    post.tags.contains(searchQuery, ignoreCase = true) ||
                    post.authorName.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSubject && matchesQuery
        }
    }

    val totalQuestions = posts.count { it.isQuestion }
    val totalTips = posts.count { it.isTip }
    val totalUpvotes = posts.sumOf { it.upvotes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Campus Study Feed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Peer-to-peer study questions, exam tips & problem solving",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("community_feed_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("community_feed_add_icon_btn")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "New Post", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Ask or Share Tip", fontWeight = FontWeight.SemiBold) },
                containerColor = Primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_community_post_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("community_feed_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats & Overview Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_feed_stats_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Collaborative Study Network",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Local Room persistence enabled for all posts & upvotes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatPill(count = totalQuestions, label = "Questions", icon = Icons.Default.HelpOutline, color = Primary)
                            StatPill(count = totalTips, label = "Tips", icon = Icons.Default.Lightbulb, color = SuccessGreen)
                            StatPill(count = totalUpvotes, label = "Upvotes", icon = Icons.Default.ThumbUp, color = Secondary)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search questions, tips, or tags...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_feed_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Post Type Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text("All Posts (${posts.size})") },
                        leadingIcon = { Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("filter_all_posts")
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "QUESTION",
                        onClick = { selectedTypeFilter = "QUESTION" },
                        label = { Text("Questions ❓ ($totalQuestions)") },
                        leadingIcon = { Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Warning.copy(alpha = 0.2f),
                            selectedLabelColor = Warning
                        ),
                        modifier = Modifier.testTag("filter_question_posts")
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "TIP",
                        onClick = { selectedTypeFilter = "TIP" },
                        label = { Text("Study Tips 💡 ($totalTips)") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SuccessGreen
                        ),
                        modifier = Modifier.testTag("filter_tip_posts")
                    )
                }
            }

            // Subject Code Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        SuggestionChip(
                            onClick = { selectedSubjectFilter = "ALL" },
                            label = { Text("All Courses") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedSubjectFilter == "ALL") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("chip_subject_all")
                        )
                    }
                    items(subjects) { subject ->
                        SuggestionChip(
                            onClick = {
                                selectedSubjectFilter = if (selectedSubjectFilter == subject.code) "ALL" else subject.code
                            },
                            label = { Text("${subject.code} ${subject.name}") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedSubjectFilter == subject.code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("chip_subject_${subject.code}")
                        )
                    }
                }
            }

            // Empty state
            if (filteredPosts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No posts found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Be the first to ask a study question or share an exam tip!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("empty_state_create_post_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Post")
                            }
                        }
                    }
                }
            } else {
                // Post Feed List
                items(filteredPosts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        onUpvote = { viewModel.upvoteCommunityPost(post.id) },
                        onDelete = { viewModel.deleteCommunityPost(post.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCommunityPostDialog(
            defaultAuthorName = profile.name.ifBlank { "Rahul Sharma" },
            defaultAuthorRole = profile.title.ifBlank { "3rd Year • B.Tech CSE" },
            subjects = subjects.map { it.code to it.name },
            onDismiss = { showCreateDialog = false },
            onSubmit = { authorName, authorRole, postType, title, content, subjectCode, subjectName, tags ->
                viewModel.addCommunityPost(
                    authorName = authorName,
                    authorRole = authorRole,
                    postType = postType,
                    title = title,
                    content = content,
                    subjectCode = subjectCode,
                    subjectName = subjectName,
                    tags = tags
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun StatPill(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Column {
                Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Individual Community Post Card representing either a Study Question or Study Tip.
 */
@Composable
fun CommunityPostCard(
    post: CommunityPostEntity,
    onUpvote: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Type badge, Pinned indicator, Timestamp, Delete menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (post.isQuestion) {
                        Surface(
                            color = Warning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("❓", fontSize = 11.sp)
                                Text(
                                    text = "QUESTION",
                                    color = Warning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💡", fontSize = 11.sp)
                                Text(
                                    text = "STUDY TIP",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (post.isPinned) {
                        Surface(
                            color = Primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = null, tint = Primary, modifier = Modifier.size(11.dp))
                                Text("PINNED", color = Primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = post.formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_post_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete post",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Author Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (post.isQuestion) Warning.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (post.isQuestion) Warning else SuccessGreen,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = post.authorRole,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Content (with tap to expand/collapse if long)
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                modifier = Modifier.clickable { expanded = !expanded }
            )

            if (post.content.length > 160 && !expanded) {
                Text(
                    text = "Read more...",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { expanded = true }
                )
            }

            // Tags & Subject Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.subjectCode.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${post.subjectCode} ${post.subjectName}".trim(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = post.tags,
                    fontSize = 11.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp
            )

            // Bottom Actions: Upvote Button & Answers Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upvote button (Room persistence)
                FilledTonalButton(
                    onClick = onUpvote,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (post.upvotes > 0) Primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (post.upvotes > 0) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("upvote_post_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.upvotes > 0) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Upvote",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Helpful (${post.upvotes})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (post.isQuestion) "${post.answersCount} answers" else "${post.answersCount} comments",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Dialog for composing and persisting a new Study Question or Study Tip into Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityPostDialog(
    defaultAuthorName: String,
    defaultAuthorRole: String,
    subjects: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSubmit: (
        authorName: String,
        authorRole: String,
        postType: String,
        title: String,
        content: String,
        subjectCode: String,
        subjectName: String,
        tags: String
    ) -> Unit
) {
    var postType by remember { mutableStateOf("QUESTION") } // "QUESTION" or "TIP"
    var authorName by remember { mutableStateOf(defaultAuthorName) }
    var authorRole by remember { mutableStateOf(defaultAuthorRole) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedSubjectCode by remember { mutableStateOf(subjects.firstOrNull()?.first ?: "CS-302") }
    var tags by remember { mutableStateOf("#Study #ExamPrep") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("create_post_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (postType == "QUESTION") "Ask a Study Question" else "Share a Study Tip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Segmented Post Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isQ = postType == "QUESTION"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isQ) Warning else Color.Transparent)
                            .clickable { postType = "QUESTION" }
                            .padding(vertical = 8.dp)
                            .testTag("select_post_type_question"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❓ Question",
                            fontWeight = FontWeight.Bold,
                            color = if (isQ) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    val isT = postType == "TIP"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isT) SuccessGreen else Color.Transparent)
                            .clickable { postType = "TIP" }
                            .padding(vertical = 8.dp)
                            .testTag("select_post_type_tip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💡 Study Tip",
                            fontWeight = FontWeight.Bold,
                            color = if (isT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (hasError) hasError = false
                    },
                    label = { Text("Title / Summary *") },
                    placeholder = {
                        Text(
                            if (postType == "QUESTION") "e.g. How does Raft leader election handle split votes?"
                            else "e.g. B+ Tree Clustered Index Optimization Tip"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_title_input"),
                    singleLine = true,
                    isError = hasError && title.isBlank()
                )

                // Content input
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if (hasError) hasError = false
                    },
                    label = { Text("Detailed Explanation / Question *") },
                    placeholder = {
                        Text(
                            if (postType == "QUESTION") "Provide context, what you've tried, code snippets or theorem specifics..."
                            else "Explain key concepts, memory formulas, or problem-solving shortcuts..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("post_content_input"),
                    maxLines = 5,
                    isError = hasError && content.isBlank()
                )

                // Course Picker Row
                Text(
                    text = "Related Course:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { (code, _) ->
                        val isSelected = selectedSubjectCode == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectCode = code },
                            label = { Text(code, fontSize = 11.sp) },
                            modifier = Modifier.testTag("dialog_course_chip_$code")
                        )
                    }
                }

                // Tags input
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags") },
                    placeholder = { Text("#Exam #Lab #DSA") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_tags_input"),
                    singleLine = true
                )

                if (hasError) {
                    Text(
                        text = "Please enter both a title and details.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_post_btn")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                hasError = true
                            } else {
                                val subjectName = subjects.find { it.first == selectedSubjectCode }?.second ?: ""
                                onSubmit(
                                    authorName.ifBlank { "Rahul Sharma" },
                                    authorRole.ifBlank { "3rd Year • B.Tech CSE" },
                                    postType,
                                    title.trim(),
                                    content.trim(),
                                    selectedSubjectCode,
                                    subjectName,
                                    tags.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (postType == "QUESTION") Warning else SuccessGreen
                        ),
                        modifier = Modifier.testTag("submit_community_post_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (postType == "QUESTION") "Post Question" else "Share Tip")
                    }
                }
            }
        }
    }
}
