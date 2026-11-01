package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockData
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen
import kotlinx.coroutines.launch

@Composable
fun MeScreen(
    viewModel: CampusLifeViewModel,
    onOpenExecutiveDesk: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    var selectedSemFilter by remember { mutableStateOf("all") }
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    val filteredTimeline = MockData.timelineNodes.filter {
        selectedSemFilter == "all" || it.semesterTag == selectedSemFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("me_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Profile Hero & Identity Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Primary)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(100), color = PrimaryFixed) {
                            Text("CR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnPrimaryFixed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text(profile.title, style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(100), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${profile.college} • ${profile.rollNo}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                        FilledTonalButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Card")
                        }
                    }
                }
            }
        }

        // Academic & Social Stats Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CGPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.cgpa}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Top 8%", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Tertiary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.overallAttendance}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Safe >75%", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Tertiary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Projects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.projectsCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("2 Live", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Secondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Network", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.networkCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("DTU Peers", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Campus Privacy Guard Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Campus Privacy Guard", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(100), color = TertiaryFixed) {
                                Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(
                            "Academic records, attendance metrics, and fee dues are encrypted & private to you. Only showcased projects and club badges are publicly viewable on campus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // College Life Timeline Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DTU JOURNEY", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                        Text("College Life Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(
                        onClick = { showAddMemoryDialog = true },
                        shape = RoundedCornerShape(100)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Memory", fontSize = 12.sp)
                    }
                }

                // Horizontal Semester Filter Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val sems = listOf("all" to "All (4 Yrs)", "sem6" to "Sem 6 (Current)", "sem5" to "Sem 5", "sem4" to "Sem 4", "sem1" to "Sem 1")
                    items(sems) { (key, label) ->
                        val isSelected = selectedSemFilter == key
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.clickable { selectedSemFilter = key }
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Timeline Items
        items(filteredTimeline) { node ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Node dot and line
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Primary))
                    Box(modifier = Modifier.width(2.dp).height(100.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(shape = RoundedCornerShape(100), color = PrimaryFixed.copy(alpha = 0.5f)) {
                                Text(node.dateLabel, style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text(node.categoryBadge, style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(node.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(node.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            node.tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                                    Text(tag, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Settings & Student Hub
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Settings & Student Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    SettingRow(
                        icon = Icons.Default.SmartToy,
                        iconBg = SecondaryFixed,
                        iconTint = Secondary,
                        title = "Campus Copilot AI Settings",
                        subtitle = "Syllabus ingestion & mock viva permissions",
                        onClick = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) }
                    )

                    SettingRow(
                        icon = Icons.Default.AccountBalance,
                        iconBg = PrimaryFixed,
                        iconTint = Primary,
                        title = "Executive Council Portal Desk",
                        subtitle = "Dean & Student Senate Verification Queue",
                        onClick = onOpenExecutiveDesk
                    )

                    SettingRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBg = TertiaryFixed,
                        iconTint = Tertiary,
                        title = "Expense & Budget (₹ INR)",
                        subtitle = "Canteen dues, stationery ledger • ₹1,420 Due",
                        onClick = {}
                    )

                    SettingRow(
                        icon = Icons.Default.VerifiedUser,
                        iconBg = PrimaryContainer,
                        iconTint = Color.White,
                        title = "Verify Student Identity",
                        subtitle = "Institutional email (.ac.in) & roll card",
                        onClick = { viewModel.openSubScreen(SubScreen.ID_VERIFICATION) }
                    )
                }
            }
        }

        // App Updates & Vercel OTA Synchronization
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val isChecking by com.example.util.AppUpdateManager.isChecking.collectAsState()
            val isDownloading by com.example.util.AppUpdateManager.isDownloading.collectAsState()
            val downloadProgress by com.example.util.AppUpdateManager.downloadProgress.collectAsState()
            val updateAvailable by com.example.util.AppUpdateManager.updateAvailable.collectAsState()
            val remoteVersion by com.example.util.AppUpdateManager.remoteVersion.collectAsState()
            val statusMessage by com.example.util.AppUpdateManager.statusMessage.collectAsState()

            var showEditUrlDialog by remember { mutableStateOf(false) }
            var vercelUrlInput by remember { mutableStateOf(com.example.util.AppUpdateManager.getVercelUrl(context)) }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_updates_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Text("In-App Updates (Vercel OTA)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Current: v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (updateAvailable) ErrorContainer else TertiaryContainer
                        ) {
                            Text(
                                text = if (updateAvailable) "Update Ready" else "Up to Date",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (updateAvailable) OnErrorContainer else OnTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Vercel deployment link preview
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vercel Source Link", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = com.example.util.AppUpdateManager.getVercelUrl(context),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    vercelUrlInput = com.example.util.AppUpdateManager.getVercelUrl(context)
                                    showEditUrlDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Vercel URL", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Status feedback message if any
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Download progress indicator
                    if (isDownloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp)
                            )
                            Text("Downloading update: $downloadProgress%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Remote version release notes card if update is available
                    if (updateAvailable && remoteVersion != null) {
                        val remote = remoteVersion!!
                        Surface(
                            color = TertiaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "🚀 New Version ${remote.versionName} Available!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = OnTertiaryContainer
                                )
                                Text(
                                    text = remote.releaseNotes,
                                    fontSize = 12.sp,
                                    color = OnTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Check for updates button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    com.example.util.AppUpdateManager.checkForUpdates(context)
                                }
                            },
                            enabled = !isChecking && !isDownloading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_check_vercel_updates"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check Updates")
                            }
                        }

                        // Install or Open Web Page button
                        if (updateAvailable) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        com.example.util.AppUpdateManager.downloadAndInstall(context)
                                    }
                                },
                                enabled = !isDownloading,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_install_vercel_update"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install Update")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    com.example.util.AppUpdateManager.openVercelPageInBrowser(context)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_vercel_page"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Vercel")
                            }
                        }
                    }
                }
            }

            if (showEditUrlDialog) {
                AlertDialog(
                    onDismissRequest = { showEditUrlDialog = false },
                    title = { Text("Configure Vercel Deployment Link") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Enter the public Vercel production URL where the latest APK and version.json are hosted:", fontSize = 12.sp)
                            OutlinedTextField(
                                value = vercelUrlInput,
                                onValueChange = { vercelUrlInput = it },
                                label = { Text("Vercel URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            com.example.util.AppUpdateManager.setVercelUrl(context, vercelUrlInput)
                            showEditUrlDialog = false
                        }) {
                            Text("Save Link")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditUrlDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

    if (showAddMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = { Text("Record Campus Milestone") },
            text = { Text("Save this memory to your college-life portfolio timeline.") },
            confirmButton = {
                Button(onClick = { showAddMemoryDialog = false }) {
                    Text("Save Memory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
    }
}
