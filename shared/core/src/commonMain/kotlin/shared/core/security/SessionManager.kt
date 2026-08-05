package shared.core.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import shared.core.lifecycle.AppLifecycle
import shared.core.lifecycle.AppLifecycleObserver
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Manages app-level session timing and auto-lock security.
 *
 * It monitors user activity and automatically locks the session after a period of inactivity.
 * It also listens to app lifecycle events to handle background/foreground transitions.
 *
 * @property lifecycleObserver Optional observer for app lifecycle events.
 * @property timeout The duration of inactivity allowed before auto-locking. Default is 5 minutes.
 * @property scope The [CoroutineScope] in which the inactivity timer runs.
 * @property currentTimeProvider Provides the current time in milliseconds.
 */
class SessionManager(
    private val lifecycleObserver: AppLifecycleObserver? = null,
    private val timeout: Duration = 5.minutes,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val currentTimeProvider: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() },
) {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastActivityTime: Long = currentTimeProvider()
    private var timerJob: Job? = null

    init {
        startTimer()
        observeLifecycle()
    }

    private fun observeLifecycle() {
        lifecycleObserver?.state?.onEach { lifecycle ->
            if (lifecycle == AppLifecycle.BACKGROUND) {
                // Instantly lock if backgrounded for more than 1 min,
                // or just update activity time to trigger normal timeout.
                // For high security, we can lock instantly:
                // lock()
            }
        }?.launchIn(scope)
    }

    /**
     * Resets the inactivity timer. This should be called on every user interaction
     * (e.g., button clicks, scrolls, text input) to prevent premature locking.
     */
    fun reportActivity() {
        lastActivityTime = currentTimeProvider()
        if (_isLocked.value) {
            // If we want to auto-unlock on activity (not recommended for security), 
            // we'd do it here. But usually "Lock" requires re-auth.
        }
    }

    /**
     * Explicitly locks the session. This is typically called when the user logs out
     * or when a high-security threshold is met.
     */
    fun lock() {
        _isLocked.value = true
        stopTimer()
    }

    /**
     * Unlocks the session and restarts the inactivity timer. This is called after
     * a successful authentication (login or biometric re-auth).
     */
    fun unlock() {
        _isLocked.value = false
        lastActivityTime = currentTimeProvider()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(10.seconds) // Check every 10 seconds
                val elapsed = currentTimeProvider() - lastActivityTime
                if (elapsed >= timeout.inWholeMilliseconds) {
                    _isLocked.value = true
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
