package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.MockData
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppDestination(val label: String) {
    HOME("Home"),
    TRACK("Track"),
    COMMUNITY("Community"),
    GROWTH("Growth"),
    ME("Me")
}

enum class SubScreen {
    NONE,
    FOCUS_TIMER,
    HACKATHON_PASS,
    SUBJECT_DETAIL,
    ID_VERIFICATION,
    CAMPUS_COPILOT,
    EXECUTIVE_DESK
}

class CampusLifeViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow(AppDestination.HOME)
    val currentTab: StateFlow<AppDestination> = _currentTab.asStateFlow()

    private val _subScreen = MutableStateFlow(SubScreen.NONE)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    private val _profile = MutableStateFlow(MockData.defaultProfile)
    val profile: StateFlow<StudentProfile> = _profile.asStateFlow()

    private val _subjects = MutableStateFlow(MockData.initialSubjects)
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _assignments = MutableStateFlow(MockData.assignments)
    val assignments: StateFlow<List<AssignmentItem>> = _assignments.asStateFlow()

    private val _projects = MutableStateFlow(MockData.projects)
    val projects: StateFlow<List<ProjectItem>> = _projects.asStateFlow()

    private val _societies = MutableStateFlow(MockData.societies)
    val societies: StateFlow<List<SocietyItem>> = _societies.asStateFlow()

    private val _conversations = MutableStateFlow(MockData.conversations)
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // Simulator consecutive classes state
    private val _simulatorCount = MutableStateFlow(3)
    val simulatorCount: StateFlow<Int> = _simulatorCount.asStateFlow()

    // Home Checklist tasks status
    private val _homeTaskState = MutableStateFlow(mapOf(0 to false, 1 to false, 2 to false))
    val homeTaskState: StateFlow<Map<Int, Boolean>> = _homeTaskState.asStateFlow()

    // Attendance Logged state
    private val _todayAttendanceLogged = MutableStateFlow(false)
    val todayAttendanceLogged: StateFlow<Boolean> = _todayAttendanceLogged.asStateFlow()

    // Track subtabs
    private val _trackSubTab = MutableStateFlow("Academics") // "Academics", "Timetable", "Assignments", "Exams"
    val trackSubTab: StateFlow<String> = _trackSubTab.asStateFlow()

    // Copilot messages
    private val _copilotMessages = MutableStateFlow(
        listOf(
            CopilotMessage("m-1", true, "What is my plan for this afternoon, and can I afford to skip the 2:45 PM Agile lab?", "12:38 PM"),
            CopilotMessage("m-2", false, "Here is your afternoon layout:", "12:38 PM", hasLayoutCard = true)
        )
    )
    val copilotMessages: StateFlow<List<CopilotMessage>> = _copilotMessages.asStateFlow()

    // Focus Timer state
    private val _focusSeconds = MutableStateFlow(38 * 60 + 45)
    val focusSeconds: StateFlow<Int> = _focusSeconds.asStateFlow()
    private val _isFocusRunning = MutableStateFlow(true)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    fun selectTab(tab: AppDestination) {
        _currentTab.value = tab
        _subScreen.value = SubScreen.NONE
    }

    fun openSubScreen(screen: SubScreen) {
        _subScreen.value = screen
    }

    fun closeSubScreen() {
        _subScreen.value = SubScreen.NONE
    }

    fun setTrackSubTab(tab: String) {
        _trackSubTab.value = tab
    }

    fun incrementSimulator() {
        if (_simulatorCount.value < 15) {
            _simulatorCount.update { it + 1 }
        }
    }

    fun decrementSimulator() {
        if (_simulatorCount.value > 1) {
            _simulatorCount.update { it - 1 }
        }
    }

    fun resetSimulator() {
        _simulatorCount.value = 3
    }

    fun toggleHomeTask(index: Int) {
        _homeTaskState.update { current ->
            current.toMutableMap().apply {
                this[index] = !(this[index] ?: false)
            }
        }
    }

    fun markTodayAttended() {
        _todayAttendanceLogged.value = true
        // Increment DBMS attendance
        _subjects.update { current ->
            current.map {
                if (it.code == "CS-302") it.copy(attendedClasses = it.attendedClasses + 1, totalClasses = it.totalClasses + 1)
                else it
            }
        }
    }

    fun toggleFocusTimer() {
        _isFocusRunning.update { !it }
    }

    fun resetFocusTimer() {
        _focusSeconds.value = 50 * 60
        _isFocusRunning.value = false
    }

    fun sendCopilotQuery(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = CopilotMessage(id = "usr-${System.currentTimeMillis()}", isUser = true, text = prompt)
        _copilotMessages.update { it + userMsg }

        // Generate intelligent contextual response
        val botMsg = CopilotMessage(
            id = "bot-${System.currentTimeMillis()}",
            isUser = false,
            text = "Analyzing syllabus & attendance logs for '$prompt'...\n\nYour current overall attendance is ${_profile.value.overallAttendance}%. You have safe margins in Software Engineering (+12%) and DBMS (+11%). Tomorrow's Operating Systems lecture at 9:15 AM is critical to stay above the 75% statutory NAAC minimum."
        )
        _copilotMessages.update { it + botMsg }
    }

    fun toggleAssignmentComplete(id: String) {
        _assignments.update { list ->
            list.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }
}
