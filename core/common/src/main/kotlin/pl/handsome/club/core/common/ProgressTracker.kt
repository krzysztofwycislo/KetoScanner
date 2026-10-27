package pl.handsome.club.core.common

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ProgressTracker {

    private val mutex = Mutex()
    private var running = 0
    private val progress = MutableStateFlow(false)

    val inProgress: StateFlow<Boolean> = progress.asStateFlow()

    suspend fun <T> reportWork(block: suspend () -> T): T {
        mutex.withLock {
            running++
            progress.value = running > 0
        }
        try {
            return block()
        } finally {
            withContext(NonCancellable) {
                mutex.withLock {
                    running--
                    progress.value = running > 0
                }
            }
        }
    }
}
