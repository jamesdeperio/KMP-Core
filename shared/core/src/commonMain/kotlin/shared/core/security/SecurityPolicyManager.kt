package shared.core.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Monitors the security state of the application and enforces policies.
 * Policies include clearing tokens and notifying the UI when the device is compromised.
 */
class SecurityPolicyManager(
    private val tokenProvider: TokenProvider,
    private val config: RASPConfig = RASPConfig.Strict,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _isCompromised = MutableStateFlow(false)
    val isCompromised: StateFlow<Boolean> = _isCompromised.asStateFlow()

    private val _securityEvents = MutableSharedFlow<SecurityEvent>()
    val securityEvents: SharedFlow<SecurityEvent> = _securityEvents.asSharedFlow()

    init {
        // Observe RASP checks periodically
        appCompromiseFlow(config = config, intervalMs = 10000L)
            .onEach { compromised ->
                _isCompromised.value = compromised
                if (compromised) {
                    handleCompromisedDevice()
                }
            }
            .launchIn(scope)
    }

    private suspend fun handleCompromisedDevice() {
        // 1. Wipe sensitive data immediately
        tokenProvider.clearTokens()
        
        // 2. Notify the UI to show a "Device Compromised" screen or block the user
        _securityEvents.emit(SecurityEvent.DeviceCompromised)
    }

    sealed class SecurityEvent {
        object DeviceCompromised : SecurityEvent()
    }
}
