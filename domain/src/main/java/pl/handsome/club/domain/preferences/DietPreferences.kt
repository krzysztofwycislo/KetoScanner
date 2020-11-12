package pl.handsome.club.domain.preferences

data class DietPreferences(
    val kcalPerDay: Int,
    val macronutrientPreferences: MacronutrientPreferences,
    val ingredientPreferences: IngredientPreferences
)
