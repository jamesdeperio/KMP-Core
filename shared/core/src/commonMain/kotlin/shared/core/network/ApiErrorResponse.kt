package shared.core.network

import kotlinx.serialization.Serializable

/**
 * Standard generic error response wrapper used by all APIs.
 * Structure: {"status": Int, "error": T}
 */
@Serializable
data class ApiErrorResponse<T>(
    val status: Int,
    val error: T,
)
