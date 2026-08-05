package shared.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeUtilsTest {

    @Test
    fun formatTime_formatsCorrectly() {
        assertEquals("1 second", formatTime(1))
        assertEquals("59 seconds", formatTime(59))
        assertEquals("1 minute", formatTime(60))
        assertEquals("1 minute 1 second", formatTime(61))
        assertEquals("2 minutes 5 seconds", formatTime(125))
    }

    @Test
    fun formatTimeShort_formatsCorrectly() {
        assertEquals("0 : 01", formatTimeShort(1))
        assertEquals("1 : 00", formatTimeShort(60))
        assertEquals("2 : 05", formatTimeShort(125))
    }

    @Test
    fun reformatDate_formatsCorrectly() {
        val input = "2024-08-05"
        assertEquals("05-08-2024", input.reformatDate("dd-MM-yyyy"))
        assertEquals("08-05-2024", input.reformatDate("MM-dd-yyyy"))
        assertEquals("2024-08-05", input.reformatDate("invalid-pattern"))
    }

    @Test
    fun toTimeAgo_returnsJustNowForCurrentTime() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        assertEquals("Just now", now.toTimeAgo())
    }
}
