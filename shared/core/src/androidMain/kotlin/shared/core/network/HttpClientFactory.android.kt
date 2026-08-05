package shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import shared.core.security.CertificatePins

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp) {
        block()
        engine {
            config {
                if (NetworkConfig.isCertificatePinningEnabled) {
                    val pinner =
                        CertificatePinner
                            .Builder()
                            .apply {
                                CertificatePins.pinsByHost.forEach { (host, pins) ->
                                    pins.forEach { pin -> add(host, pin) }
                                }
                            }.build()
                    certificatePinner(pinner)
                }
            }
        }
    }

actual fun platformName(): String = "Android"
