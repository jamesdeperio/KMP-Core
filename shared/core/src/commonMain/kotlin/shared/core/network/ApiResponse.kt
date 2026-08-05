package shared.core.network

import kotlinx.serialization.Serializable

/**
 * Standard generic success response wrapper used by all APIs.
 * Structure: {"status": Int, "data": T, "message": String}
 */
@Serializable
data class ApiResponse<T>(
    val status: Int,
    val data: T? = null,
    val message: String? = null,
)
