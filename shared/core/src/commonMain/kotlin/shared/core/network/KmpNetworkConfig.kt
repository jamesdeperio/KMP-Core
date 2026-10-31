package shared.core.network

/**
 * Generic configuration model for initializing the multiplatform HTTP client.
 */
data class KmpNetworkConfig(
    val baseUrl: String,
    val isDebugMode: Boolean = false,
    val payloadEncryptionEnabled: Boolean = false,
    val connectTimeoutMs: Long = NetworkConfig.CONNECT_TIMEOUT_MS,
    val requestTimeoutMs: Long = NetworkConfig.REQUEST_TIMEOUT_MS,
    val socketTimeoutMs: Long = NetworkConfig.SOCKET_TIMEOUT_MS,
)
