package app.carbpeek.core.common

interface AppLogger {
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, cause: Throwable? = null)
}
