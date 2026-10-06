package com.captainledger.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateTimeUtilsTest {

    @Test
    fun `test isSameDay returns true for same calendar day`() {
        val now = System.currentTimeMillis()
        val sameDayLater = now + 1000 * 60 * 30 // +30 mins
        assertTrue(DateTimeUtils.isSameDay(now, sameDayLater))
    }

    @Test
    fun `test isSameDay returns false for different calendar day`() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        assertFalse(DateTimeUtils.isSameDay(now, yesterday))
    }

    @Test
    fun `test formatDateOrTime returns time format for today`() {
        val now = System.currentTimeMillis()
        val formatted = DateTimeUtils.formatDateOrTime(now)
        val expectedTime = DateTimeUtils.formatTime(now)
        assertEquals(expectedTime, formatted)
    }

    @Test
    fun `test formatDateOrTime returns date format for past day`() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -2)
        }
        val pastDate = calendar.timeInMillis
        val formatted = DateTimeUtils.formatDateOrTime(pastDate)
        val expectedDate = DateTimeUtils.formatDate(pastDate)
        assertEquals(expectedDate, formatted)
    }

    @Test
    fun `test getStartOfToday is less than or equal to current time`() {
        val startOfToday = DateTimeUtils.getStartOfToday()
        val now = System.currentTimeMillis()
        assertTrue(startOfToday <= now)
    }

    @Test
    fun `test getStartOfWeek is less than or equal to start of today`() {
        val startOfWeek = DateTimeUtils.getStartOfWeek()
        val startOfToday = DateTimeUtils.getStartOfToday()
        assertTrue(startOfWeek <= startOfToday)
    }
}
