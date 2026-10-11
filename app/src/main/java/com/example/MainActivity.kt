package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Subject
import com.example.ui.components.CampusLifeBottomNav
import com.example.ui.components.CampusLifeTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CampusLifeTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusLifeTheme {
                CampusLifeApp()
            }
        }
    }
}

@Composable
fun CampusLifeApp(
    viewModel: CampusLifeViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val subScreen by viewModel.subScreen.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    // Handle back button when inside a subscreen
    BackHandler(enabled = subScreen != SubScreen.NONE) {
        viewModel.closeSubScreen()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (subScreen == SubScreen.NONE) {
                val subtitle = when (currentTab) {
                    AppDestination.HOME -> "Home"
                    AppDestination.TRACK -> "Track & Timetable"
                    AppDestination.COMMUNITY -> "Community & Clubs"
                    AppDestination.GROWTH -> "Growth & Portfolio"
                    AppDestination.ME -> "Me & Settings"
                }
                CampusLifeTopBar(
                    subtitle = subtitle,
                    onSearchClick = {},
                    onNotificationsClick = {},
                    onProfileClick = { viewModel.selectTab(AppDestination.ME) },
                    onExecutiveDeskClick = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                )
            }
        },
        bottomBar = {
            if (subScreen == SubScreen.NONE) {
                CampusLifeBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (subScreen) {
                SubScreen.FOCUS_TIMER -> {
                    FocusTimerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.HACKATHON_PASS -> {
                    HackathonPassScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.SUBJECT_DETAIL -> {
                    val dbmsSubject = subjects.firstOrNull { it.code == "CS-302" } ?: subjects.first()
                    SubjectDetailScreen(
                        subject = dbmsSubject,
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.ID_VERIFICATION -> {
                    IdVerificationScreen(
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.CAMPUS_COPILOT -> {
                    CampusCopilotScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.EXECUTIVE_DESK -> {
                    ExecutiveDeskScreen(
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.REVISION_PLAN -> {
                    ExamRevisionPlanScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.NONE -> {
                    when (currentTab) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTrack = { viewModel.selectTab(AppDestination.TRACK) },
                                onOpenCopilot = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) },
                                onOpenExecutiveDesk = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                            )
                        }
                        AppDestination.TRACK -> {
                            TrackScreen(
                                viewModel = viewModel,
                                onOpenSubjectDetail = {
                                    viewModel.openSubScreen(SubScreen.SUBJECT_DETAIL)
                                }
                            )
                        }
                        AppDestination.COMMUNITY -> {
                            CommunityScreen(viewModel = viewModel)
                        }
                        AppDestination.GROWTH -> {
                            GrowthScreen(viewModel = viewModel)
                        }
                        AppDestination.ME -> {
                            MeScreen(
                                viewModel = viewModel,
                                onOpenExecutiveDesk = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                            )
                        }
                    }
                }
            }
        }
    }
}
