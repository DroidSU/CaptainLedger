package com.captainledger.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility class for date/time calculations and formatting across the application.
 */
object DateTimeUtils {

    /**
     * Checks if two timestamps fall on the exact same calendar day.
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long = System.currentTimeMillis()): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return (cal1[Calendar.YEAR] == cal2[Calendar.YEAR]) &&
                (cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR])
    }

    /**
     * Formats a timestamp:
     * - Returns time (e.g. "10:30 AM") if the timestamp is on today's date.
     * - Returns date (e.g. "24 Oct") if it's on a different day.
     */
    fun formatDateOrTime(timestamp: Long): String {
        return if (isSameDay(timestamp)) {
            formatTime(timestamp)
        } else {
            formatDate(timestamp)
        }
    }

    /**
     * Formats timestamp into 12-hour time format (e.g. "10:30 AM").
     */
    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Formats timestamp into short date format (e.g. "24 Oct").
     */
    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Formats timestamp for notification debug logs (e.g. "14:32:05 • 24 Oct").
     */
    fun formatDebugLogTimestamp(timestamp: Long): String {
        return SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Returns the timestamp for the start of today (00:00:00.000).
     */
    fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Returns the timestamp for the start of the current week (00:00:00.000).
     */
    fun getStartOfWeek(): Long {
        return Calendar.getInstance().apply {
            val firstDay = firstDayOfWeek
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val currentDay = get(Calendar.DAY_OF_WEEK)
            val daysToSubtract = (currentDay - firstDay + 7) % 7
            add(Calendar.DAY_OF_MONTH, -daysToSubtract)
        }.timeInMillis
    }
}
