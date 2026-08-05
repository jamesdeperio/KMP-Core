package shared.core.network

/**
 * Base API Exception that contains a code and optional data.
 * Reusable across different features for handling domain-specific network errors.
 */
class APIException(
    val code: Int,
    override val message: String,
    val data: Any? = null,
) : Exception(message)
