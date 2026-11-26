package app.carbpeek.shared.contract

private const val SHORT_LENGTH = 8
private const val STANDARD_LENGTH = 13
private const val MAX_LENGTH = 14

fun normalizeBarcode(raw: String): String? {
    if (raw.length > MAX_LENGTH || raw.any { it !in '0'..'9' }) {
        return null
    }
    val digits = raw.trimStart('0')
    return when {
        digits.isEmpty() -> null
        digits.length <= SHORT_LENGTH -> digits.padStart(length = SHORT_LENGTH, padChar = '0')
        else -> digits.padStart(length = STANDARD_LENGTH, padChar = '0')
    }
}
