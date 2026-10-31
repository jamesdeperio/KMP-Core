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
     * Current App Version Code.
     */
    const val VERSION_CODE = 1 // This should ideally come from BuildKonfig or platform actual

    /**
     * Name/Service ID for the primary secure storage.
     */
    const val SECURE_STORAGE_NAME = "trivana_shared.core.secure_storage"

    /**
     * Key alias for the Android KeyStore (requires Biometrics).
     */
    const val KEY_ALIAS = "trivana_shared_core_master_key"

    /**
     * Key alias for data secured at rest but not requiring biometrics.
     */
    const val AT_REST_KEY_ALIAS = "trivana_shared_core_at_rest_key"

    /**
     * Name for the Android general secure preferences file.
     */
    const val SECURE_PREFS_NAME = "trivana_shared_core_secure_prefs"

    /**
     * Name for the Android internal secrets preferences file.
     */
    const val SECRETS_PREFS_NAME = "trivana_shared_core_secrets"

    /**
     * Name for the Android token preferences file.
     */
    const val TOKENS_PREFS_NAME = "trivana_shared_core_tokens"
}
