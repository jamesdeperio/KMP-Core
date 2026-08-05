package shared.core

/**
 * Global application configuration.
 * Pulls values from BuildKonfig which are populated via environment properties.
 */
object Config {
    /**
     * True if the app is running in debug/non-production mode.
     * Used to enable logging, debug tools, and bypass certain security checks.
     */
    val isDebugMode: Boolean = BuildKonfig.IS_DEBUG

    /**
     * Name/Service ID for the primary secure storage.
     */
    const val SECURE_STORAGE_NAME = "shared.core.secure_storage"

    /**
     * Key alias for the Android KeyStore.
     */
    const val KEY_ALIAS = "shared_core_master_key"

    /**
     * Name for the Android general secure preferences file.
     */
    const val SECURE_PREFS_NAME = "shared_core_secure_prefs"

    /**
     * Name for the Android internal secrets preferences file.
     */
    const val SECRETS_PREFS_NAME = "shared_core_secrets"

    /**
     * Name for the Android token preferences file.
     */
    const val TOKENS_PREFS_NAME = "shared_core_tokens"
}
