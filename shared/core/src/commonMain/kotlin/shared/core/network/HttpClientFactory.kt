package shared.core.network

import io.github.jamesdeperio.devtool.NetworkDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import shared.core.Config
import shared.core.network.plugins.PayloadEncryptionPlugin
import shared.core.network.plugins.RefreshedTokens
import shared.core.network.plugins.installAuth
import shared.core.security.DataMasker
import shared.core.security.PayloadCipher
import shared.core.security.TokenProvider

/**
 * Hook for platform-specific HTTP client engine configuration.
 *
 * - **Android**: Uses OkHttp engine with TLS and Certificate Pinning support.
 * - **iOS**: Uses Darwin engine.
 *
 * @param block Shared configuration that will be applied to the engine-specific client.
 */
expect fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient

/**
 * Returns a simple platform label (e.g., "android", "ios") to be used in request headers.
 */
expect fun platformName(): String

private val jsonConfig =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

private val prettyJson =
    Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

private fun prettify(message: String): String =
    try {
        val trimmed = message.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val jsonElement = prettyJson.parseToJsonElement(trimmed)
            prettyJson.encodeToString(JsonElement.serializer(), jsonElement)
        } else {
            message
        }
    } catch (_: Exception) {
        message
    }

/**
 * Factory function to build the app's single shared [HttpClient].
 *
 * It pre-configures essential Ktor plugins:
 * 1. **Timeouts**: Connect, Request, and Socket timeouts.
 * 2. **Logging**: Pretty-printed JSON logs in debug mode.
 * 3. **Payload Encryption**: Automatically decrypts server responses (if enabled).
 * 4. **Auth**: Handles Bearer token injection and automatic 401 token refreshing.
 * 5. **Content Negotiation**: Uses Kotlinx Serialization for JSON parsing.
 *
 * @param tokenProvider Provides access and refresh tokens.
 * @param refreshTokens Lambda to execute the refresh token API call.
 * @param payloadCipherKeyProvider Optional provider for the AES encryption key.
 * @param onAuthFailed Lambda called when both access and refresh tokens are invalid.
 */
fun createHttpClient(
    tokenProvider: TokenProvider,
    refreshTokens: suspend (refreshToken: String) -> RefreshedTokens?,
    payloadCipherKeyProvider: (() -> ByteArray)? = null,
    onAuthFailed: suspend () -> Unit = {},
): HttpClient =
    createPlatformHttpClient {
        expectSuccess = true

        install(HttpTimeout) {
            connectTimeoutMillis = NetworkConfig.CONNECT_TIMEOUT_MS
            requestTimeoutMillis = NetworkConfig.REQUEST_TIMEOUT_MS
            socketTimeoutMillis = NetworkConfig.SOCKET_TIMEOUT_MS
        }

        val requestBodyKey = AttributeKey<String>("RequestBodyKey")

        // Installed BEFORE PayloadEncryption so it can capture the PLAINTEXT request body
        // if encryption hasn't happened yet (onRequest hooks run in install order).
        if (Config.isDebugMode) {
            install(
                createClientPlugin("RequestLogger") {
                    onRequest { request, content ->
                        val body = (content as? TextContent)?.text ?: ""
                        request.attributes.put(requestBodyKey, body)
                        if (body.isNotEmpty()) {
                            println("KTOR-LOG: REQUEST BODY:\n${prettify(body)}")
                        }
                    }
                },
            )
        }

        // Installed BEFORE ContentNegotiation on purpose: response-pipeline
        // transform hooks run in install order, so payload decryption must
        // happen first, handing plaintext JSON bytes to ContentNegotiation.
        if (payloadCipherKeyProvider != null) {
            install(PayloadEncryptionPlugin) {
                cipher = PayloadCipher(payloadCipherKeyProvider)
                enabled = NetworkConfig.payloadEncryptionEnabled
            }
        }

        // Installed AFTER PayloadEncryption so it can capture the DECRYPTED response body
        // (transformResponseBody hooks run in install order).
        if (Config.isDebugMode) {
            install(
                createClientPlugin("ResponseLogger") {
                    onResponse { response ->
                        val requestBody =
                            response.call.request.attributes
                                .getOrNull(requestBodyKey) ?: ""

                        NetworkDebug.add(
                            io.github.jamesdeperio.devtool.ApiLogData(
                                path = response.call.request.url.encodedPath,
                                method = response.call.request.method.value,
                                statusCode = response.status.value,
                                request = DataMasker.maskJson(prettify(requestBody)),
                                response = "See Logcat (filter: KTOR-LOG) for decrypted body",
                                headers =
                                    response.headers.entries().joinToString("\n") {
                                        "${it.key}: ${it.value.joinToString(", ")}"
                                    },
                            ),
                        )
                    }
                },
            )
        }

        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(Logging) {
            // Use LogLevel.ALL to see full Request/Response details (including bodies) in Logcat/Console.
            level = if (Config.isDebugMode) LogLevel.ALL else LogLevel.NONE
            logger =
                object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("KTOR-LOG: ${prettify(message)}")
                    }
                }
        }

        installAuth(
            tokenProvider = tokenProvider,
            refreshTokens = refreshTokens,
            onRefreshFailed = onAuthFailed,
        )

        defaultRequest {
            url {
                val urlString = NetworkConfig.baseUrl
                val isHttp = urlString.startsWith("http://")
                protocol = if (isHttp) URLProtocol.HTTP else URLProtocol.HTTPS

                val hostPort =
                    urlString
                        .removePrefix("https://")
                        .removePrefix("http://")
                        .substringBefore("/")

                if (hostPort.contains(":")) {
                    host = hostPort.substringBefore(":")
                    port = hostPort.substringAfter(":").toInt()
                } else {
                    host = hostPort
                }
            }
            contentType(ContentType.Application.Json)
            header("X-Client-Platform", platformName())
        }
    }
