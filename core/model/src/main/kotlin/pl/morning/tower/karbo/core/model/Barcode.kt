package pl.handsome.club.core.model

import kotlin.jvm.JvmInline

/** Product barcode as scanned or typed. It is not validated. */
@JvmInline
value class Barcode(val value: String)
