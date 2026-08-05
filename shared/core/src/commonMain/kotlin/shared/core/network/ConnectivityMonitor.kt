package shared.core.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors the network connectivity status across platforms.
 */
interface ConnectivityMonitor {
    /**
     * A flow that emits true when the device has an active internet connection, false otherwise.
     */
    val isConnected: StateFlow<Boolean>
}

/**
 * Hook for platform-specific implementation.
 */
expect fun createConnectivityMonitor(): ConnectivityMonitor
