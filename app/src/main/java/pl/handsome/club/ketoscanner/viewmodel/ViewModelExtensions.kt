package pl.handsome.club.ketoscanner.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun ViewModel.logException(t: Throwable) {
    Log.e(this.javaClass.simpleName, t.message, t)
}

fun ViewModel.launchOnIO(
    coroutineExceptionHandler: CoroutineExceptionHandler,
    block: suspend () -> Unit
) {
    viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        block.invoke()
    }
}