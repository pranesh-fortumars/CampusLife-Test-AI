package com.example.data

import com.example.model.*

object MockData {
    val defaultProfile = StudentProfile()

    val initialSubjects = listOf(
        Subject(
            code = "CS-302",
            name = "Database Management Systems",
            credits = 4,
            faculty = "Dr. Ananya Roy",
            attendedClasses = 26,
            totalClasses = 30,
            nextClassTime = "Today, 10:30 AM",
            room = "CS Lab 304, Block B"
        ),
        Subject(
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            faculty = "Prof. K. Muralidhar",
            attendedClasses = 23,
            totalClasses = 31,
            nextClassTime = "Tomorrow, 9:15 AM",
            room = "LH-102"
        ),
        Subject(
            code = "CS-306",
            name = "Computer Networks",
            credits = 3,
            faculty = "Dr. R. Venkat",
            attendedClasses = 23,
            totalClasses = 29,
            nextClassTime = "Friday, 11:30 AM",
            room = "Room 214"
        ),
        Subject(
            code = "CS-308",
            name = "Software Engineering & Agile",
            credits = 3,
            faculty = "Prof. Neha Gupta",
            attendedClasses = 27,
            totalClasses = 30,
            nextClassTime = "Thursday, 2:45 PM",
            room = "Lab 2 (Software Wing)"
        ),
        Subject(
            code = "CS-312",
            name = "Machine Learning Elective",
            credits = 4,
            faculty = "Dr. Siddharth Sen",
            attendedClasses = 23,
            totalClasses = 28,
            nextClassTime = "Monday, 3:00 PM",
            room = "CS Seminar Hall 1"
        )
    )

    val assignments = listOf(
        AssignmentItem(
            id = "asgn-1",
            title = "Computer Networks Lab Assignment 4",
            subject = "Computer Networks",
            faculty = "Dr. R. Venkat",
            dueDate = "Today",
            dueTime = "5:00 PM",
            priority = "Urgent",
            internalWeightage = "15% Lab",
            isUrgent = true,
            draftFile = "packet_trace_wireshark.pcap (4.2 MB)",
            status = "In Progress",
            isCompleted = false
        ),
        AssignmentItem(
            id = "asgn-2",
            title = "DBMS Relational Calculus & B+ Trees Problem Set",
            subject = "Database Management Systems",
            faculty = "Dr. Ananya Roy",
            dueDate = "Tomorrow, Oct 25",
            dueTime = "11:59 PM",
            priority = "High Priority",
            internalWeightage = "10% Internal",
            isUrgent = false,
            progressCompleted = 3,
            progressTotal = 5,
            status = "In Progress",
            collaborators = listOf("Priya Nair")
        ),
        AssignmentItem(
            id = "asgn-3",
            title = "Operating Systems Semaphore Synchronization Project",
            subject = "Operating Systems",
            faculty = "Prof. K. Muralidhar",
            dueDate = "Oct 29",
            dueTime = "11:59 PM",
            priority = "Medium Priority",
            internalWeightage = "Group Lab (3)",
            collaborators = listOf("Priya Nair", "Rohan M."),
            status = "Reviewing draft"
        ),
        AssignmentItem(
            id = "asgn-4",
            title = "Agile Sprint Retrospective & Jira Report",
            subject = "Software Engineering",
            faculty = "Prof. Neha Gupta",
            dueDate = "Nov 2",
            dueTime = "5:00 PM",
            priority = "Normal Priority",
            internalWeightage = "Individual",
            status = "Not Started"
        )
    )

    val examSchedule = listOf(
        ExamScheduleItem("CS-304", "OS", "Nov 4", "10:00 AM", "LH-302"),
        ExamScheduleItem("CS-302", "DBMS", "Nov 6", "02:00 PM", "LH-302"),
        ExamScheduleItem("CS-306", "CN", "Nov 8", "10:00 AM", "LH-302")
    )

    val thursdaySlots = listOf(
        TimetableSlot(
            id = "slot-1",
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            faculty = "Prof. K. Muralidhar",
            facultyAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuDTNkBuGRpZf7mA4YPpkrjEgXUMYd3JI39mInHe9yD_6dD6TQm4Y9XiBHn1Vn-I_2yUn9rFBo1vIW84PaAcd3SeX1iegUEsdg67-cqfZ2vdKmRZFMKskz-wn_lJl8qGfnJab-p_PmGCsTjSt_7CSKuBOLIRzo11hppUeoxdg9mY7gY36gj5XBVhGKYkzhVtB_WGeAUm029OIwz9vBSxKHkIYSZwwSkGMeIaJYnXRMNaCcUGM0B98nww2A",
            timeStart = "09:00",
            timeEnd = "10:15",
            room = "LH-102",
            isAttended = true
        ),
        TimetableSlot(
            id = "slot-2",
            code = "CS-302",
            name = "Database Management Systems",
            credits = 3,
            faculty = "Dr. Ananya Roy",
            facultyAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuAJB3UuFLKjF7fY-JJ-VHUTBz-jZkW5f9UXV551Cc16IDX7103qyy-R52jeY7GEqSZ1Gdn3BW63gGIxFFBDZ5d8L3XpuOKSLtOZSeNCUz3R9uHOJlMs2ZjXd8DWxGzpsQmmRxoHXWjgjsdj8Ezci7jhtGteZREwR2AwtYotDeaLe9ntEqTx8_F5iy2mF9y9eGu1sYIvxSuS5VMRW-Dk0zHTV19VMOECDCJiX7f9w7Oride9hJsaSZ1WxA",
            timeStart = "10:30",
            timeEnd = "11:45",
            room = "CS Lab 304",
            isLiveNow = true
        ),
        TimetableSlot(
            id = "slot-3",
            code = "BREAK",
            name = "Lunch Break & Canteen",
            credits = 0,
            faculty = "Auditorium Cafeteria",
            facultyAvatar = "",
            timeStart = "12:00",
            timeEnd = "01:00",
            room = "60 mins",
            isBreak = true
        ),
        TimetableSlot(
            id = "slot-4",
            code = "CS-306",
            name = "Computer Networks",
            credits = 4,
            faculty = "Dr. R. Venkat",
            facultyAvatar = "",
            timeStart = "01:15",
            timeEnd = "02:30",
            room = "Room 214"
        ),
        TimetableSlot(
            id = "slot-5",
            code = "CS-308",
            name = "SE Agile Lab Practicum",
            credits = 2,
            faculty = "Prof. Neha Gupta",
            facultyAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuDe3G4oOFIrnQ0_R_m77NQc5KWrbacDueH0yuWDzSpTiaAnccpe8b5OENfQzIq0QPQinDE4iXKBvnY1mynlxudzGc9TK5R087_MhG8GtPSONZ9qIGahLa4BygOTA4jE4Hskt0xdULC2AZZijB_NKfbVSmTFgXJtrHfOsVKqUV8KY0FyTNWgvkh1JyhscUEWUlIB49hh0_AxL0QsNtS0h8VCa72WtiL2qFNTbeeNdChhmkgCVTAhNKN1rg",
            timeStart = "02:45",
            timeEnd = "04:45",
            room = "Lab 2 (Software Wing)"
        )
    )

    val projects = listOf(
        ProjectItem(
            id = "proj-1",
            title = "SmartCampus Parking IoT & Automated Allocation",
            description = "Computer vision & MQTT edge gateway for university vehicle triage. Eliminated 25-minute car parking queues at DTU North Gate. Formal adoption by DTU Proctorial Board for 1,400 daily vehicle transits.",
            category = "systems",
            techStack = listOf("Python", "OpenCV", "FastAPI", "React Native", "MQTT", "Docker"),
            stars = 420,
            status = "Deployed in DTU",
            bannerUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDF9QR66D6zvIJCBW4wc2Z4xTOd1xykcVkvBvpepl_MLViYXTnB4_OD0vIfAmrFH4fco3sONpvIVAgruTOiUVyi20BUJasSpAb9FqgupkfSgncXYYIEp6PW9zmhjY-J7f6Bu0oMGgs3U4KxFudIw7_eFwWQi6SW2CuozwuSKLJ7PvzrEkNCJIr_Hgd964B2DdirLtx8L9wy34ROibhS_cNdxWSxqPZd1vPewjJuC0F1dPNfjbQ8hotHJg",
            collaborators = listOf("Rahul (Lead)", "Priya", "Kabir"),
            isFeatured = true,
            impactNote = "Eliminated 25-minute car parking queues at DTU North Gate."
        ),
        ProjectItem(
            id = "proj-2",
            title = "CampusLife Peer Notes & Vault",
            description = "Collaborative academic repository with OCR search, verified professor lecture notes, and semester-wise past year solutions.",
            category = "web",
            techStack = listOf("Next.js 14", "Tailwind", "PostgreSQL", "Meilisearch"),
            stars = 184,
            status = "In Progress • Beta Testing (CSE Dept)",
            progress = 85
        ),
        ProjectItem(
            id = "proj-3",
            title = "Distributed Cache & Raft Store",
            description = "Built-from-scratch Go implementation of Raft consensus protocol with distributed log replication, state snapshotting, and WAL storage.",
            category = "systems",
            techStack = listOf("Golang", "gRPC", "Protobuf", "Docker"),
            stars = 512,
            status = "Completed • Open Source Engine"
        ),
        ProjectItem(
            id = "proj-4",
            title = "EdTech Real-Time Quiz Engine",
            description = "Real-time adaptive testing platform featuring sub-second synchronization, WebSocket live polling, and Redis sliding leaderboard.",
            category = "web",
            techStack = listOf("Node.js", "Express", "MongoDB", "Socket.io", "Redis"),
            stars = 124,
            status = "Milestone 3/4 • Socket Mesh"
        )
    )

    val societies = listOf(
        SocietyItem(
            id = "soc-1",
            name = "ACM DTU Chapter",
            category = "Technical",
            memberCount = 480,
            lead = "Rahul Sharma (Pres) & Priya Nair (VP)",
            statusBadge = "Top Technical 2024",
            nextEventTitle = "HackCampus 2024 • 36h Sprint",
            nextEventTime = "Nov 16-17",
            tags = listOf("Competitive Coding", "Hackathons", "Research"),
            isMember = true,
            logoText = "</>"
        ),
        SocietyItem(
            id = "soc-2",
            name = "GDSC DTU",
            category = "Technical",
            memberCount = 350,
            lead = "Aditya K.",
            statusBadge = "18 Workshops",
            nextEventTitle = "Kubernetes & Docker Hands-on Lab",
            nextEventTime = "Tomorrow, 4:00 PM",
            tags = listOf("Flutter", "Google Cloud", "Open Source"),
            isMember = true
        ),
        SocietyItem(
            id = "soc-3",
            name = "Pratibimb — Dramatics",
            category = "Cultural",
            memberCount = 120,
            lead = "Sneha & Shreyas",
            statusBadge = "4 Nat. Trophies",
            nextEventTitle = "Annual Street Auditions",
            nextEventTime = "Open until Oct 28",
            tags = listOf("Street Play (Nukkad)", "Monologue", "Scriptwriting"),
            isMember = false
        ),
        SocietyItem(
            id = "soc-4",
            name = "E-Cell DTU",
            category = "Startup",
            memberCount = 280,
            lead = "Karan V.",
            statusBadge = "₹1.5 Cr Seed Fund",
            nextEventTitle = "Campus Pitch Day with Peak XV Partners",
            nextEventTime = "Oct 30",
            tags = listOf("Pitching", "Angel Funding", "Incubation"),
            isMember = false
        ),
        SocietyItem(
            id = "soc-5",
            name = "Aura — Design Collective",
            category = "Creative",
            memberCount = 195,
            lead = "Tanvi M.",
            statusBadge = "Fest Branding",
            nextEventTitle = "3D Blender & UI Workshop",
            nextEventTime = "Saturday, 2 PM",
            tags = listOf("UI/UX", "Blender 3D", "Poster Design"),
            isMember = false
        )
    )

    val timelineNodes = listOf(
        TimelineNode(
            id = "tm-1",
            semesterTag = "sem6",
            dateLabel = "Oct 2024 • Sem 6",
            title = "Elected Class Representative & Started Major Project",
            description = "Chosen as Section B CR. Initiated Distributed Systems campus microservices architecture capstone under Dr. S. Rao.",
            categoryBadge = "Elected",
            tags = listOf("Leadership", "Go / gRPC")
        ),
        TimelineNode(
            id = "tm-2",
            semesterTag = "sem5",
            dateLabel = "Jul 2024 • Summer",
            title = "Completed 8-Week Software Internship at TechCorp Bangalore",
            description = "Built latency-sensitive caching layers for high-throughput messaging infrastructure. Received pre-placement interview invite.",
            categoryBadge = "Internship",
            tags = listOf("Redis", "Kubernetes", "Certificate")
        ),
        TimelineNode(
            id = "tm-3",
            semesterTag = "sem5",
            dateLabel = "Apr 2024 • Sem 5",
            title = "Secured SGPA 8.78 • 1st Runner Up in Inter-College Hackathon",
            description = "36-hour hackathon winning project: AI-driven autonomous campus lost-and-found mesh tracker built with 3 batchmates.",
            categoryBadge = "Podium",
            tags = listOf("SGPA 8.78", "HackDTU")
        ),
        TimelineNode(
            id = "tm-4",
            semesterTag = "sem4",
            dateLabel = "Dec 2023 • Sem 4",
            title = "Organized TechKriti '23 Workshop for 300+ Freshers",
            description = "Conducted hands-on Git, Linux command line, and Docker starter sessions in Pragati Hall Auditorium.",
            categoryBadge = "Organizing",
            tags = listOf("Mentorship", "Linux")
        ),
        TimelineNode(
            id = "tm-5",
            semesterTag = "sem1",
            dateLabel = "Aug 2022 • Sem 1",
            title = "Day 1 at DTU — Orientation & Joined Coding Society",
            description = "Step onto Bawana Road campus for central registration. Enrolled in Crio Developers Club as a rookie contributor.",
            categoryBadge = "Day 1",
            tags = listOf("Orientation", "Freshers")
        )
    )

    val skills = listOf(
        SkillItem("Data Structures & Algorithms", "Advanced", 4.5, 14, 0.90f),
        SkillItem("React & TypeScript", "Proficient", 4.0, 9, 0.80f),
        SkillItem("Database Systems (SQL)", "Proficient", 4.0, 12, 0.80f),
        SkillItem("Cloud / AWS", "Intermediate", 3.0, 6, 0.60f)
    )

    val conversations = listOf(
        ChatConversation(
            id = "conv-1",
            title = "CS-302 DBMS Mid-Sem",
            subtitle = "Priya Nair: Uploaded the handwritten notes for Unit 3 BCNF proofs! 📄",
            time = "10:42 AM",
            unreadCount = 3,
            type = "groups",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBTu4_n-XWIAQ24f7n60nAETiRLQkFVq7KiiCWu8jyweBe37_heEi-tyD4AYs9G77p9jBPryMY96MDcAiW5t9gCFEdIxCg0PMxplKD8c4oWnXyErF2vaTCLaA5G5eSpTFF-ZFNfGDpYWH5zR2BX0MaBRi4Z6tiF_hYdji_aWqrAMNyw92MaEmEE3L2j4UeGryWyFfCSu0acrp94iI6Yo2f1nWazwmMcGwDVqrYyRMJx8QY0GcNidkVmnQ",
            attachmentName = "BCNF_Tutorial_Sheet.pdf (2.4 MB)",
            isLiveAudioHuddle = true
        ),
        ChatConversation(
            id = "conv-2",
            title = "HackCampus '24 (ByteCraft)",
            subtitle = "Kabir V.: Deployed the IoT endpoint on AWS EC2 micro, ready for testing.",
            time = "9:15 AM",
            unreadCount = 0,
            type = "groups",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBeXKo0zDw5qetCfL651-p9RBvIs90ypHBhuTxrarqsYN1WdSPJFZLgILjcMnSFXWYmM83BQlK0XgTUnM6iu5BxdSqN0HvNRie84tp1X-7q8SfSPcLxlXeQPAZbxCXjZKLwiCR6BXTgm_CgnmTjpmlL16seIC82fKI6p6k5IXTg_FfJI4pw-5s8zMHruhkoVP14r5DrKDAyF5V3eH3HiMIdJjU28jkVE6LNLgKCV7UkmCNoDxtfC6QwWA"
        ),
        ChatConversation(
            id = "conv-3",
            title = "Prof. Dr. Ananya Roy",
            subtitle = "Please bring your lab journal for evaluation tomorrow at 10:30 AM.",
            time = "Yesterday",
            unreadCount = 0,
            type = "dms",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBbauEX2Y_eIrkYngq0so5Z4KPNKbVYsnhyDg6o8fvVbvJTAu9PndwKuCDJ_Fzph_dc0cKNgFSz6KsbQM6hAqflxlBSu5YqfAMcKyv4hhgDmLaheCIPLsnnZnVK_3QoZUDWULFjPbsKi-v2t297V8Qap5kLjWRXNeMAdYO6y7FEVHtqF7CeP8bWyNq_q2xEERSK6aOGsoSXhIws2gsd-1PKgvRTCF9cHcD7Fgf73GFWK2reU_vaszCyiw",
            isFaculty = true
        ),
        ChatConversation(
            id = "conv-4",
            title = "Priya Nair",
            subtitle = "Hey Rahul, did you submit the attendance waiver sheet to the HOD?",
            time = "Yesterday",
            unreadCount = 1,
            type = "dms",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1v-AQquQACgtoQt1f37Pm29He4LoyBcqX6TMMKPgG7WBCzSICvIJJYw6PhIeB6ixWwNj0pCWWy67fVx5RxUrbAloc_Lieggqt1JB9JPU5V5lZbrN0p5LVmsYJpEHtQzIDiBWLEs6y8kW0HV7Ij27RnEHGHBEIkRssB64G_hhRLojYlpORChgmH1PTqh9rJXQjzgyh9AfiDjhR_R-u5r8j0cecWzaOEQSHGpSaij9DOW_wv-NoJTIG-Q",
            isOnline = true
        ),
        ChatConversation(
            id = "conv-5",
            title = "ACM DTU Council",
            subtitle = "Announcement: HackCampus registration deadline extended by 24h! 🚀",
            time = "Oct 23",
            unreadCount = 0,
            type = "channels",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAssTLlOQ9isAfOgqHLO2fzLcL1KklunmqF3Dhaj9E9NKHuRr2mb3kn1-PIqLrAFSImcaW_wag3VW2Fmzof3q0AnAdGwD844lfn-tTSLYHXRNmFtnQWlY1Wiv1JyY6XKDgWSIL_j3QI5DSAeA1kuB2ZnVfT40pPR4q4z9OBHwEcrTAQgVrpqkEL3256Vc6b6zZQtVwb8i8i6Mm_XTwChC0hEwRTWaTVWM1kD53q0028_X8QwsBP0yPBLg"
        ),
        ChatConversation(
            id = "conv-6",
            title = "Tanmay Gupta",
            subtitle = "I have the Arduino jumper wires and breadboard for next class.",
            time = "Oct 22",
            unreadCount = 0,
            type = "dms",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDcFBtrZiRomGEO1eqS9GtUlG1U8-00eJx8jVVaETIRdNtWglTHCoeMFAUQqeQjbsXVKAkYIzW8RPUvQw9WcJjsXFdvw3NGGWtEJQ7PA1U_PuwMgkTb_X53RI6znT-0SsgxxXgrrYBBjbuVrsKWBsBCTkRPjlF_o2S0SaTg0ZcMdJnONie8YoSWWld9LE7pU_0GPgEjSwP2V-3HEx_ZMz8FtVr0cTRm6wu3PfJBJfBFmavVD3A69k6ulw"
        )
    )
}
