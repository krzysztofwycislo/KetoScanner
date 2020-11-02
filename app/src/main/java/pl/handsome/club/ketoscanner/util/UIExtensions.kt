package pl.handsome.club.ketoscanner.util

import android.widget.Button
import android.widget.EditText

fun EditText.getNotEmptyString() : String? {
    return text.toString().takeIf(String::isNotEmpty)
}

fun Button.disable() {
    isEnabled = false
}

fun Button.enable() {
    isEnabled = true
}