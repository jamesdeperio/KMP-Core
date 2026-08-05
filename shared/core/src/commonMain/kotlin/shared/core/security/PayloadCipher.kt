package shared.core.security

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * End-to-end payload encryption, independent of TLS.
 *
 * TLS protects the transport (attacker can't read traffic on the wire),
 * but it terminates at load balancers / reverse proxies / CDNs, and doesn't
 * protect against a compromised intermediary or logging layer inside your
 * own infra. This layer encrypts the actual JSON payload with AES-256-GCM,
 * so only the client and the service holding the matching key can read it.
 *
 * Key provisioning is intentionally left to you — this class only performs
 * the crypto operation. Common approaches:
 *  - Pre-shared key baked in via secure config/secret manager (simplest,
 *    same key across all clients — fine for internal/enterprise apps)
 *  - Per-session key negotiated via ECDH at login, then used for the
 *    session's lifetime (stronger — see the KeyExchange note below)
 *
 * Wire format: base64(iv || ciphertext || tag), matching what
 * cryptography-kotlin's AES-GCM cipher produces by default.
 */
@OptIn(ExperimentalEncodingApi::class)
class PayloadCipher(
    private val keyProvider: () -> ByteArray,
) {
    private val aesGcm = CryptographyProvider.Default.get(AES.GCM)

    suspend fun encrypt(plaintext: ByteArray): String {
        val keyBytes = keyProvider()
        val key = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
        val cipher = key.cipher()
        val ciphertextWithIv = cipher.encrypt(plaintext)
        return Base64.encode(ciphertextWithIv)
    }

    suspend fun decrypt(encoded: String): ByteArray {
        val keyBytes = keyProvider()
        val key = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
        val cipher = key.cipher()
        val combined = Base64.decode(encoded)
        return cipher.decrypt(combined)
    }

    suspend fun encryptText(plaintext: String): String = encrypt(plaintext.encodeToByteArray())

    suspend fun decryptText(encoded: String): String = decrypt(encoded).decodeToString()

    companion object {
        /** Generates a fresh random 256-bit key — use during key provisioning/rotation, not per-request. */
        suspend fun generateKey(): ByteArray {
            val provider = CryptographyProvider.Default
            val aesGcm = provider.get(AES.GCM)
            val key = aesGcm.keyGenerator(AES.Key.Size.B256).generateKey()
            return key.encodeToByteArray(AES.Key.Format.RAW)
        }
    }
}
