package pl.handsome.club.domain

import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.preferences.MacronutrientPreferences


val exampleKetoAdaptationPreferences = MacronutrientPreferences(
    80,
    30,
    5,
    15
)

val exampleKetoPreferences = MacronutrientPreferences(
    75,
    40,
    10,
    15
)

val exampleIngredientPreferences = IngredientPreferences(
    true,
    emptyList(),
    emptyList()
)