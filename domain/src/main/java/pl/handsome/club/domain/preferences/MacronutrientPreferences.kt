package pl.handsome.club.domain.preferences

import java.lang.IllegalStateException


data class MacronutrientPreferences(
    val fatPercentage: Int,
    val carbohydratesPercentage: Int,
    val proteinsPercentage: Int
) {

    init {
        val macroSummary = fatPercentage + carbohydratesPercentage + proteinsPercentage
        if(macroSummary != 100) {
            throw IllegalStateException("Macronutrients sum must be equal to 100 ")
        }
    }

}
