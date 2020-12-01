package pl.handsome.club.ketoscanner.util

import android.util.Log
import androidx.fragment.app.Fragment


fun Fragment.logException(throwable: Throwable) {
    Log.e(this::class.java.simpleName, throwable.message, throwable)
}

fun Fragment.logWarning(message: String) {
    Log.w(this::class.java.simpleName, message)
}

fun Any.logInfo(message: String) {
    Log.w(this::class.java.simpleName, message)
}