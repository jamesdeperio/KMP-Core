package shared.core.security

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtUtilsTest {

    private val validJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjQ2ODU5ODk3MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    private val expiredJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    @Test
    fun decodePayload_withValidJwt_returnsJsonObject() {
        val payload = JwtUtils.decodePayload(validJwt)
        assertNotNull(payload)
        assertEquals("1234567890", payload["sub"]?.jsonPrimitive?.content)
        assertEquals(4685989700L, payload["exp"]?.jsonPrimitive?.longOrNull)
    }

    @Test
    fun decodePayload_withInvalidJwt_returnsNull() {
        assertNull(JwtUtils.decodePayload("invalid.jwt"))
        assertNull(JwtUtils.decodePayload("not.a.jwt.at.all"))
    }

    @Test
    fun isExpired_withValidJwt_returnsFalse() {
        assertFalse(JwtUtils.isExpired(validJwt))
    }

    @Test
    fun isExpired_withExpiredJwt_returnsTrue() {
        assertTrue(JwtUtils.isExpired(expiredJwt))
    }

    @Test
    fun isExpired_withMissingExpClaim_returnsTrue() {
        val jwtNoExp = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        assertTrue(JwtUtils.isExpired(jwtNoExp))
    }

    @Test
    fun expiresInSeconds_withValidJwt_returnsPositiveValue() {
        val remaining = JwtUtils.expiresInSeconds(validJwt)
        assertNotNull(remaining)
        assertTrue(remaining > 0)
    }

    @Test
    fun expiresInSeconds_withExpiredJwt_returnsNegativeValue() {
        val remaining = JwtUtils.expiresInSeconds(expiredJwt)
        assertNotNull(remaining)
        assertTrue(remaining < 0)
    }
}
