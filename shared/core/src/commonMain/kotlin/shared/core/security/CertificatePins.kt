package shared.core.security

import shared.core.network.NetworkConfig

/**
 * Certificate pinning is configured on the platform HTTP engine (OkHttp on
 * Android/Desktop, NSURLSession delegate on iOS) because Ktor has no
 * cross-platform pinning API — see the `HttpClientEngine.kt` `actual`
 * implementation in each source set.
 *
 * This object just centralizes the pin data so it isn't duplicated per
 * platform, and documents the operational requirement around it.
 *
 * IMPORTANT: always include at least one BACKUP pin (e.g. an intermediate
 * or a not-yet-deployed leaf cert). Pinning only your current leaf cert
 * means every cert rotation without an app update can hard-lock users out
 * of your API. Rotate pins well ahead of any planned certificate renewal.
 *
 * Pin format: `sha256/<base64 SPKI hash>` — generate with:
 *   openssl x509 -in cert.pem -pubkey -noout |
 *     openssl pkey -pubin -outform der |
 *     openssl dgst -sha256 -binary | openssl enc -base64
 */
object CertificatePins {
    val pinsByHost: Map<String, List<String>>
        get() = NetworkConfig.certificatePins
}
