package shared.core.util

import shared.core.Config

/**
 * Simple cross-platform logger that only prints output in debug mode.
 *
 * In production builds ([shared.core.Config.isDebugMode] is false), these methods
 * will produce no output to ensure security and performance.
 */
object Logger {
    /**
     * Logs a debug message.
     * @param tag The category of the log (e.g., "Network", "Database").
     * @param message The information to log.
     */
    fun d(
        tag: String,
        message: String,
    ) {
        if (Config.isDebugMode) {
            println("DEBUG: [$tag] $message")
        }
    }

    /**
     * Logs an error message and an optional exception.
     * @param tag The category of the log.
     * @param message A description of the error.
     * @param throwable The stack trace of the exception.
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (Config.isDebugMode) {
            println("ERROR: [$tag] $message")
            throwable?.printStackTrace()
        }
    }
}

/**
 * Idiomatic debug logging. Automatically uses the class name as the tag.
 * Usage: logD("My message")
 */
inline fun <reified T> T.logD(message: String) {
    Logger.d(T::class.simpleName ?: "Unknown", message)
}

/**
 * Idiomatic error logging. Automatically uses the class name as the tag.
 * Usage: logE("My message", throwable)
 */
inline fun <reified T> T.logE(
    message: String,
    throwable: Throwable? = null,
) {
    Logger.e(T::class.simpleName ?: "Unknown", message, throwable)
}
