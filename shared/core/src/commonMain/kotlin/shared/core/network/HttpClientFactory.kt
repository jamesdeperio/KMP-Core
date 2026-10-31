package shared.core.network

import io.github.jamesdeperio.devtool.NetworkDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.content.ByteArrayContent
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
import shared.core.security.PayloadCipher
import shared.core.security.TokenProvider

/**
 * Hook for platform-specific HTTP client engine configuration.
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
 */
fun createHttpClient(
    tokenProvider: TokenProvider,
    refreshTokens: suspend (refreshToken: String) -> RefreshedTokens?,
    networkConfig: KmpNetworkConfig =
        KmpNetworkConfig(
            baseUrl = NetworkConfig.baseUrl,
            isDebugMode = Config.isDebugMode,
            payloadEncryptionEnabled = NetworkConfig.payloadEncryptionEnabled,
        ),
    payloadCipherKeyProvider: (() -> ByteArray)? = null,
    onAuthFailed: suspend () -> Unit = {},
): HttpClient =
    createPlatformHttpClient {
        expectSuccess = true

        configureTimeouts(networkConfig)
        configurePayloadEncryption(payloadCipherKeyProvider, networkConfig.payloadEncryptionEnabled)
        configureNetworkLogging(networkConfig.isDebugMode)
        configureSerialization()

        installAuth(
            tokenProvider = tokenProvider,
            refreshTokens = refreshTokens,
            onRefreshFailed = onAuthFailed,
        )

        configureDefaultHeadersAndUrl(networkConfig.baseUrl)
    }

private fun HttpClientConfig<*>.configureTimeouts(config: KmpNetworkConfig) {
    install(HttpTimeout) {
        connectTimeoutMillis = config.connectTimeoutMs
        requestTimeoutMillis = config.requestTimeoutMs
        socketTimeoutMillis = config.socketTimeoutMs
    }
}

private fun HttpClientConfig<*>.configurePayloadEncryption(
    payloadCipherKeyProvider: (() -> ByteArray)?,
    enabled: Boolean,
) {
    if (payloadCipherKeyProvider != null) {
        install(PayloadEncryptionPlugin) {
            cipher = PayloadCipher(payloadCipherKeyProvider)
            this.enabled = enabled
        }
    }
}

private fun HttpClientConfig<*>.configureNetworkLogging(isDebugMode: Boolean) {
    val requestBodyKey = AttributeKey<String>("RequestBodyKey")

    if (isDebugMode) {
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

        install(
            createClientPlugin("ResponseLogger") {
                onResponse { response ->
                    val content = response.call.request.content
                    val bodyText = when (content) {
                        is TextContent -> content.text
                        is ByteArrayContent -> content.bytes().decodeToString()
                        else -> content.toString()
                    }
                    NetworkDebug.add(
                        io.github.jamesdeperio.devtool.ApiLogData(
                            path = response.call.request.url.encodedPath,
                            method = response.call.request.method.value,
                            statusCode = response.status.value,
                            request = prettify(bodyText),
                            response = prettify(response.body()),
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

    install(Logging) {
        level = if (isDebugMode) LogLevel.ALL else LogLevel.NONE
        logger =
            object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    println("KTOR-LOG: ${prettify(message)}")
                }
            }
    }
}

private fun HttpClientConfig<*>.configureSerialization() {
    install(ContentNegotiation) {
        json(jsonConfig)
    }
}

private fun HttpClientConfig<*>.configureDefaultHeadersAndUrl(baseUrl: String) {
    defaultRequest {
        url {
            val isHttp = baseUrl.startsWith("http://")
            protocol = if (isHttp) URLProtocol.HTTP else URLProtocol.HTTPS

            val hostPort =
                baseUrl
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
