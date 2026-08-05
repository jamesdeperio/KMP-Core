package shared.core.di

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import shared.core.analytics.AnalyticsTracker
import shared.core.analytics.LoggerAnalyticsTracker
import shared.core.lifecycle.AppLifecycleObserver
import shared.core.lifecycle.createAppLifecycleObserver
import shared.core.network.ConnectivityMonitor
import shared.core.network.NetworkRefreshHandler
import shared.core.network.createConnectivityMonitor
import shared.core.network.createHttpClient
import shared.core.permission.PermissionManager
import shared.core.permission.createPermissionManager
import shared.core.security.BiometricAuthenticator
import shared.core.security.RASPConfig
import shared.core.security.SecurityPolicyManager
import shared.core.security.SessionManager

/**
 * The platform-specific parts of the core module (Storage/Secure Settings).
 */
expect val platformModule: Module

/**
 * The unified core DI module providing shared infrastructure across all features.
 *
 * This module should be included in the main Koin start-up configuration.
 * It provides singletons for:
 * - **Security**: [BiometricAuthenticator], [SessionManager], [SecurityPolicyManager].
 * - **Analytics**: [AnalyticsTracker] (defaults to [LoggerAnalyticsTracker]).
 * - **Infrastructure**: [HttpClient], [ConnectivityMonitor], [AppLifecycleObserver].
 * - **Permissions**: [PermissionManager].
 */
val coreModule: Module =
    module {
        includes(platformModule)

        // Security Components
        single { BiometricAuthenticator() }
        single { SessionManager(get()) }
        single { SecurityPolicyManager(get(), getOrNull() ?: RASPConfig.Strict) }
        
        // Analytics
        single<AnalyticsTracker> { LoggerAnalyticsTracker() }

        // Lifecycle
        single { createAppLifecycleObserver() }

        // Connectivity
        single { createConnectivityMonitor() }

        // Permissions
        single { createPermissionManager() }

        // Provide the HttpClient.
        // Requires a NetworkRefreshHandler to be registered in your app module.
        single<HttpClient> {
            val refreshHandler = getOrNull<NetworkRefreshHandler>()

            createHttpClient(
                tokenProvider = get(),
                refreshTokens = { token -> refreshHandler?.refresh(token) },
                onAuthFailed = { refreshHandler?.onAuthFailed() },
            )
        }
    }
