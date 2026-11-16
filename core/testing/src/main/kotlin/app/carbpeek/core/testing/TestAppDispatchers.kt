package app.carbpeek.core.testing

import app.carbpeek.core.common.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher

class TestAppDispatchers(scheduler: TestCoroutineScheduler? = null) : AppDispatchers {
    val dispatcher: TestDispatcher = StandardTestDispatcher(scheduler)
    override val io: CoroutineDispatcher get() = dispatcher
    override val default: CoroutineDispatcher get() = dispatcher
}
