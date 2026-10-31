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
 * Uses the Android Keystore to encrypt/decrypt data using hardware-backed keys.
 */
object AndroidKeyStoreCipher {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"

    private fun getOrGenerateKey(alias: String, requireAuth: Boolean): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(requireAuth)
                    .apply {
                        if (requireAuth) {
                            setUserAuthenticationValidityDurationSeconds(300)
                        }
                    }
                    .build()
            )
            keyGenerator.generateKey()
        }
        
        return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(data: ByteArray, alias: String = Config.KEY_ALIAS, requireAuth: Boolean = true): String {
        try {
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, getOrGenerateKey(alias, requireAuth))
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: UserNotAuthenticatedException) {
            throw SecurityAuthenticationRequiredException("Biometric authentication required to access key")
        }
    }

    fun decrypt(encryptedBase64: String, alias: String = Config.KEY_ALIAS, requireAuth: Boolean = true): ByteArray {
        try {
            val combined = Base64.getDecoder().decode(encryptedBase64)
            val iv = ByteArray(12)
            val encrypted = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, encrypted, 0, encrypted.size)

            val cipher = Cipher.getInstance(AES_MODE)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrGenerateKey(alias, requireAuth), spec)
            return cipher.doFinal(encrypted)
        } catch (e: UserNotAuthenticatedException) {
            throw SecurityAuthenticationRequiredException("Biometric authentication required to access key")
        }
    }
}
