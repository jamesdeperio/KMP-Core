package shared.core.security

import kotlinx.coroutines.flow.Flow

/**
 * Configuration for Runtime Application Self-Protection (RASP).
 * Allows the app to selectively enable or disable specific security checks.
 */
data class RASPConfig(
    val checkEmulator: Boolean = true,
    val checkRooted: Boolean = true,
    val checkDebugger: Boolean = true,
    val checkDeveloperOptions: Boolean = true,
    val checkProxy: Boolean = true,
    val checkOverlay: Boolean = true,
) {
    companion object {
        /** All security checks are enabled. Recommended for Production. */
        val Strict = RASPConfig()

        /** All security checks are disabled. Useful for local development/testing. */
        val None = RASPConfig(
            checkEmulator = false,
            checkRooted = false,
            checkDebugger = false,
            checkDeveloperOptions = false,
            checkProxy = false,
            checkOverlay = false
        )
        
        /** Only critical OS integrity checks are enabled. */
        val Basic = RASPConfig(
            checkEmulator = false,
            checkRooted = true,
            checkDebugger = false,
            checkDeveloperOptions = false,
            checkProxy = false,
            checkOverlay = false
        )
    }
}

/**
 * Checks if the application environment is compromised based on the provided [config].
 */
suspend fun isAppCompromised(config: RASPConfig = RASPConfig.Strict): Boolean {
    if (config.checkEmulator && checkEmulator()) return true
    if (config.checkRooted && checkRootedOrJailbroken()) return true
    if (config.checkDebugger && checkDebuggerAttached()) return true
    if (config.checkDeveloperOptions && checkDeveloperOptionsEnabled()) return true
    if (config.checkProxy && checkProxyOrVpn()) return true
    if (config.checkOverlay && checkOverlayThreat()) return true
    return false
}

// Low-level checks
expect suspend fun checkEmulator(): Boolean

expect suspend fun checkRootedOrJailbroken(): Boolean

expect suspend fun checkDebuggerAttached(): Boolean

expect suspend fun checkDeveloperOptionsEnabled(): Boolean

expect suspend fun checkProxyOrVpn(): Boolean

expect suspend fun checkOverlayThreat(): Boolean

/**
 * Returns a Flow that periodically emits true if the environment becomes compromised
 * based on the provided [config].
 */
expect fun appCompromiseFlow(
    config: RASPConfig = RASPConfig.Strict,
    intervalMs: Long = 5000L
): Flow<Boolean>

/**
 * Enables or disables screenshot and screen recording protection.
 */
expect fun shouldPreventScreenSniffing(enable: Boolean)
