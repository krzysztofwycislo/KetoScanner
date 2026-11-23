package app.carbpeek.openfoodapi

import app.carbpeek.openfoodapi.data.ApiIngredient

private const val GAP = "[\\s\\u00A0_*]*"
private const val BARE_PERCENT = "\\d+(?:[.,]\\d+)?[\\s\\u00A0]*%"
private const val BRACKETED_PERCENT = "\\([\\s\\u00A0]*$BARE_PERCENT[\\s\\u00A0]*\\)"
private const val OPTIONAL_PERCENT = "(?:(?:$BARE_PERCENT|$BRACKETED_PERCENT)$GAP)?"
private const val CONTENTS_BRACKET = "(?!$BRACKETED_PERCENT)[(\\[\\{]"
private const val SEPARATOR = "(?:^|[;:(\\[\\{]|,(?!\\d)|(?<!\\d),)"
private val separatorAtEnd = Regex("$SEPARATOR$GAP$OPTIONAL_PERCENT$")

internal fun isNestingTrusted(ingredient: ApiIngredient, labelText: String?): Boolean {
    val nested = ingredient.ingredients.orEmpty()
    val labelName = ingredient.text?.trim().orEmpty()
    if (labelText == null || labelName.isEmpty() || nested.isEmpty() || !nested.all { it.hasEnglishTaxonomyId() }) {
        return false
    }
    val occurrences = Regex(Regex.escape(labelName) + GAP + OPTIONAL_PERCENT + CONTENTS_BRACKET)
        .findAll(labelText)
        .toList()
    return occurrences.isNotEmpty() &&
        occurrences.all { separatorAtEnd.containsMatchIn(labelText.take(it.range.first)) }
}

private fun ApiIngredient.hasEnglishTaxonomyId(): Boolean =
    isInTaxonomy == IN_TAXONOMY && id?.startsWith(ENGLISH_TAG_PREFIX) == true
