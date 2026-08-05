package shared.core.network

import shared.core.BuildKonfig

/**
 * Single source of truth for network configuration.
 * Swap `baseUrl` per build variant/flavor (debug vs release, staging vs prod).
 */
object NetworkConfig {
    /** Base URL of the API, pulled from BuildKonfig (environment variables). */
    val baseUrl: String = BuildKonfig.BASE_URL

    const val CONNECT_TIMEOUT_MS = 15_000L
    const val REQUEST_TIMEOUT_MS = 30_000L
    const val SOCKET_TIMEOUT_MS = 30_000L

    /** Certificate pins per host. Replace with your real backend's pins before shipping. */
    val certificatePins: Map<String, List<String>> =
        mapOf(
            "api.yourapp.com" to
                listOf(
                    // "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // current leaf/intermediate
                    // "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=", // backup pin (required!)
                ),
        )

    /** Whether certificate pinning is enforced, pulled from BuildKonfig. */
    val isCertificatePinningEnabled: Boolean = BuildKonfig.PINNING_ENABLED

    /** Toggle end-to-end payload encryption independently of TLS. */
    var payloadEncryptionEnabled: Boolean = BuildKonfig.ENCRYPTION_ENABLED
}
