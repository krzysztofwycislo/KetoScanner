package pl.handsome.club.domain.preferences

data class DietPreferences(
    val kcalPerDay: Int,
    val ingredientPreferences: IngredientPreferences?,
    val macronutrientPreferences: MacronutrientPreferences?
)
