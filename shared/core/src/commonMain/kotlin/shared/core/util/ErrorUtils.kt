package shared.core.util

import shared.core.network.APIException
import shared.core.security.SecurityAuthenticationRequiredException

/**
 * Resolves a [Throwable] into a user-facing message.
 * Prioritizes the message coming from the API (APIException).
 */
fun Throwable.resolveMessage(): String {
    return when (this) {
        is APIException -> message
        is SecurityAuthenticationRequiredException -> "Authentication required to access secure data."
        is kotlinx.io.IOException -> "Network error. Please check your connection."
        else -> message ?: "An unexpected error occurred."
    }
}
