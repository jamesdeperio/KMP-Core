package shared.core.util

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

/**
 * Returns true if the string is a valid email format.
 */
fun String.isValidEmail(): Boolean = this.matches(EMAIL_REGEX)

/**
 * Returns only the digits within a string. Useful for phone numbers or credit cards.
 */
fun String.digitsOnly(): String = filter { it.isDigit() }

/**
 * Returns a masked version of an email address (e.g., "jo***n@gmail.com").
 */
fun String.maskEmail(): String {
    val atIndex = indexOf("@")
    if (atIndex <= 2) return this
    return replaceRange(2, atIndex, "***")
}

/**
 * Capitalizes the first letter of each word in a string.
 */
fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
