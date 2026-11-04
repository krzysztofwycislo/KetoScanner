package pl.morning.tower.karbo.core.testing

import pl.morning.tower.karbo.core.common.AppLogger

class FakeAppLogger : AppLogger {

    enum class Level { Warn, Error }

    data class Entry(val level: Level, val tag: String, val message: String, val cause: Throwable?)

    val entries = mutableListOf<Entry>()

    override fun warn(tag: String, message: String) {
        entries += Entry(Level.Warn, tag, message, null)
    }

    override fun error(tag: String, message: String, cause: Throwable?) {
        entries += Entry(Level.Error, tag, message, cause)
    }
}
