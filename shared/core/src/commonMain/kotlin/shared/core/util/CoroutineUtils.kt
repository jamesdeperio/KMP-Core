package shared.core.util

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Retries a suspending [block] with exponential backoff.
 * Useful for flaky network calls.
 */
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            // Log if needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

/**
 * Ensures a block of code runs even if the parent coroutine scope is cancelled.
 * Useful for cleanup tasks or logging logout events.
 */
suspend fun <T> runNonCancellable(block: suspend () -> T): T {
    return withContext(NonCancellable) {
        block()
    }
}
