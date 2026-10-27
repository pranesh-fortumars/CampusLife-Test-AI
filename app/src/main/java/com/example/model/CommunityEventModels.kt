package com.example.model

import java.util.UUID

/**
 * Categorization for campus events.
 */
enum class EventCategory(val label: String, val iconEmoji: String) {
    ALL("All Events", "📅"),
    CLUB_MEETINGS("Club Meetings", "🎪"),
    ACADEMIC_SEMINARS("Academic Seminars", "🎓"),
    WORKSHOPS("Workshops & Sprints", "⚡"),
    MY_RSVPS("My RSVPs", "⭐")
}

/**
 * Distinct types of campus gatherings.
 */
enum class CommunityEventType(val label: String, val badgeColorHex: Long) {
    CLUB_MEETING("Club Meeting", 0xFF0D9488),        // Teal
    ACADEMIC_SEMINAR("Academic Seminar", 0xFF4F46E5), // Indigo
    WORKSHOP("Workshop / Hackathon", 0xFFD97706)     // Amber
}

/**
 * Student RSVP status.
 */
enum class RsvpStatus(val label: String) {
    NONE("Not Responded"),
    GOING("Going"),
    INTERESTED("Interested"),
    DECLINED("Can't Attend")
}

/**
 * Representation of a campus event (club meeting, academic research seminar, workshop).
 */
data class CampusEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val organizer: String, // e.g. "ACM DTU Chapter", "Dept. of Computer Science & Engineering"
    val organizerType: String = "Campus Club", // "Campus Club", "Academic Department", "Research Lab"
    val eventType: CommunityEventType,
    val dateFormatted: String, // e.g. "Tomorrow, Oct 28", "Thu, Oct 30"
    val timeFormatted: String, // e.g. "4:30 PM - 6:00 PM"
    val venue: String, // e.g. "Senate Hall, Admin Block", "CS Lab 304, Block B"
    val isOnlineOrHybrid: Boolean = false,
    val meetingLinkOrStream: String? = null,
    val speakerOrHost: String = "",
    val speakerAffiliation: String = "",
    val description: String,
    val tags: List<String> = emptyList(),
    val attendeeCount: Int = 0,
    val capacity: Int? = null,
    val rsvpStatus: RsvpStatus = RsvpStatus.NONE,
    val isReminderSet: Boolean = false,
    val isFeatured: Boolean = false,
    val prereqsOrNotes: String? = null,
    val contactEmailOrPhone: String = "events@dtu.ac.in"
)

/**
 * Pre-populated mock data of campus club meetings and academic seminars.
 */
object CampusEventMockData {
    val sampleEvents: List<CampusEvent> = listOf(
        CampusEvent(
            id = "evt-seminar-1",
            title = "Advances in Foundation Models & Constitutional AI Alignment",
            organizer = "Dept. of Computer Science & Engineering",
            organizerType = "Academic Department",
            eventType = CommunityEventType.ACADEMIC_SEMINAR,
            dateFormatted = "Tomorrow, Oct 28",
            timeFormatted = "3:30 PM - 5:00 PM",
            venue = "Senate Hall, Admin Complex (Hybrid)",
            isOnlineOrHybrid = true,
            meetingLinkOrStream = "https://meet.google.com/dtu-cs-seminar",
            speakerOrHost = "Dr. Arvind Singhania",
            speakerAffiliation = "Visiting Research Fellow, DeepMind & Ex-IITD",
            description = "A deep-dive research seminar exploring self-play reinforcement learning from AI feedback (RLAIF), safety alignment vectors in multimodal foundation models, and open research challenges in automated interpretability.",
            tags = listOf("AI", "MachineLearning", "ResearchColloquium", "Alignment"),
            attendeeCount = 142,
            capacity = 200,
            rsvpStatus = RsvpStatus.GOING,
            isReminderSet = true,
            isFeatured = true,
            prereqsOrNotes = "Familiarity with Transformer architectures and gradient descent optimization recommended."
        ),
        CampusEvent(
            id = "evt-club-1",
            title = "ACM Weekly Competitive Coding Sprint & System Design Critique",
            organizer = "ACM DTU Chapter",
            organizerType = "Campus Club",
            eventType = CommunityEventType.CLUB_MEETING,
            dateFormatted = "Wednesday, Oct 29",
            timeFormatted = "5:15 PM - 7:00 PM",
            venue = "CS Lab 304, Academic Block B",
            isOnlineOrHybrid = false,
            speakerOrHost = "Rahul Sharma & Tanya Verma",
            speakerAffiliation = "CodeCraft Team & ACM Lead Mentors",
            description = "Hands-on problem solving on dynamic programming with bitmasking, followed by an interactive architecture critique of a distributed real-time rate limiter using Redis and sliding window logs.",
            tags = listOf("CompetitiveCoding", "Algorithms", "SystemDesign", "ACM"),
            attendeeCount = 68,
            capacity = 80,
            rsvpStatus = RsvpStatus.GOING,
            isReminderSet = true,
            isFeatured = false,
            prereqsOrNotes = "Bring your laptop with your preferred IDE and C++/Java/Python compiler configured."
        ),
        CampusEvent(
            id = "evt-seminar-2",
            title = "Semiconductor Horizons: 2nm Gate-All-Around (GAA) Architecture",
            organizer = "Dept. of Electronics & Communication Engineering",
            organizerType = "Academic Department",
            eventType = CommunityEventType.ACADEMIC_SEMINAR,
            dateFormatted = "Thursday, Oct 30",
            timeFormatted = "11:00 AM - 12:30 PM",
            venue = "ECE Seminar Hall, 2nd Floor",
            isOnlineOrHybrid = false,
            speakerOrHost = "Prof. Meenakshi Sundaram",
            speakerAffiliation = "Distinguished Chair Professor of Microelectronics",
            description = "Departmental colloquium detailing sub-nanometer lithography, nanosheet FET fabrication physics, and the thermal mitigation hurdles facing next-generation exascale processor nodes.",
            tags = listOf("VLSI", "Hardware", "Semiconductors", "ECE"),
            attendeeCount = 95,
            capacity = 150,
            rsvpStatus = RsvpStatus.INTERESTED,
            isReminderSet = false,
            isFeatured = false,
            prereqsOrNotes = "Open to all B.Tech/M.Tech and research scholars. Refreshments provided."
        ),
        CampusEvent(
            id = "evt-club-2",
            title = "Autonomous Drone Navigation with ROS2 & Visual SLAM",
            organizer = "Robotics & Automation Society",
            organizerType = "Campus Club",
            eventType = CommunityEventType.CLUB_MEETING,
            dateFormatted = "Friday, Oct 31",
            timeFormatted = "4:00 PM - 6:30 PM",
            venue = "Central Workshop Arena & Drone Cage",
            isOnlineOrHybrid = false,
            speakerOrHost = "Aditya Kapur",
            speakerAffiliation = "RoboSub & UAS Team Captain",
            description = "Live flight telemetry demonstration and code walkthrough integrating camera depth sensors with ROS2 Humble Nav2 stacks for obstacle evasion in GPS-denied environments.",
            tags = listOf("Robotics", "ROS2", "SLAM", "Drones"),
            attendeeCount = 54,
            capacity = 60,
            rsvpStatus = RsvpStatus.NONE,
            isReminderSet = false,
            isFeatured = true,
            prereqsOrNotes = "Live testing will occur inside the safety netting. Protective eyewear supplied."
        ),
        CampusEvent(
            id = "evt-workshop-1",
            title = "Cloud Run & Vertex AI Multi-Agent Hack Sprint",
            organizer = "Google Developer Student Club (GDSC)",
            organizerType = "Campus Club",
            eventType = CommunityEventType.WORKSHOP,
            dateFormatted = "Saturday, Nov 1",
            timeFormatted = "10:00 AM - 4:00 PM",
            venue = "Auditorium 2 & Innovation Lounge",
            isOnlineOrHybrid = true,
            meetingLinkOrStream = "https://discord.gg/gdsc-dtu-sprint",
            speakerOrHost = "GDSC Technical Leads & Google Cloud Innovators",
            speakerAffiliation = "Certified Google Cloud Engineers",
            description = "Build and deploy collaborative multi-agent microservices connected to vector search databases. Cloud credits, mentorship, certificates, and prize pool for top 3 teams.",
            tags = listOf("Cloud", "GoogleCloud", "Hackathon", "AI-Agents"),
            attendeeCount = 186,
            capacity = 220,
            rsvpStatus = RsvpStatus.NONE,
            isReminderSet = false,
            isFeatured = false,
            prereqsOrNotes = "Prerequisites: Basic REST API knowledge. Free GCP sandbox credits provided on arrival."
        ),
        CampusEvent(
            id = "evt-seminar-3",
            title = "Quantum Algorithms for Combinatorial Graph Optimization",
            organizer = "Centre for Theoretical Computer Science & Applied Physics",
            organizerType = "Research Lab",
            eventType = CommunityEventType.ACADEMIC_SEMINAR,
            dateFormatted = "Monday, Nov 3",
            timeFormatted = "2:00 PM - 3:30 PM",
            venue = "Ramanujan Lecture Theatre (LT-1)",
            isOnlineOrHybrid = true,
            meetingLinkOrStream = "https://zoom.us/j/dtu-quantum-colloquium",
            speakerOrHost = "Dr. Elena Rostova",
            speakerAffiliation = "Max Planck Institute of Quantum Optics Fellow",
            description = "Investigation into Quantum Approximate Optimization Algorithm (QAOA) speedups over classical simulated annealing on Max-Cut instances under noisy intermediate-scale quantum (NISQ) constraints.",
            tags = listOf("Quantum", "Algorithms", "TheoreticalCS", "Physics"),
            attendeeCount = 78,
            capacity = 120,
            rsvpStatus = RsvpStatus.NONE,
            isReminderSet = false,
            isFeatured = false,
            prereqsOrNotes = "Undergraduate linear algebra and basics of complexity theory."
        ),
        CampusEvent(
            id = "evt-club-3",
            title = "UI/UX Design Jam: Accessibility & Material 3 Critique",
            organizer = "Design Guild & Creative Society",
            organizerType = "Campus Club",
            eventType = CommunityEventType.CLUB_MEETING,
            dateFormatted = "Tuesday, Nov 4",
            timeFormatted = "5:00 PM - 6:45 PM",
            venue = "SAC Room 12, Student Activity Centre",
            isOnlineOrHybrid = false,
            speakerOrHost = "Sanjana Rao",
            speakerAffiliation = "Product Design Lead at FinTech Startup",
            description = "Interactive design review and rapid prototyping session. Bring your Figma links or student portfolio projects for real-time critique on color contrast, typography hierarchies, and touch targets.",
            tags = listOf("Design", "UIUX", "Accessibility", "Figma"),
            attendeeCount = 42,
            capacity = 50,
            rsvpStatus = RsvpStatus.NONE,
            isReminderSet = false,
            isFeatured = false,
            prereqsOrNotes = "Bring a laptop with Figma or pen & paper for wireframe sketches."
        )
    )
}
