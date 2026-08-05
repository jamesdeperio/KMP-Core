package shared.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringExtensionsTest {

    @Test
    fun isValidEmail_validatesCorrectly() {
        assertTrue("test@example.com".isValidEmail())
        assertTrue("user.name+tag@sub.domain.org".isValidEmail())
        assertFalse("plainaddress".isValidEmail())
        assertFalse("@missinguser.com".isValidEmail())
        assertFalse("user@missingdomain".isValidEmail())
    }

    @Test
    fun digitsOnly_extractsDigits() {
        assertEquals("123456", "12-34 56".digitsOnly())
        assertEquals("639123456789", "+63 912-345-6789".digitsOnly())
        assertEquals("", "No digits here".digitsOnly())
    }

    @Test
    fun maskEmail_masksCorrectly() {
        assertEquals("jo***@gmail.com", "john@gmail.com".maskEmail())
        assertEquals("te***@example.com", "test@example.com".maskEmail())
        assertEquals("hi@me.com", "hi@me.com".maskEmail()) // Too short to mask range [2, 2)
    }

    @Test
    fun capitalizeWords_capitalizesCorrectly() {
        assertEquals("Hello World", "hello world".capitalizeWords())
        assertEquals("The Quick Brown Fox", "THE QUICK BROWN FOX".capitalizeWords())
        assertEquals("Kmp Development", "kmp development".capitalizeWords())
    }
}
