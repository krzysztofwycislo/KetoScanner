package pl.handsome.club.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Dispatchers for `domain` and `data` code. Inject this instead of referencing [Dispatchers] directly,
 * so tests can substitute a test dispatcher (architecture section 4).
 * There is no `main`: ViewModels use `viewModelScope`.
 */
interface AppDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

object DefaultAppDispatchers : AppDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
