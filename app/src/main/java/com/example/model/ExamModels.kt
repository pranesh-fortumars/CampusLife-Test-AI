package com.example.model

data class SubjectTargetMarks(
    val code: String,
    val name: String,
    val credits: Int,
    val targetGrade: String, // 'O', 'A+', etc.
    val internalScored: Int,
    val internalMax: Int = 20,
    val midSemMin: Int,
    val midSemMax: Int = 30,
    val endSemMin: Int,
    val endSemMax: Int = 50,
    val statusNote: String,
    val isPriority: Boolean = false,
    val isSafe: Boolean = true
)

data class RevisionTopic(
    val title: String,
    val isDone: Boolean,
    val estimateTime: String? = null
)

data class RevisionSprint(
    val examDate: String,
    val title: String,
    val subtitle: String,
    val readinessPct: Int,
    val topics: List<RevisionTopic>,
    val flashcardLink: String? = null
)
