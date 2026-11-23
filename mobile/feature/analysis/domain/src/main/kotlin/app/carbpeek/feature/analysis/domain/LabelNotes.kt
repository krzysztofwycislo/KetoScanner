package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.product.api.model.Ingredient

private val mayContainPrefixes = listOf(
    "may contain",
    "may also contain",
    "can contain",
    "manufactured in a unit that uses",
    "contains traces",
    "traces of",
    "trace of",
    "kann ",
    "spuren von",
    "enthält spuren",
    "peut contenir",
    "peuvent contenir",
    "peut aussi contenir",
    "traces de",
    "traces d'",
    "traces éventuelles",
    "contient des traces",
    "puede contener",
    "pueden contener",
    "trazas de",
    "contiene trazas",
    "può contenere",
    "puo contenere",
    "potrebbe contenere",
    "tracce di",
    "contiene tracce",
    "może zawierać",
    "moze zawierac",
    "może również zawierać",
    "produkt może zawierać",
    "śladowe ilości",
    "kan ",
    "sporen van",
    "bevat sporen",
    "pode conter",
    "podem conter",
    "os ingredientes em negrito contém alergénios pode conter",
    "vestígios de",
    "contém vestígios",
    "může obsahovat",
    "môže obsahovať",
    "poate conține",
    "var saturēt",
    "var saturet",
)

private val warningPrefixes = listOf(
    "excessive consumption",
    "contains a source of phenylalanine",
    "bold ingredients contain allergens",
    "übermäßiger verzehr",
    "bei übermäßigem verzehr",
    "enthält eine phenylalaninquelle",
    "allergenhinweis",
    "allergiehinweis",
    "consommation excessive",
    "une consommation excessive",
    "contient une source de phénylalanine",
    "consumo excesivo",
    "un consumo excesivo",
    "contiene una fuente de fenilalanina",
    "consumo eccessivo",
    "un consumo eccessivo",
    "contiene una fonte di fenilalanina",
    "per gli allergeni",
    "allergeni contiene",
    "gli ingredienti evidenziati",
    "spożycie w nadmiernych",
    "nadmierne spożycie",
    "zawiera źródło fen",
    "overmatig gebruik",
    "bevat een bron van fenylalanine",
    "consumo excessivo",
    "o consumo excessivo",
    "contém uma fonte de fenilalanina",
)

private val storagePrefixes = listOf(
    "store in",
    "keep refrigerated",
    "shake before",
    "to be consumed",
    "best before",
    "after opening",
    "conservare ",
    "per conservare al meglio",
    "conserver ",
    "conservar ",
    "à conserver",
    "à consommer",
    "da consumarsi",
    "przechowywać",
    "najlepiej spożyć",
    "należy spożyć",
)

private val notePrefixes = mayContainPrefixes + warningPrefixes + storagePrefixes

private val allergens = setOf(
    "en:milk",
    "en:dairy",
    "en:lactose",
    "en:egg",
    "en:nut",
    "en:peanut",
    "en:almond",
    "en:hazelnut",
    "en:walnut",
    "en:cashew-nuts",
    "en:pecan-nut",
    "en:brazil-nut",
    "en:pistachio-nuts",
    "en:macadamia-nut",
    "en:soya",
    "en:soya-bean",
    "en:sesame",
    "en:sesame-seeds",
    "en:mustard",
    "en:mustard-seed",
    "en:celery",
    "en:fish",
    "en:crustacean",
    "en:mollusc",
    "en:lupin",
    "en:lupin-bean",
    "en:gluten",
    "en:cereals-containing-gluten",
    "en:wheat",
    "en:rye",
    "en:barley",
    "en:oat",
    "en:spelt",
    "en:sulfite",
)

internal fun List<Ingredient>.withoutLabelNotes(): List<Ingredient> {
    val mayContainIndex = indices.firstOrNull { index ->
        this[index].startsWithAny(mayContainPrefixes) && drop(index + 1).hasOnlyAllergensAndUnknowns()
    }
    return filterIndexed { index, ingredient ->
        val isAfterMayContain = mayContainIndex != null && index > mayContainIndex
        !ingredient.isLabelNote() && !(isAfterMayContain && ingredient.isBareAllergen())
    }
}

private fun List<Ingredient>.hasOnlyAllergensAndUnknowns(): Boolean =
    all { !isKnownIngredientId(it.id) || it.isAllergen() || it.isLabelNote() }

private fun Ingredient.isLabelNote(): Boolean = noteText().isEmpty() || startsWithAny(notePrefixes)

private fun Ingredient.startsWithAny(prefixes: List<String>): Boolean = prefixes.any { noteText().startsWith(it) }

private fun Ingredient.noteText(): String = labelName.trimStart { !it.isLetter() }.lowercase().replace('’', '\'')

private fun Ingredient.isBareAllergen(): Boolean =
    isAllergen() && subIngredients.isEmpty() && declaredPercent == null

private fun Ingredient.isAllergen(): Boolean = id.lowercase() in allergens
