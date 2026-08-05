package shared.core.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return when {
        minutes > 0 && remainingSeconds > 0 -> {
            "$minutes minute${if (minutes > 1) "s" else ""} $remainingSeconds second${if (remainingSeconds > 1) "s" else ""}"
        }
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
        else -> "$remainingSeconds second${if (remainingSeconds > 1) "s" else ""}"
    }
}

fun formatTimeShort(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes : ${remainingSeconds.toString().padStart(2, '0')}"
}

fun getCurrentHour(): Int {
    val nowInstant = Clock.System.now()
    val localDateTime = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.hour
}
/*
@OptIn(ExperimentalTime::class)
fun getCurrentHour(): Int {
    val nowInstant = Clock.System.now()
    val localDateTime = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.hour
}

 */

fun String.reformatDate(outputPattern: String = "dd-MM-yyyy"): String {
    val parts = this.split("-")
    if (parts.size != 3) return this

    return when (outputPattern) {
        "dd-MM-yyyy" -> "${parts[2]}-${parts[1]}-${parts[0]}"
        "MM-dd-yyyy" -> "${parts[1]}-${parts[2]}-${parts[0]}"
        else -> this // fallback
    }
}

/**
 * Returns a human-readable "time ago" string (e.g., "3 minutes ago").
 */
fun Long.toTimeAgo(): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - this
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> "Long ago" // For more precision, use a proper date formatter
    }
}

/**
 * Returns current timestamp in ISO 8601 format.
 */
fun getCurrentIsoTimestamp(): String {
    return Clock.System.now().toString()
}
