package pl.handsome.club.ketoscanner.ui

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.nutrients_table_fragment.*
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R


class MacronutrientsTableFragment : Fragment(R.layout.nutrients_table_fragment) {

    fun initializeProductMacronutrientTable(product: Product) {
        val nutriments = product.nutriments

        val servingAmount = product.servingAmount
        if (servingAmount != null) {
            perServingHeader.text = getString(R.string.per_serving_with_value, servingAmount)
        }

        nutriments.energyPerServing
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(energyPerServing::setText)
        nutriments.energyPer100g
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(energyPer100g::setText)

        nutriments.fatsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(fatsPerServing::setText)
        nutriments.fatsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(fatsPer100g::setText)

        nutriments.proteinsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(proteinsPerServing::setText)
        nutriments.proteinsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(proteinsPer100g::setText)

        nutriments.carbohydratesPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(carbsPerServing::setText)
        nutriments.carbohydratesPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(carbsPer100g::setText)

        nutriments.saltPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(saltPerServing::setText)
        nutriments.saltPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(saltPer100g::setText)
    }

}