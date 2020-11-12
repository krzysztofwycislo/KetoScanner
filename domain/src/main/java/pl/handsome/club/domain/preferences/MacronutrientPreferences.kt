package pl.handsome.club.domain.preferences


data class MacronutrientPreferences(
    val fatPercentage: Int,
    val carbohydratesPercentage: Int,
    val proteinsPercentage: Int,
    val maxCarbohydratesAmount: Int
) {

    init {
        val macroSummary = fatPercentage + carbohydratesPercentage + proteinsPercentage
        if(macroSummary != 100) {
            throw IllegalStateException("Macronutrients sum must be equal to 100 ")
        }
    }

}
