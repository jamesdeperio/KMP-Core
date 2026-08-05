package shared.core.security

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Client-side JWT inspection only — decoding a JWT payload here does NOT
 * verify its signature. Signature verification must always happen on the
 * server; a client can't hold the verification secret without defeating
 * the purpose of signing in the first place. This is purely so the app can
 * proactively refresh a token before it expires, without waiting for a 401.
 */
object JwtUtils {
    @OptIn(ExperimentalEncodingApi::class)
    fun decodePayload(jwt: String): JsonObject? {
        val parts = jwt.split(".")
        if (parts.size != 3) return null
        return try {
            val payloadBytes = Base64.UrlSafe.decode(parts[1].padBase64())
            Json.parseToJsonElement(payloadBytes.decodeToString()) as? JsonObject
        } catch (e: Exception) {
            null
        }
    }

    /** True if the token's `exp` claim is in the past (or missing/unparseable → treated as expired). */
    fun isExpired(
        jwt: String,
        clockSkewToleranceSeconds: Long = 60,
    ): Boolean {
        val exp = decodePayload(jwt)?.get("exp")?.jsonPrimitive?.longOrNull ?: return true
        val nowSeconds =
            kotlin.time.Clock.System
                .now()
                .epochSeconds
        return nowSeconds >= (exp - clockSkewToleranceSeconds)
    }

    /** Seconds remaining until expiry; negative/zero if already expired. */
    fun expiresInSeconds(jwt: String): Long? {
        val exp = decodePayload(jwt)?.get("exp")?.jsonPrimitive?.longOrNull ?: return null
        return exp -
            kotlin.time.Clock.System
                .now()
                .epochSeconds
    }

    private fun String.padBase64(): String {
        val remainder = length % 4
        return if (remainder == 0) this else this + "=".repeat(4 - remainder)
    }
}
