package pl.morning.tower.karbo.util

import android.util.Log
import pl.morning.tower.karbo.core.common.AppLogger


internal object AndroidAppLogger : AppLogger {

    override fun warn(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun error(tag: String, message: String, cause: Throwable?) {
        Log.e(tag, message, cause)
    }

}
