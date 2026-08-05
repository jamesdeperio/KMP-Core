package shared.core.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification

class IosAppLifecycleObserver : AppLifecycleObserver {
    private val _state = MutableStateFlow(AppLifecycle.FOREGROUND)
    override val state: StateFlow<AppLifecycle> = _state.asStateFlow()

    init {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) {
            _state.value = AppLifecycle.FOREGROUND
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) {
            _state.value = AppLifecycle.BACKGROUND
        }
    }
}

actual fun createAppLifecycleObserver(): AppLifecycleObserver = IosAppLifecycleObserver()
