package shared.core.util

/**
 * Executes the given [block] and returns a [Result] containing the result or any [Exception] thrown.
 * This is useful for wrapping network or database calls in a standardized way.
 */
suspend fun <T> runSafe(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Returns the encapsulated value if this instance represents success or the result of [onFailure] function
 * if it is failure.
 */
inline fun <T> Result<T>.getOrElse(onFailure: (exception: Throwable) -> T): T {
    return when (val exception = exceptionOrNull()) {
        null -> getOrThrow()
        else -> onFailure(exception)
    }
}
