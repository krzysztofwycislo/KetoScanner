package pl.morning.tower.karbo.core.common

interface AppLogger {
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, cause: Throwable? = null)
}
