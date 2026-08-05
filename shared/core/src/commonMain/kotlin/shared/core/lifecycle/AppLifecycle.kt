package shared.core.lifecycle

import kotlinx.coroutines.flow.StateFlow

enum class AppLifecycle {
    FOREGROUND,
    BACKGROUND
}

/**
 * Monitors the application's lifecycle (Foreground/Background) across platforms.
 */
interface AppLifecycleObserver {
    val state: StateFlow<AppLifecycle>
}

/**
 * Platform-specific factory hook.
 */
expect fun createAppLifecycleObserver(): AppLifecycleObserver
