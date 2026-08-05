package shared.core.network

import kotlinx.serialization.Serializable

/**
 * Common error data structure for API errors.
 */
@Serializable
data class BaseErrorData(
    val code: Int? = null,
    val message: String? = null,
    val email: String? = null,
    val data: String? = null,
)
