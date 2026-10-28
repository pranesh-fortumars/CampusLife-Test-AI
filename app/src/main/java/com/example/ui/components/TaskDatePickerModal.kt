package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for formatting and parsing dates selected via Material Design 3 DatePicker.
 * M3 DatePicker's selectedDateMillis is UTC-normalized midnight.
 */
object TaskDateUtils {
    private val utcTimeZone = TimeZone.getTimeZone("UTC")

    /**
     * Formats epoch milliseconds from M3 DatePicker into a readable display string: e.g. "Oct 30, 2026"
     */
    fun formatEpochMillisToDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US).apply {
            timeZone = utcTimeZone
        }
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch milliseconds into a concise short date string: e.g. "Oct 30"
     */
    fun formatEpochMillisToShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.US).apply {
            timeZone = utcTimeZone
        }
        return sdf.format(Date(epochMillis))
    }

    /**
     * Parses a date string back to epoch milliseconds in UTC.
     * Handles formatted dates like "Oct 30, 2026", "2026-10-30", and relative labels like "Today", "Tomorrow".
     */
    fun parseDateStringToEpochMillis(dateStr: String): Long {
        val trimmed = dateStr.trim()
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L

        return when {
            trimmed.equals("Today", ignoreCase = true) -> now
            trimmed.equals("Tomorrow", ignoreCase = true) -> now + oneDayMillis
            trimmed.contains("3 Days", ignoreCase = true) -> now + (3 * oneDayMillis)
            trimmed.contains("Week", ignoreCase = true) -> now + (7 * oneDayMillis)
            else -> {
                // Try "MMM dd, yyyy"
                try {
                    val sdf1 = SimpleDateFormat("MMM dd, yyyy", Locale.US).apply { timeZone = utcTimeZone }
                    sdf1.parse(trimmed)?.time ?: (now + oneDayMillis)
                } catch (e1: Exception) {
                    try {
                        val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utcTimeZone }
                        sdf2.parse(trimmed)?.time ?: (now + oneDayMillis)
                    } catch (e2: Exception) {
                        try {
                            val sdf3 = SimpleDateFormat("MMM dd", Locale.US).apply { timeZone = utcTimeZone }
                            val parsed = sdf3.parse(trimmed)
                            if (parsed != null) {
                                val cal = Calendar.getInstance(utcTimeZone).apply { time = parsed }
                                val currentYear = Calendar.getInstance(utcTimeZone).get(Calendar.YEAR)
                                cal.set(Calendar.YEAR, currentYear)
                                cal.timeInMillis
                            } else {
                                now + oneDayMillis
                            }
                        } catch (e3: Exception) {
                            now + oneDayMillis
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material Design 3 DatePickerDialog that allows users to interactively select
 * a specific due date for academic tasks stored in Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    initialSelectedDateMillis: Long? = null,
    onDateSelected: (selectedDateMillis: Long, formattedDate: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Current UTC millis or provided timestamp
    val initialMillis = initialSelectedDateMillis ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayedMonthMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = TaskDateUtils.formatEpochMillisToDate(millis)
                        onDateSelected(millis, formatted)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("date_picker_confirm_btn")
            ) {
                Text(
                    text = "Save Date",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("date_picker_cancel_btn")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("material_date_picker_dialog")
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SELECT TASK DUE DATE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            headline = {
                val selectedMillis = datePickerState.selectedDateMillis
                val headlineText = if (selectedMillis != null) {
                    TaskDateUtils.formatEpochMillisToDate(selectedMillis)
                } else {
                    "Choose deadline"
                }
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            },
            showModeToggle = true
        )
    }
}
