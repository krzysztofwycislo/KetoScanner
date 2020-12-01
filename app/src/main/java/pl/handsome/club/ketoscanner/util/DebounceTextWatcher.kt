package pl.handsome.club.ketoscanner.util

import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebounceTextWatcher(
    private val coroutineScope: CoroutineScope,
    private val onTextChanged: (String) -> Unit
) : TextWatcher {

    private var debounceJob: Job? = null


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s.toString().trim()

        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(300)
            onTextChanged(text)
        }
    }

    override fun afterTextChanged(s: Editable?) = Unit
}