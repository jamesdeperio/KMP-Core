package shared.core.network.plugins

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import shared.core.security.PayloadCipher

/**
 * Wraps the JSON request body as `{ "payload": "<base64 aes-gcm ciphertext>" }`
 * before it's sent, and unwraps/decrypts the same envelope on the response.
 *
 * This is a layer ON TOP of TLS, not a replacement for it — see the doc
 * comment on [PayloadCipher] for why you'd want both. Requires the backend
 * to speak the same envelope format and hold the matching key.
 *
 * IMPORTANT — install order: install this plugin BEFORE `ContentNegotiation`
 * (see HttpClientFactory.kt). Ktor's response pipeline runs transform hooks
 * in installation order, so this must decrypt the envelope into raw JSON
 * bytes first, so ContentNegotiation's own deserializer can then parse the
 * plaintext JSON into your target type.
 *
 * Enable/disable per environment via
 * [shared.core.network.NetworkConfig.payloadEncryptionEnabled], e.g.
 * keep it off against a local/staging backend not wired for it yet.
 */
val PayloadEncryptionKey = AttributeKey<Boolean>("PayloadEncryptionDisabledForRequest")

class PayloadEncryptionConfig {
    var cipher: PayloadCipher? = null
    var enabled: Boolean = true
}

val PayloadEncryptionPlugin =
    createClientPlugin("PayloadEncryptionPlugin", ::PayloadEncryptionConfig) {
        val cipher = pluginConfig.cipher
        val enabled = pluginConfig.enabled

        onRequest { request, content ->
            if (!enabled || cipher == null) return@onRequest
            if (request.attributes.getOrNull(PayloadEncryptionKey) == true) return@onRequest // opted out

            val bodyText = (content as? TextContent)?.text ?: return@onRequest
            val encrypted = cipher.encryptText(bodyText)
            val envelope = JsonObject(mapOf("payload" to JsonPrimitive(encrypted)))

            request.setBody(TextContent(Json.encodeToString(JsonObject.serializer(), envelope), ContentType.Application.Json))
            request.header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            request.header("X-Payload-Encrypted", "1")
        }

        transformResponseBody { response, content, _ ->
            if (!enabled || cipher == null) return@transformResponseBody null
            if (response.headers["X-Payload-Encrypted"] != "1") return@transformResponseBody null

            val rawBytes = content.readRemaining().readByteArray()
            val envelope = Json.decodeFromString(JsonObject.serializer(), rawBytes.decodeToString())
            val encryptedPayload =
                (envelope["payload"] as? JsonPrimitive)?.content
                    ?: return@transformResponseBody null

            // Hand back plaintext JSON bytes as a fresh channel — the next
            // transformer in the pipeline (ContentNegotiation) picks this up
            // and deserializes it into the caller's requested type.
            ByteReadChannel(cipher.decrypt(encryptedPayload))
        }
    }

/** Opt a single request out of payload encryption (e.g. multipart uploads, public endpoints). */
fun HttpRequestBuilder.skipPayloadEncryption() {
    attributes.put(PayloadEncryptionKey, true)
}
