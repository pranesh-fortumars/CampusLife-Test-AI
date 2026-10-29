package com.example.model

import java.util.UUID

/**
 * Status columns for the Kanban Job & Internship Application Board.
 */
enum class ApplicationStatus(val displayName: String, val badgeColorHex: Long) {
    APPLIED("Applied", 0xFF3B5BDB),           // Royal Indigo
    INTERVIEWING("Interviewing", 0xFFD97706), // Amber Warning
    OFFER("Offer", 0xFF10B981)               // Emerald Success
}

/**
 * Category of career milestone.
 */
enum class CareerItemType(val label: String, val iconEmoji: String) {
    INTERNSHIP("Internship", "💼"),
    FULL_TIME("Full-Time Job", "🚀"),
    NETWORKING("Networking Milestone", "🤝")
}

/**
 * Model representing a job application, internship, or networking milestone card on the Kanban board.
 */
data class CareerApplication(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String,
    val roleTitle: String,
    val type: CareerItemType = CareerItemType.INTERNSHIP,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val location: String = "Bangalore, India",
    val compensation: String = "₹1.25L / mo",
    val appliedDate: String = "Oct 15, 2026",
    val nextMilestone: String = "Awaiting initial resume shortlist",
    val nextMilestoneDate: String = "Nov 02",
    val interviewRound: String = "Round 1: Online Assessment",
    val referralContact: String = "Alumni Referral (Priya Nair)",
    val tags: List<String> = listOf("Kotlin", "Backend", "On-Campus"),
    val notes: String = "Submitted tailored resume with SmartCampus IoT project highlighted.",
    val portalUrl: String = "https://careers.google.com",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val canMoveForward: Boolean
        get() = status != ApplicationStatus.OFFER

    val canMoveBackward: Boolean
        get() = status != ApplicationStatus.APPLIED

    val nextStatus: ApplicationStatus?
        get() = when (status) {
            ApplicationStatus.APPLIED -> ApplicationStatus.INTERVIEWING
            ApplicationStatus.INTERVIEWING -> ApplicationStatus.OFFER
            ApplicationStatus.OFFER -> null
        }

    val prevStatus: ApplicationStatus?
        get() = when (status) {
            ApplicationStatus.APPLIED -> null
            ApplicationStatus.INTERVIEWING -> ApplicationStatus.APPLIED
            ApplicationStatus.OFFER -> ApplicationStatus.INTERVIEWING
        }
}

/**
 * Realistic default dataset for academic campus placements and summer internships.
 */
object CareerMockData {
    val defaultApplications: List<CareerApplication> = listOf(
        // Applied column
        CareerApplication(
            id = "app-google",
            companyName = "Google",
            roleTitle = "Software Engineering Intern (Summer 2027)",
            type = CareerItemType.INTERNSHIP,
            status = ApplicationStatus.APPLIED,
            location = "Bangalore / Hyderabad",
            compensation = "₹1.40L / month",
            appliedDate = "Oct 18, 2026",
            nextMilestone = "Online Assessment (OA) Link",
            nextMilestoneDate = "Nov 05",
            interviewRound = "Screening Phase",
            referralContact = "Referred by Rohan Mehta (SWE III)",
            tags = listOf("Data Structures", "C++ / Java", "Off-Campus"),
            notes = "Applied via university referral link. Covered graph traversal & dynamic programming in prep."
        ),
        CareerApplication(
            id = "app-stripe",
            companyName = "Stripe",
            roleTitle = "Backend Infrastructure Intern",
            type = CareerItemType.INTERNSHIP,
            status = ApplicationStatus.APPLIED,
            location = "Remote / Bangalore",
            compensation = "₹1.50L / month",
            appliedDate = "Oct 22, 2026",
            nextMilestone = "Take-Home API Design Assignment",
            nextMilestoneDate = "Nov 08",
            interviewRound = "Application Under Review",
            referralContact = "Recruiter outreach on LinkedIn",
            tags = listOf("Distributed Systems", "Go / Kotlin"),
            notes = "Highlighted distributed transaction consistency and high-throughput Redis caching experience."
        ),
        CareerApplication(
            id = "app-network-de-shaw",
            companyName = "D.E. Shaw & Co.",
            roleTitle = "Quant Tech Networking & Coffee Chat",
            type = CareerItemType.NETWORKING,
            status = ApplicationStatus.APPLIED,
            location = "Hyderabad",
            compensation = "Alumni Outreach",
            appliedDate = "Oct 25, 2026",
            nextMilestone = "Virtual 1:1 with Senior VP of Engineering",
            nextMilestoneDate = "Nov 04",
            interviewRound = "Networking Sync",
            referralContact = "Ananya Roy (DTU Alum '21)",
            tags = listOf("Alumni Network", "FinTech", "System Design"),
            notes = "Shared SmartCampus IoT architecture paper and discussed low-latency trading infrastructure."
        ),

        // Interviewing column
        CareerApplication(
            id = "app-microsoft",
            companyName = "Microsoft",
            roleTitle = "Software Development Engineer (SDE-1)",
            type = CareerItemType.FULL_TIME,
            status = ApplicationStatus.INTERVIEWING,
            location = "Hyderabad / Noida",
            compensation = "₹44.5 LPA CTC",
            appliedDate = "Oct 05, 2026",
            nextMilestone = "Round 2: System Architecture & Concurrency",
            nextMilestoneDate = "Nov 03, 03:00 PM",
            interviewRound = "Technical Round 2 of 3",
            referralContact = "On-Campus Placement Cell (TPO DTU)",
            tags = listOf("On-Campus", "Cloud Systems", "Azure"),
            notes = "Passed Round 1 coding with 100% test cases (Trie prefix trees & Dijkstra). Focus on thread-safety next."
        ),
        CareerApplication(
            id = "app-cred",
            companyName = "CRED",
            roleTitle = "Product Backend Engineer Intern",
            type = CareerItemType.INTERNSHIP,
            status = ApplicationStatus.INTERVIEWING,
            location = "Bangalore (Indiranagar)",
            compensation = "₹1.20L / month",
            appliedDate = "Oct 10, 2026",
            nextMilestone = "Machine Coding & Live Refactoring",
            nextMilestoneDate = "Nov 06, 11:30 AM",
            interviewRound = "Round 2: Live Architecture",
            referralContact = "Hackathon Win Fast-Track",
            tags = listOf("Microservices", "Kafka", "PostgreSQL"),
            notes = "Live refactoring round: simulate concurrent flash-sale checkout with idempotency keys."
        ),

        // Offer column
        CareerApplication(
            id = "app-atlassian",
            companyName = "Atlassian",
            roleTitle = "Associate Software Engineer",
            type = CareerItemType.FULL_TIME,
            status = ApplicationStatus.OFFER,
            location = "Bengaluru (Hybrid)",
            compensation = "₹52.8 LPA CTC",
            appliedDate = "Sep 20, 2026",
            nextMilestone = "Formal Offer Acceptance Deadline",
            nextMilestoneDate = "Nov 15, 2026",
            interviewRound = "Offer Letter Received 🎉",
            referralContact = "PPO Conversion from 2026 Internship",
            tags = listOf("PPO Offer", "Distributed Systems", "Jira Core"),
            notes = "Received Pre-Placement Offer following Summer Internship rating of Exceeds Expectations!"
        ),
        CareerApplication(
            id = "app-network-uber",
            companyName = "Uber",
            roleTitle = "Staff Engineer Mentorship Milestone",
            type = CareerItemType.NETWORKING,
            status = ApplicationStatus.OFFER,
            location = "Bangalore",
            compensation = "Referral Secured",
            appliedDate = "Sep 15, 2026",
            nextMilestone = "Direct Referral Code Applied for Summer '27",
            nextMilestoneDate = "Completed",
            interviewRound = "Referral Confirmed",
            referralContact = "Karan Saxena (Staff Engineer, Uber Eats)",
            tags = listOf("Alumni Referral", "Backend", "Mentorship"),
            notes = "Mentorship milestone completed! Official referral code issued for University Talent Program."
        )
    )
}
