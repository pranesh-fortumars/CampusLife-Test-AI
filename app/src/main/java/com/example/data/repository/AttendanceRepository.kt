package com.example.data.repository

import com.example.data.MockData
import com.example.data.local.AttendanceDao
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.SubjectEntity
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class AttendanceRepository(private val dao: AttendanceDao) {

    val subjects: Flow<List<Subject>> = dao.getAllSubjects().map { entities ->
        entities.map { it.toDomain() }
    }

    val allRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getRecordsForSubject(subjectCode: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceForSubject(subjectCode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllSubjects().first()
        if (existing.isEmpty()) {
            val initialEntities = MockData.initialSubjects.map { subject ->
                SubjectEntity(
                    code = subject.code,
                    name = subject.name,
                    credits = subject.credits,
                    faculty = subject.faculty,
                    department = subject.department,
                    attendedClasses = subject.attendedClasses,
                    totalClasses = subject.totalClasses,
                    targetPercentage = subject.targetPercentage,
                    nextClassTime = subject.nextClassTime,
                    room = subject.room,
                    isLabIncluded = subject.isLabIncluded
                )
            }
            dao.insertSubjects(initialEntities)

            // Seed a few initial historical records so the student sees real recent attendance history
            val now = System.currentTimeMillis()
            val sampleRecords = listOf(
                AttendanceRecordEntity(
                    id = "seed-1",
                    subjectCode = "CS-302",
                    subjectName = "Database Management Systems",
                    date = "Today, Oct 14",
                    time = "10:30 AM",
                    type = "ATTENDED",
                    note = "B+ Trees and Leaf Overflow",
                    timestamp = now - 3600000
                ),
                AttendanceRecordEntity(
                    id = "seed-2",
                    subjectCode = "CS-304",
                    subjectName = "Operating Systems",
                    date = "Yesterday, Oct 13",
                    time = "09:15 AM",
                    type = "MISSED",
                    note = "Attended HackCampus squad meeting",
                    timestamp = now - 86400000
                ),
                AttendanceRecordEntity(
                    id = "seed-3",
                    subjectCode = "CS-306",
                    subjectName = "Computer Networks",
                    date = "Oct 12",
                    time = "01:15 PM",
                    type = "ATTENDED",
                    note = "Wireshark packet capture lab",
                    timestamp = now - 172800000
                ),
                AttendanceRecordEntity(
                    id = "seed-4",
                    subjectCode = "CS-308",
                    subjectName = "Software Engineering & Agile",
                    date = "Oct 11",
                    time = "02:45 PM",
                    type = "ATTENDED",
                    note = "Sprint 2 Retrospective Lab",
                    timestamp = now - 259200000
                )
            )
            for (rec in sampleRecords) {
                dao.insertRecord(rec)
            }
        }
    }

    suspend fun logAttendance(
        subjectCode: String,
        type: AttendanceType,
        note: String = "",
        customDate: String? = null,
        customTime: String? = null
    ) {
        val currentSubjects = dao.getAllSubjects().first()
        val subjectEntity = currentSubjects.find { it.code == subjectCode } ?: return

        val newAttended = when (type) {
            AttendanceType.ATTENDED -> subjectEntity.attendedClasses + 1
            AttendanceType.MISSED -> subjectEntity.attendedClasses
            AttendanceType.CANCELLED -> subjectEntity.attendedClasses
        }

        val newTotal = when (type) {
            AttendanceType.ATTENDED -> subjectEntity.totalClasses + 1
            AttendanceType.MISSED -> subjectEntity.totalClasses + 1
            AttendanceType.CANCELLED -> subjectEntity.totalClasses
        }

        dao.updateSubject(
            subjectEntity.copy(
                attendedClasses = newAttended,
                totalClasses = newTotal
            )
        )

        val dateStr = customDate ?: SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        val timeStr = customTime ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        dao.insertRecord(
            AttendanceRecordEntity(
                id = UUID.randomUUID().toString(),
                subjectCode = subjectEntity.code,
                subjectName = subjectEntity.name,
                date = dateStr,
                time = timeStr,
                type = type.name,
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun undoLastRecordForSubject(subjectCode: String) {
        val records = dao.getAttendanceForSubject(subjectCode).first()
        val lastRecord = records.firstOrNull() ?: return

        val currentSubjects = dao.getAllSubjects().first()
        val subjectEntity = currentSubjects.find { it.code == subjectCode } ?: return

        val type = try { AttendanceType.valueOf(lastRecord.type) } catch (e: Exception) { AttendanceType.ATTENDED }

        val newAttended = when (type) {
            AttendanceType.ATTENDED -> (subjectEntity.attendedClasses - 1).coerceAtLeast(0)
            AttendanceType.MISSED -> subjectEntity.attendedClasses
            AttendanceType.CANCELLED -> subjectEntity.attendedClasses
        }

        val newTotal = when (type) {
            AttendanceType.ATTENDED -> (subjectEntity.totalClasses - 1).coerceAtLeast(0)
            AttendanceType.MISSED -> (subjectEntity.totalClasses - 1).coerceAtLeast(0)
            AttendanceType.CANCELLED -> subjectEntity.totalClasses
        }

        dao.updateSubject(
            subjectEntity.copy(
                attendedClasses = newAttended,
                totalClasses = newTotal
            )
        )

        dao.deleteRecordById(lastRecord.id)
    }

    suspend fun updateCounts(subjectCode: String, attended: Int, missed: Int, targetPercentage: Double? = null) {
        val currentSubjects = dao.getAllSubjects().first()
        val subjectEntity = currentSubjects.find { it.code == subjectCode } ?: return

        val safeAttended = attended.coerceAtLeast(0)
        val safeMissed = missed.coerceAtLeast(0)
        val safeTotal = safeAttended + safeMissed

        dao.updateSubject(
            subjectEntity.copy(
                attendedClasses = safeAttended,
                totalClasses = safeTotal,
                targetPercentage = targetPercentage ?: subjectEntity.targetPercentage
            )
        )
    }

    suspend fun addSubject(subject: Subject) {
        dao.insertSubject(
            SubjectEntity(
                code = subject.code.trim().uppercase(),
                name = subject.name.trim(),
                credits = subject.credits,
                faculty = subject.faculty.trim(),
                department = subject.department.trim(),
                attendedClasses = subject.attendedClasses.coerceAtLeast(0),
                totalClasses = subject.totalClasses.coerceAtLeast(0),
                targetPercentage = subject.targetPercentage,
                nextClassTime = subject.nextClassTime,
                room = subject.room,
                isLabIncluded = subject.isLabIncluded
            )
        )
    }

    suspend fun deleteSubject(code: String) {
        dao.deleteSubjectByCode(code)
        dao.deleteRecordsForSubject(code)
    }

    private fun SubjectEntity.toDomain(): Subject {
        return Subject(
            code = code,
            name = name,
            credits = credits,
            faculty = faculty,
            department = department,
            attendedClasses = attendedClasses,
            totalClasses = totalClasses,
            targetPercentage = targetPercentage,
            nextClassTime = nextClassTime,
            room = room,
            isLabIncluded = isLabIncluded
        )
    }

    private fun AttendanceRecordEntity.toDomain(): AttendanceRecord {
        val attType = try {
            AttendanceType.valueOf(type)
        } catch (e: Exception) {
            AttendanceType.ATTENDED
        }
        return AttendanceRecord(
            id = id,
            subjectCode = subjectCode,
            subjectName = subjectName,
            date = date,
            time = time,
            type = attType,
            note = note,
            timestamp = timestamp
        )
    }
}
