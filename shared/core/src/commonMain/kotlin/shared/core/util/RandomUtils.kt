package shared.core.util

import kotlin.random.Random

/**
 * Generates a random alphanumeric string of a given length.
 * Useful for temporary IDs, nonces, or salt values in common code.
 */
fun generateRandomString(length: Int = 16): String {
    val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { Random.nextInt(0, charPool.length).let { charPool[it] } }
        .joinToString("")
}

/**
 * Returns a random element from a list, or null if the list is empty.
 */
fun <T> List<T>.randomOrNull(): T? {
    if (isEmpty()) return null
    return this[Random.nextInt(size)]
}
