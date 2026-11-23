package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.Vitamin
import app.carbpeek.feature.product.api.model.Ingredient

private val knownIdPattern = Regex("^en:[a-z0-9-]+$")

private val eNumberPattern = Regex("^en:e((\\d{3,4})[a-z]*)$")

private const val RIBOFLAVIN_E_NUMBER = 101

private const val NIACIN_E_NUMBER = 375

private val printedRiboflavinENumber = Regex("\\be[\\s\\u00A0-]*$RIBOFLAVIN_E_NUMBER(?!\\d)", RegexOption.IGNORE_CASE)

private val polyolENumbers = setOf(420, 421, 953, 964, 965, 966, 967, 968)

private val sweetenerENumbers = setOf(950, 951, 952, 954, 955, 957, 959, 960, 961, 962, 969)

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
    "en:erythritol",
)

private val namedSweeteners = setOf(
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
    "en:molasses",
    "en:treacle",
    "en:malt-extract",
    "en:barley-malt-extract",
)

private val addedSugarWords = setOf("sugar", "sugars", "syrup", "syrups", "honey", "honeys")

private val addedSugarExcludingWords = setOf("with", "without", "no")

private val notAddedSugars = setOf("en:oligofructose-syrup", "en:rakia-sugar")

private val namedAdditives = setOf(
    "en:carob-seed-flour",
    "en:emulsifier",
    "en:emulsifying-salts",
    "en:preservative",
    "en:colour",
    "en:acid",
    "en:acidity-regulator",
    "en:antioxidant",
    "en:stabiliser",
    "en:thickener",
    "en:raising-agent",
    "en:humectant",
    "en:flavour-enhancer",
    "en:gelling-agent",
    "en:firming-agent",
    "en:anti-caking-agent",
    "en:anti-foaming-agent",
    "en:foaming-agent",
    "en:glazing-agent",
    "en:bulking-agent",
    "en:flour-treatment-agent",
    "en:sequestrant",
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
    "en:maltodextrin",
)

private val notStarches = setOf("en:konjac-flour", "en:acerola-powder-on-manioc-starch")

private val milkIngredients = setOf(
    "en:dairy",
    "en:milk",
    "en:cow-s-milk",
    "en:whole-milk",
    "en:semi-skimmed-milk",
    "en:skimmed-milk",
    "en:pasteurised-milk",
    "en:pasteurised-whole-milk",
    "en:pasteurized-semi-skimmed-milk",
    "en:pasteurized-skimmed-milk",
    "en:pasteurized-milk-and-cream",
    "en:goat-milk",
    "en:sheep-milk",
    "en:buffalo-milk",
    "en:haymilk",
    "en:condensed-whole-milk",
    "en:evaporated-milk",
    "en:sweetened-condensed-milk",
    "en:sweetened-condensed-whole-milk",
    "en:sweetened-condensed-skimmed-milk",
    "en:milk-powder",
    "en:whole-milk-powder",
    "en:semi-skimmed-milk-powder",
    "en:skimmed-milk-powder",
    "en:milk-solids",
    "en:milkfat",
    "en:butterfat",
    "en:milk-proteins",
    "en:milk-protein-concentrate",
    "en:milk-protein-isolate",
    "en:hydrolysed-milk-protein",
    "en:casein",
    "en:caseinate",
    "en:calcium-caseinate",
    "en:sodium-caseinate",
    "en:lactose",
    "en:lactose-and-milk-proteins",
    "en:whey",
    "en:whey-powder",
    "en:sweet-whey",
    "en:sweet-whey-powder",
    "en:acid-whey",
    "en:demineralised-whey",
    "en:whey-permeate",
    "en:whey-product",
    "en:whey-protein",
    "en:whey-protein-isolate",
    "en:concentrated-whey-protein",
    "en:buttermilk",
    "en:buttermilk-solids",
    "en:cream",
    "en:single-cream",
    "en:double-cream",
    "en:light-cream",
    "en:fresh-cream",
    "en:heavy-cream",
    "en:whipping-cream",
    "en:whipped-cream",
    "en:sour-cream",
    "en:creme-fraiche",
    "en:smetana",
    "en:clotted-cream",
    "en:butter",
    "en:fresh-butter",
    "en:salted-butter",
    "en:unsalted-butter",
    "en:lightly-salted-butter",
    "en:clarified-butter",
    "en:ghee",
    "en:sweet-cream-butter",
    "en:yogurt",
    "en:plain-yogurt",
    "en:whole-yogurt",
    "en:greek-yogurt",
    "en:skimmed-yogurt",
    "en:mild-yogurt",
    "en:yogurt-powder",
    "en:kefir",
    "en:skyr",
    "en:quark",
    "en:fromage-blanc",
    "en:soft-white-cheese",
    "en:cottage-cheese",
    "en:cream-cheese",
    "en:fresh-cream-cheese",
    "en:ricotta",
    "en:mascarpone",
    "en:cheese",
    "en:hard-cheese",
    "en:semi-hard-cheeses",
    "en:soft-cheese",
    "en:melted-cheese",
    "en:cheese-powder",
    "en:mozzarella",
    "en:cheddar",
    "en:gouda",
    "en:edam",
    "en:emmental",
    "en:gruyere",
    "en:parmigiano-reggiano",
    "en:grana-padano",
    "en:pecorino",
    "en:pecorino-romano",
    "en:feta",
    "en:halloumi",
    "en:brie",
    "en:camembert",
    "en:gorgonzola",
    "en:roquefort",
    "en:blue-cheese",
    "en:provolone",
    "en:manchego",
    "en:maasdam",
    "en:raclette",
    "en:comte",
    "en:goat-cheese",
)

internal fun isKnownIngredientId(id: String): Boolean = id.lowercase().matches(knownIdPattern)

internal fun eNumberOf(ingredient: Ingredient): String? =
    eNumberPattern.find(ingredient.id.lowercase())
        ?.takeIf { vitaminOf(ingredient) == null }
        ?.let { "E" + it.groupValues[1] }

internal fun vitaminOf(ingredient: Ingredient): Vitamin? =
    when (baseENumberOf(ingredient.id)) {
        RIBOFLAVIN_E_NUMBER -> Vitamin.RIBOFLAVIN.takeUnless { ingredient.printsRiboflavinENumber() }
        NIACIN_E_NUMBER -> Vitamin.NIACIN
        else -> null
    }

internal fun ingredientKindOf(ingredient: Ingredient): IngredientKind {
    val key = ingredient.id.lowercase()
    val eNumber = baseENumberOf(key)
    return when {
        !isKnownIngredientId(key) -> IngredientKind.PLAIN
        vitaminOf(ingredient) != null -> IngredientKind.PLAIN
        eNumber != null -> eNumberKindOf(eNumber)
        key in namedPolyols -> IngredientKind.POLYOL
        key in namedSweeteners -> IngredientKind.SWEETENER
        key.isAdditive() -> IngredientKind.ADDITIVE
        key.isAddedSugar() -> IngredientKind.ADDED_SUGAR
        key.isStarch() -> IngredientKind.STARCH
        key in milkIngredients -> IngredientKind.MILK
        else -> IngredientKind.PLAIN
    }
}

private fun baseENumberOf(id: String): Int? =
    eNumberPattern.find(id.lowercase())?.groupValues?.get(2)?.toInt()

private fun Ingredient.printsRiboflavinENumber(): Boolean =
    (listOf(this) + subIngredients).any { printedRiboflavinENumber.containsMatchIn(it.labelName) }

private fun eNumberKindOf(number: Int): IngredientKind =
    when (number) {
        in polyolENumbers -> IngredientKind.POLYOL
        in sweetenerENumbers -> IngredientKind.SWEETENER
        in modifiedStarchENumbers -> IngredientKind.STARCH
        else -> IngredientKind.ADDITIVE
    }

private fun String.isAdditive(): Boolean = endsWith("lecithin") || this in namedAdditives

private fun String.isAddedSugar(): Boolean {
    val words = removePrefix("en:").split('-')
    return when {
        this in notAddedSugars -> false
        words.any { it in addedSugarExcludingWords } -> false
        else -> this in addedSugars ||
            endsWith("syrup-solids") ||
            words.takeWhile { it != "from" }.lastOrNull() in addedSugarWords
    }
}

private fun String.isStarch(): Boolean = this !in notStarches && (this in namedStarches || hasStarchName())

private fun String.hasStarchName(): Boolean =
    contains("starch") || contains("semolina") || contains("breadcrumb") || isHighCarbFlour()

private fun String.isHighCarbFlour(): Boolean = endsWith("flour") && lowCarbFlourSources.none { contains(it) }
