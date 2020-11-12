package pl.handsome.club.domain

import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.preferences.MacronutrientPreferences


val exampleKetoAdaptationPreferences = MacronutrientPreferences(
    80,
    5,
    15,
    30
)

val exampleKetoPreferences = MacronutrientPreferences(
    75,
    10,
    15,
    40
)

val exampleIngredientPreferences = IngredientPreferences(
    true,
    emptyList(),
    emptyList()
)