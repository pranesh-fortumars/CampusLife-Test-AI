package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.local.CommunityPostEntity
import com.example.ui.components.GlobalThemeToggleButton
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: CampusLifeViewModel) {
    val posts by viewModel.posts.collectAsState()
    var selectedTag by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val tags = listOf("All", "Question", "Study Tip", "Group Study", "Notes")
    val filteredPosts = if (selectedTag == "All") posts else posts.filter { it.tag == selectedTag }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Peer Community Feed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ask study doubts, tips & share notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isDarkModePref by viewModel.isDarkMode.collectAsState()
                            val isSystemDark = isSystemInDarkTheme()
                            val isDark = isDarkModePref ?: isSystemDark

                            GlobalThemeToggleButton(
                                isDark = isDark,
                                onToggle = { viewModel.toggleTheme(isSystemDark) }
                            )

                            FilledTonalButton(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_create_post")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tags) { tag ->
                            val isSelected = selectedTag == tag
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTag = tag },
                                label = { Text(tag, fontSize = 12.sp) },
                                shape = RoundedCornerShape(100),
                                modifier = Modifier.heightIn(min = 36.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("community_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredPosts, key = { it.id }) { post ->
                CommunityPostSearchCard(
                    post = post,
                    onUpvote = { viewModel.upvotePost(post) }
                )
            }
        }
    }

    if (showCreateDialog) {
        var postTitle by remember { mutableStateOf("") }
        var postContent by remember { mutableStateOf("") }
        var courseCode by remember { mutableStateOf("CS201") }
        var postTag by remember { mutableStateOf("Question") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post Question or Study Tip") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Question on AVL Rotations") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("Details & Description") },
                        placeholder = { Text("Explain your question or share your revision tip...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = courseCode,
                            onValueChange = { courseCode = it },
                            label = { Text("Course Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = postTag,
                            onValueChange = { postTag = it },
                            label = { Text("Tag") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postContent.isNotBlank()) {
                            viewModel.createPost(postTitle, postContent, courseCode, postTag)
                            showCreateDialog = false
                        }
                    },
                    enabled = postTitle.isNotBlank() && postContent.isNotBlank()
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
