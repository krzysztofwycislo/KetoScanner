package pl.handsome.club.ketoscanner.util

import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText

fun EditText.getNotEmptyString(): String? {
    return text.toString().takeIf(String::isNotEmpty)
}

fun Button.disable() {
    isEnabled = false
}

fun Button.enable() {
    isEnabled = true
}

fun EditText.onKeyEnter(block: () -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            block.invoke()
            true
        } else {
            false
        }
    }
}