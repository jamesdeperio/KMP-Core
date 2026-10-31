package shared.core.security

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import shared.core.Config

/**
 * Creates a SecureTokenProvider using standard SharedPreferences +
 * manual AES-GCM encryption (backed by Android Keystore).
 * This avoids the deprecated EncryptedSharedPreferences library.
 */
fun createSecureTokenProvider(context: Context): SecureTokenProvider {
    // 1. Standard (unencrypted) storage
    val prefs = context.getSharedPreferences(Config.TOKENS_PREFS_NAME, Context.MODE_PRIVATE)
    val settings = SharedPreferencesSettings(prefs)

    // 2. Get the unique hardware-backed encryption key for this device
    val keyManager = AndroidSecureKeyManager(context)
    val cipher = PayloadCipher { keyManager.getOrCreatePayloadKey() }

    // 3. Return the provider that handles encryption transparently
    return SecureTokenProvider(settings, cipher)
}

/**
 * Creates a generic SecureStorage instance.
 * @param name The name of the SharedPreferences file.
 */
fun createSecureStorage(
    context: Context,
    name: String,
): SecureStorage {
    val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    val settings = SharedPreferencesSettings(prefs)

    val keyManager = AndroidSecureKeyManager(context)
    val cipher = PayloadCipher { keyManager.getOrCreatePayloadKey() }

    return SecureStorage(settings, cipher)
}

/**
 * Creates a PayloadCipher backed by a hardware key that does NOT require biometrics.
 * Useful for securing local data (like Room/Drafts) at rest.
 */
fun createAtRestCipher(context: Context): PayloadCipher {
    val keyManager = AndroidSecureKeyManager(context)
    return PayloadCipher { 
        keyManager.getOrCreateAtRestKey()
    }
}
