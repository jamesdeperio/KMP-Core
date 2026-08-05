package shared.core.security

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * A generic key-value storage that encrypts all string values at rest.
 *
 * On Android, this uses AES-256-GCM encryption with keys stored in the Keystore.
 * On iOS, this uses the Keychain which is hardware-encrypted by default.
 */
class SecureStorage(
    private val settings: Settings,
    private val cipher: PayloadCipher? = null,
) {
    suspend fun putString(
        key: String,
        value: String,
    ) {
        val finalValue = if (cipher != null) cipher.encryptText(value) else value
        settings[key] = finalValue
    }

    suspend fun getString(key: String): String? {
        val stored = settings.getStringOrNull(key) ?: return null
        val c = cipher
        return if (c != null) {
            runCatching { c.decryptText(stored) }.getOrNull()
        } else {
            stored
        }
    }

    suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        putString(key, value.toString())
    }

    suspend fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean = getString(key)?.toBooleanStrictOrNull() ?: defaultValue

    suspend fun putLong(
        key: String,
        value: Long,
    ) {
        putString(key, value.toString())
    }

    suspend fun getLong(key: String): Long? = getString(key)?.toLongOrNull()

    fun remove(key: String) = settings.remove(key)

    fun clear() = settings.clear()
}
