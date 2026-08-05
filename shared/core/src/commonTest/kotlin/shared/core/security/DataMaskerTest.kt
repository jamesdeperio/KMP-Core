package shared.core.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataMaskerTest {

    @Test
    fun testMaskEmail() {
        assertEquals("a*@b.com", DataMasker.maskEmail("ab@b.com"))
        assertEquals("j*n@domain.com", DataMasker.maskEmail("jan@domain.com"))
        assertEquals("", DataMasker.maskEmail(null))
    }

    @Test
    fun testMaskPhone() {
        assertEquals("+63********89", DataMasker.maskPhone("+639123456789"))
        assertEquals("12***", DataMasker.maskPhone("12345"))
    }

    @Test
    fun testMaskGeneral() {
        assertEquals("se****", DataMasker.maskGeneral("secret"))
        assertEquals("****", DataMasker.maskGeneral("abcd", 0))
    }

    @Test
    fun testMaskJson() {
        val input = """
            {
                "user": "kaizen",
                "password": "secret_password",
                "nested": {
                    "token": "abc-123",
                    "id": 101
                },
                "list": [
                    {"secret": "hidden"},
                    {"public": "visible"}
                ]
            }
        """.trimIndent()
        
        val masked = DataMasker.maskJson(input)
        
        // Assert sensitive keys are masked
        assertTrue(masked.contains("\"password\":\"***\""))
        assertTrue(masked.contains("\"token\":\"***\""))
        assertTrue(masked.contains("\"secret\":\"***\""))
        
        // Assert public keys are NOT masked
        assertTrue(masked.contains("\"user\":\"kaizen\""))
        assertTrue(masked.contains("\"id\":101"))
        assertTrue(masked.contains("\"public\":\"visible\""))
    }
}
