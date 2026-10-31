package shared.core.security

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PayloadCipherTest {

    @Test
    fun encryptAndDecrypt_returnsOriginalText() = runTest {
        val key = PayloadCipher.generateKey()
        val cipher = PayloadCipher { key }
        
        val originalText = "Hello Trivana! This is a secret message."
        val encrypted = cipher.encryptText(originalText)
        
        assertNotEquals(originalText, encrypted)
        
        val decrypted = cipher.decryptText(encrypted)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun encrypt_producesDifferentResultsEachTimeDueToIv() = runTest {
        val key = PayloadCipher.generateKey()
        val cipher = PayloadCipher { key }
        
        val originalText = "Constant message"
        val encrypted1 = cipher.encryptText(originalText)
        val encrypted2 = cipher.encryptText(originalText)
        
        // AES-GCM uses a random IV for each encryption, so output should differ
        assertNotEquals(encrypted1, encrypted2)
        
        // But both should decrypt back to same message
        assertEquals(originalText, cipher.decryptText(encrypted1))
        assertEquals(originalText, cipher.decryptText(encrypted2))
    }
}
