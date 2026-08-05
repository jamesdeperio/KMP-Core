package shared.core.network.plugins

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.encodedPath
import shared.core.security.TokenProvider

/** Result of a refresh call: new access token, and new refresh token if the server rotated it. */
data class RefreshedTokens(
    val accessToken: String,
    val refreshToken: String?,
)

/**
 * Attaches the current access token to every request and transparently
 * refreshes it on a 401 using Ktor's built-in `Auth` / `bearer` provider —
 * this is the standard, well-tested way to do token refresh with Ktor and
 * avoids hand-rolled interceptor races (e.g. multiple 401s firing
 * concurrent refreshes).
 *
 * @param onRefreshFailed called when the refresh token itself is rejected
 *   (e.g. also expired) — hook this up to force a logout / re-login.
 * @param refreshTokens given the current refresh token, calls your auth
 *   endpoint and returns a new token pair, or null if refresh failed.
 */
fun HttpClientConfig<*>.installAuth(
    tokenProvider: TokenProvider,
    refreshTokens: suspend (refreshToken: String) -> RefreshedTokens?,
    onRefreshFailed: suspend () -> Unit = {},
) {
    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenProvider.getAccessToken() ?: return@loadTokens null
                val refresh = tokenProvider.getRefreshToken().orEmpty()
                BearerTokens(accessToken = access, refreshToken = refresh)
            }

            refreshTokens {
                val currentRefreshToken = tokenProvider.getRefreshToken()
                if (currentRefreshToken.isNullOrBlank()) {
                    onRefreshFailed()
                    return@refreshTokens null
                }

                val result = refreshTokens(currentRefreshToken)
                if (result == null) {
                    tokenProvider.clearTokens()
                    onRefreshFailed()
                    return@refreshTokens null
                }

                val (newAccess, newRefresh) = result
                tokenProvider.saveTokens(newAccess, newRefresh)
                BearerTokens(accessToken = newAccess, refreshToken = newRefresh.orEmpty())
            }

            // By default Ktor's bearer provider sends the token on every
            // request that matches this predicate. Exclude auth endpoints
            // themselves to avoid sending a stale/absent token to /login.
            sendWithoutRequest { request: HttpRequestBuilder ->
                !request.url.encodedPath.startsWith("/auth/")
            }
        }
    }
}
