package pl.handsome.club.ketoscanner.util

import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observeOnce(handler: (T) -> Unit) {
    val observer = SingleTimeObserver(handler)
    this.observe(observer, observer)
}