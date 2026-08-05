package shared.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAppLifecycleObserver : AppLifecycleObserver, DefaultLifecycleObserver {
    private val _state = MutableStateFlow(AppLifecycle.FOREGROUND)
    override val state: StateFlow<AppLifecycle> = _state.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        _state.value = AppLifecycle.FOREGROUND
    }

    override fun onStop(owner: LifecycleOwner) {
        _state.value = AppLifecycle.BACKGROUND
    }
}

actual fun createAppLifecycleObserver(): AppLifecycleObserver = AndroidAppLifecycleObserver()
