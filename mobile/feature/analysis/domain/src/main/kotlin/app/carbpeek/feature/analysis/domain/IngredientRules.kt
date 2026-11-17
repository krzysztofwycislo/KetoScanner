package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientKind

private val eNumberPattern = Regex("^en:e(\\d{3,4})[a-z]*$")

private val polyolENumbers = setOf(420, 421, 953, 965, 966, 967)

private val sweetenerENumbers = setOf(950, 951, 952, 954, 955, 957, 959, 960, 961, 962, 968, 969)

private val modifiedStarchENumbers = 1400..1452

private val namedPolyols = setOf(
    "en:maltitol",
    "en:maltitol-syrup",
    "en:sorbitol",
    "en:sorbitol-syrup",
    "en:xylitol",
    "en:isomalt",
    "en:lactitol",
    "en:mannitol",
)

private val namedSweeteners = setOf(
    "en:erythritol",
    "en:stevia",
    "en:steviol-glycosides",
    "en:stevia-extract",
    "en:sucralose",
    "en:aspartame",
    "en:acesulfame-k",
    "en:saccharin",
    "en:monk-fruit",
    "en:monk-fruit-extract",
    "en:sweetener",
)

private val addedSugars = setOf(
    "en:sugar",
    "en:sucrose",
    "en:glucose",
    "en:dextrose",
    "en:fructose",
    "en:maltose",
    "en:maltodextrin",
    "en:molasses",
    "en:treacle",
    "en:caramel",
    "en:malt-extract",
    "en:barley-malt-extract",
    "en:sweetened-condensed-milk",
)

private val lowCarbFlourSources = listOf(
    "almond",
    "hazelnut",
    "coconut",
    "flax",
    "linseed",
    "sunflower",
    "pumpkin",
    "psyllium",
    "lupin",
    "soy",
    "walnut",
    "sesame",
    "peanut",
)

private val namedStarches = setOf(
    "en:wheat",
    "en:rice",
    "en:oats",
    "en:oat-flakes",
    "en:rolled-oats",
    "en:potato",
    "en:potatoes",
    "en:potato-flakes",
    "en:dehydrated-potatoes",
    "en:corn",
    "en:maize",
)

private val lactoseSources = setOf(
    "en:lactose",
    "en:milk",
    "en:whole-milk",
    "en:skimmed-milk",
    "en:semi-skimmed-milk",
    "en:pasteurised-milk",
    "en:milk-powder",
    "en:skimmed-milk-powder",
    "en:whole-milk-powder",
    "en:whey",
    "en:whey-powder",
    "en:buttermilk",
)

internal fun ingredientKindOf(id: String): IngredientKind {
    val key = id.lowercase()
    val eNumber = eNumberPattern.find(key)?.groupValues?.get(1)?.toInt()
    return when {
        eNumber != null -> eNumberKindOf(eNumber)
        key in namedPolyols -> IngredientKind.POLYOL
        key in namedSweeteners -> IngredientKind.SWEETENER
        key.isAddedSugar() -> IngredientKind.ADDED_SUGAR
        key.isStarch() -> IngredientKind.STARCH
        key in lactoseSources -> IngredientKind.LACTOSE
        else -> IngredientKind.PLAIN
    }
}

private fun eNumberKindOf(number: Int): IngredientKind =
    when (number) {
        in polyolENumbers -> IngredientKind.POLYOL
        in sweetenerENumbers -> IngredientKind.SWEETENER
        in modifiedStarchENumbers -> IngredientKind.STARCH
        else -> IngredientKind.ADDITIVE
    }

private fun String.isAddedSugar(): Boolean =
    this in addedSugars || endsWith("-sugar") || contains("syrup") || contains("honey")

private fun String.isStarch(): Boolean =
    contains("starch") ||
        contains("semolina") ||
        contains("breadcrumb") ||
        isHighCarbFlour() ||
        this in namedStarches

private fun String.isHighCarbFlour(): Boolean = endsWith("flour") && lowCarbFlourSources.none { contains(it) }
