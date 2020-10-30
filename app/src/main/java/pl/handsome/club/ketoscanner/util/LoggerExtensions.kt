package pl.handsome.club.ketoscanner.util

import android.util.Log
import androidx.fragment.app.Fragment


fun Fragment.logException(throwable: Throwable) {
    Log.e(this::class.java.simpleName, throwable.message, throwable)
}