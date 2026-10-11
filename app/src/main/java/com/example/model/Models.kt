package com.example.model

data class StudentProfile(
    val name: String = "Rahul Sharma",
    val title: String = "3rd Year • B.Tech Computer Science & Engineering",
    val college: String = "Delhi Technological University (DTU)",
    val rollNo: String = "21BCS084",
    val roleBadge: String = "CR",
    val isPro: Boolean = true,
    val openToInternships: Boolean = true,
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuAkl376-ud6ID6QH98fsaDb8X6znuuCw9iiOt47DQxwDGSNT0F_kDt0XWaxxVf96SATPnae_AsXLkVSxEoQRTIL00B6_DcKZRsCRuS6mMA442kw53tcNx0iHQtod4ASxQbTPe5wPdNA9DJR4hKHlgnQNUqOovvdv54mTKRv-bMP2p1vlmGxnn0eZgFlCxPZcRZ7wYksT1MF3_z4zPyJOMs0dvCsf64WVS3cGXkpMsPRNsmQsAPlEX6XKA",
    val bannerAvatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuDTVSSJ_edqv9wakriCJRF6R3OKRNZEiRpMY-ERBpZLU4hc8dvnNtircSL7YqEzwgknL4OGHYrxmk2xEYS2ZjS7eRI85lNIfQIlWunEjrpXtOyxT6Qz0NdWkAcerWFOg8DPqPY2EY4Oil2MmFijqdISJe1t6WjqA9CK11qphJgLZL1fu6RV4SJu84hBjx8r7atcXFVPzoZLGR8xL4niDm8ZaGxXFM6CgkQqNlULOoCU-4N6XeqPBwlQ5w",
    val bio: String = "Passionate about full-stack dev, distributed systems & campus robotics. Class Representative (CR) for Section B.",
    val cgpa: Double = 8.64,
    val sgpa: Double = 8.78,
    val overallAttendance: Double = 81.4,
    val projectsCount: Int = 5,
    val liveDtuProjects: Int = 2,
    val hackWinsCount: Int = 3,
    val ghStarsCount: String = "1.2k",
    val networkCount: Int = 248,
    val isVerified: Boolean = true
)

data class Subject(
    val code: String,
    val name: String,
    val credits: Int,
    val faculty: String,
    val department: String = "Dept. of Computer Science",
    val attendedClasses: Int,
    val totalClasses: Int,
    val nextClassTime: String,
    val room: String,
    val isLabIncluded: Boolean = true
) {
    val attendancePercentage: Double
        get() = if (totalClasses == 0) 100.0 else (attendedClasses.toDouble() / totalClasses) * 100.0

    val safeBunksLeft: Int
        get() {
            // calculate how many bunks while remaining >= 75%
            // (attendedClasses) / (totalClasses + bunks) >= 0.75
            // attendedClasses >= 0.75 * totalClasses + 0.75 * bunks
            // bunks <= (attendedClasses - 0.75 * totalClasses) / 0.75
            val maxAllowed = ((attendedClasses - 0.75 * totalClasses) / 0.75).toInt()
            return if (maxAllowed < 0) 0 else maxAllowed
        }

    val neededToReach75: Int
        get() {
            // (attendedClasses + X) / (totalClasses + X) >= 0.75
            // attendedClasses + X >= 0.75 * totalClasses + 0.75 * X
            // 0.25 * X >= 0.75 * totalClasses - attendedClasses
            val diff = (0.75 * totalClasses - attendedClasses)
            if (diff <= 0) return 0
            return kotlin.math.ceil(diff / 0.25).toInt()
        }
}

data class AssignmentItem(
    val id: String,
    val title: String,
    val subject: String,
    val faculty: String,
    val dueDate: String,
    val dueTime: String,
    val priority: String,
    val internalWeightage: String,
    val isUrgent: Boolean = false,
    val draftFile: String? = null,
    val progressCompleted: Int = 0,
    val progressTotal: Int = 0,
    val collaborators: List<String> = emptyList(),
    val status: String = "In Progress",
    val isCompleted: Boolean = false
)

data class ExamScheduleItem(
    val subjectCode: String,
    val subjectName: String,
    val date: String,
    val time: String,
    val room: String
)

data class TimetableSlot(
    val id: String,
    val code: String,
    val name: String,
    val credits: Int,
    val faculty: String,
    val facultyAvatar: String,
    val timeStart: String,
    val timeEnd: String,
    val room: String,
    val isAttended: Boolean = false,
    val isLiveNow: Boolean = false,
    val isBreak: Boolean = false
)

data class ProjectItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val techStack: List<String>,
    val stars: Int,
    val status: String,
    val progress: Int? = null,
    val bannerUrl: String? = null,
    val collaborators: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val impactNote: String? = null
)

data class SocietyItem(
    val id: String,
    val name: String,
    val category: String,
    val memberCount: Int,
    val lead: String,
    val statusBadge: String,
    val nextEventTitle: String,
    val nextEventTime: String,
    val tags: List<String>,
    val isMember: Boolean = false,
    val logoText: String? = null,
    val heroImageUrl: String? = null
)

data class TimelineNode(
    val id: String,
    val semesterTag: String,
    val dateLabel: String,
    val title: String,
    val description: String,
    val categoryBadge: String,
    val tags: List<String>
)

data class SkillItem(
    val name: String,
    val level: String, // "Advanced", "Proficient", "Intermediate"
    val rating: Double,
    val endorsementsCount: Int,
    val progressPct: Float
)

data class ChatConversation(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val unreadCount: Int = 0,
    val type: String, // "groups", "dms", "channels"
    val avatarUrl: String? = null,
    val isFaculty: Boolean = false,
    val isOnline: Boolean = false,
    val attachmentName: String? = null,
    val isLiveAudioHuddle: Boolean = false
)

data class CopilotMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "12:38 PM",
    val hasLayoutCard: Boolean = false
)
