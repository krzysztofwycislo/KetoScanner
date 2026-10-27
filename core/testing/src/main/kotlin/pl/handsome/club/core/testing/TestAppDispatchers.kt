package pl.handsome.club.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import pl.handsome.club.core.common.AppDispatchers

class TestAppDispatchers(scheduler: TestCoroutineScheduler? = null) : AppDispatchers {
    val dispatcher: TestDispatcher = StandardTestDispatcher(scheduler)
    override val io: CoroutineDispatcher get() = dispatcher
    override val default: CoroutineDispatcher get() = dispatcher
}
