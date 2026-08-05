package shared.core.security

/**
 * Interface defining the contract for managing authentication tokens.
 *
 * Implementations should handle secure persistence (e.g., Android Keystore, iOS Keychain).
 * This abstraction allows the networking layer to retrieve and refresh tokens without
 * knowing the specific storage implementation.
 */
interface TokenProvider {
    /** Retrieves the current access token, or null if it's missing or expired. */
    suspend fun getAccessToken(): String?

    /** Retrieves the refresh token used to obtain a new access token. */
    suspend fun getRefreshToken(): String?

    /**
     * Saves the provided tokens to secure storage.
     * @param accessToken The new access token.
     * @param refreshToken The new refresh token (optional).
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
    )

    /** Deletes all stored tokens (e.g., on logout). */
    suspend fun clearTokens()
}

/**
 * An in-memory implementation of [TokenProvider].
 *
 * @warning Use ONLY for tests or previews. Do not use this in production as tokens
 * will be lost when the app process is killed.
 */
class InMemoryTokenProvider : TokenProvider {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? {
        val token = accessToken ?: return null

        // INTEGRATION: Proactively check if the token is expired.
        // If it is, we return null to signal the AuthPlugin to refresh.
        if (JwtUtils.isExpired(token)) {
            return null
        }

        return token
    }

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken ?: this.refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
