package shared.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import shared.core.Config
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Uses the Android Keystore to encrypt/decrypt a "Master Seed" key.
 * The Master Seed is then used by common code (PayloadCipher) to encrypt settings.
 */
object AndroidKeyStoreCipher {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"

    init {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(Config.KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec
                    .Builder(Config.KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(300) // 5 minutes
                    .build(),
            )
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return (keyStore.getEntry(Config.KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(data: ByteArray): String {
        try {
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            // Format: IV(12 bytes) + EncryptedData
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: UserNotAuthenticatedException) {
            throw SecurityAuthenticationRequiredException("Biometric authentication required to access key")
        }
    }

    fun decrypt(encryptedBase64: String): ByteArray {
        try {
            val combined = Base64.getDecoder().decode(encryptedBase64)
            val iv = ByteArray(12)
            val encrypted = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, encrypted, 0, encrypted.size)

            val cipher = Cipher.getInstance(AES_MODE)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            return cipher.doFinal(encrypted)
        } catch (e: UserNotAuthenticatedException) {
            throw SecurityAuthenticationRequiredException("Biometric authentication required to access key")
        }
    }
}
