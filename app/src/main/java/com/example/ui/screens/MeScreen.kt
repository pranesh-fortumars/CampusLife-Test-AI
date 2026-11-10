package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlobalThemeToggleButton
import com.example.ui.theme.*
import com.example.util.AppUpdateManager
import com.example.viewmodel.CampusLifeViewModel
import kotlinx.coroutines.launch

@Composable
fun MeScreen(viewModel: CampusLifeViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isChecking by AppUpdateManager.isChecking.collectAsState()
    val isDownloading by AppUpdateManager.isDownloading.collectAsState()
    val downloadProgress by AppUpdateManager.downloadProgress.collectAsState()
    val updateAvailable by AppUpdateManager.updateAvailable.collectAsState()
    val remoteVersion by AppUpdateManager.remoteVersion.collectAsState()
    val statusMessage by AppUpdateManager.statusMessage.collectAsState()

    var showEditUrlDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf(AppUpdateManager.getVercelUrl(context)) }

    val isDarkModePref by viewModel.isDarkMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = isDarkModePref ?: isSystemDark

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("me_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Identity Card with Header Theme Toggle Button
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "DM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Devon Miles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Computer Science & Engineering • 4th Sem", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Roll No: 2024-CSE-042 • Section A", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Global Theme Toggle Button in Header
                    GlobalThemeToggleButton(
                        isDark = isDark,
                        onToggle = { viewModel.toggleTheme(isSystemDark) }
                    )
                }
            }
        }

        // Accessibility & Appearance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessibility_theme_card")
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) WarningContainer else IndigoPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (isDark) OnWarningContainer else IndigoPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Accessibility & Display",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isDark) "Dark mode active (Reduced eye strain)" else "Light mode active (High contrast)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            modifier = Modifier.testTag("theme_accessibility_switch")
                        )
                    }

                    Text(
                        text = "Switches background and text contrast between dark and light themes for optimal reading accessibility and comfortable viewing across varying ambient lighting.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Vercel OTA In-App Updates Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vercel_ota_card")
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            Text("In-App Updates (Vercel OTA)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (updateAvailable) ErrorContainer else EmeraldTertiaryContainer
                        ) {
                            Text(
                                text = if (updateAvailable) "Update Ready" else "Up to date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (updateAvailable) OnErrorContainer else EmeraldOnTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        "Automatically queries your deployed Vercel link for new commits and updates this phone without manual APK downloads.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // Active Vercel URL
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vercel Source Domain", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    AppUpdateManager.getVercelUrl(context),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IndigoPrimary
                                )
                            }
                            IconButton(
                                onClick = {
                                    customUrlInput = AppUpdateManager.getVercelUrl(context)
                                    showEditUrlDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Vercel URL", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Progress indicator if downloading
                    if (isDownloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                            Text("Downloading update: $downloadProgress%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Status feedback message
                    statusMessage?.let { msg ->
                        Text(msg, fontSize = 12.sp, color = IndigoPrimary, fontWeight = FontWeight.Medium)
                    }

                    // Release notes if update available
                    if (updateAvailable && remoteVersion != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldTertiaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    "🚀 New Build Available (${remoteVersion?.versionName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EmeraldOnTertiaryContainer
                                )
                                Text(
                                    remoteVersion?.releaseNotes ?: "",
                                    fontSize = 11.sp,
                                    color = EmeraldOnTertiaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    AppUpdateManager.checkForUpdates(context)
                                }
                            },
                            enabled = !isChecking && !isDownloading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_check_updates")
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

                        if (updateAvailable) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        AppUpdateManager.downloadAndInstall(context)
                                    }
                                },
                                enabled = !isDownloading,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_install_update")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install Now")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { AppUpdateManager.openVercelPageInBrowser(context) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_vercel_web")
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Web Hub")
                            }
                        }
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
                    Text(
                        "Enter your custom Vercel app URL (e.g. https://your-repo.vercel.app):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppUpdateManager.setVercelUrl(context, customUrlInput)
                        showEditUrlDialog = false
                    }
                ) {
                    Text("Save")
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
