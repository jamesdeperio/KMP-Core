package shared.core.security

import android.content.Context
import androidx.core.content.edit
import shared.core.Config
import java.security.SecureRandom

/**
 * Manages a 32-byte AES key that is stored encrypted by the Android Keystore.
 */
class AndroidSecureKeyManager(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(Config.SECRETS_PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENCRYPTED_SEED = "encrypted_seed"
    }

    fun getOrCreatePayloadKey(): ByteArray {
        val encryptedSeed = prefs.getString(KEY_ENCRYPTED_SEED, null)
        return if (encryptedSeed != null) {
            AndroidKeyStoreCipher.decrypt(encryptedSeed)
        } else {
            val newKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val newEncrypted = AndroidKeyStoreCipher.encrypt(newKey)
            prefs.edit { putString(KEY_ENCRYPTED_SEED, newEncrypted) }
            newKey
        }
    }
}
