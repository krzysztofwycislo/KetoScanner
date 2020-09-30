package pl.handsome.club.ketoscanner.util

import androidx.lifecycle.*

class SingleTimeObserver<T>(
    private val handle: (T) -> Unit
) : Observer<T>, LifecycleOwner {

    private val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onChanged(value: T) {
        handle.invoke(value)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

}