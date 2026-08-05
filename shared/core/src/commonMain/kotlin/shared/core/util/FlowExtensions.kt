package shared.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Updates a [MutableStateFlow] by applying the given [block] to its current value.
 * This is a shorthand for `stateFlow.update { it.copy(...) }` in MVI/MVVM architectures.
 */
fun <T> MutableStateFlow<T>.updateState(block: T.() -> T) {
    this.update { it.block() }
}
