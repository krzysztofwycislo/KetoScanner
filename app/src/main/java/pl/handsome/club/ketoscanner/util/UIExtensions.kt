package pl.handsome.club.ketoscanner.util

import android.widget.EditText

fun EditText.getNotEmptyString() : String? {
    return text.toString().takeIf(String::isNotEmpty)
}