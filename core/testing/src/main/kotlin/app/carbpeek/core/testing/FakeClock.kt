package app.carbpeek.core.testing

import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock(var now: Instant) : Clock {
    override fun now(): Instant = now
}
