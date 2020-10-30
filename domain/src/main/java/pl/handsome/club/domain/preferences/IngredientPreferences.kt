package pl.handsome.club.domain.preferences


data class IngredientPreferences(
    val onlyHealthyIngredient: Boolean,
    val allergensToAvoid: List<Allergen>,
    val otherToAvoid: List<Ingredient>,
)
