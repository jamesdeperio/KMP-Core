package shared.core.security

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Production-ready implementation of [TokenProvider] using platform-native
 * storage. If [cipher] is provided, tokens are encrypted before being saved.
 */
class SecureTokenProvider(
    private val settings: Settings,
    private val cipher: PayloadCipher? = null,
) : TokenProvider {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    override suspend fun getAccessToken(): String? {
        val stored = settings.getStringOrNull(KEY_ACCESS_TOKEN) ?: return null

        val c = cipher
        val token =
            if (c != null) {
                try {
                    c.decryptText(stored)
                } catch (e: SecurityAuthenticationRequiredException) {
                    throw e
                } catch (e: Exception) {
                    return null
                }
            } else {
                stored
            }

        // Proactively check expiry to trigger refresh flow early
        if (JwtUtils.isExpired(token)) {
            return null
        }

        return token
    }

    override suspend fun getRefreshToken(): String? {
        val stored = settings.getStringOrNull(KEY_REFRESH_TOKEN) ?: return null
        val c = cipher
        return if (c != null) {
            try {
                c.decryptText(stored)
            } catch (e: SecurityAuthenticationRequiredException) {
                throw e
            } catch (e: Exception) {
                null
            }
        } else {
            stored
        }
    }

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
    ) {
        val c = cipher
        val finalAccess = if (c != null) c.encryptText(accessToken) else accessToken
        settings[KEY_ACCESS_TOKEN] = finalAccess

        if (refreshToken != null) {
            val finalRefresh = if (c != null) c.encryptText(refreshToken) else refreshToken
            settings[KEY_REFRESH_TOKEN] = finalRefresh
        }
    }

    override suspend fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }
}
