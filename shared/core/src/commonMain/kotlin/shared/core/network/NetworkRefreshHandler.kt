package shared.core.network

import shared.core.network.plugins.RefreshedTokens

/**
 * Interface to bridge the gap between the Core Network module and the
 * App's business logic for token refreshing.
 */
interface NetworkRefreshHandler {
    suspend fun refresh(refreshToken: String): RefreshedTokens?

    suspend fun onAuthFailed()
}
