package pl.handsome.club.ketoscanner.util

import android.util.Log
import androidx.fragment.app.Fragment
import pl.handsome.club.core.common.AppError


fun Fragment.logAppError(error: AppError) {
    Log.e(this::class.java.simpleName, error.toString(), (error as? AppError.Unknown)?.cause)
}

fun Fragment.logWarning(message: String) {
    Log.w(this::class.java.simpleName, message)
}

fun Any.logInfo(message: String) {
    Log.w(this::class.java.simpleName, message)
}
