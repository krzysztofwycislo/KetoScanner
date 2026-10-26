package pl.handsome.club.ketoscanner.ui

import androidx.fragment.app.Fragment
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.NutrientsTableFragmentBinding


class MacronutrientsTableFragment : Fragment(R.layout.nutrients_table_fragment) {

    fun initializeProductMacronutrientTable(product: Product) {
        val binding = NutrientsTableFragmentBinding.bind(requireView())
        val nutriments = product.nutriments

        val servingAmount = product.servingAmount
        if (servingAmount != null) {
            binding.perServingHeader.text = getString(R.string.per_serving_with_value, servingAmount)
        }

        nutriments.energyPerServing
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(binding.energyPerServing::setText)
        nutriments.energyPer100g
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(binding.energyPer100g::setText)

        nutriments.fatsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.fatsPerServing::setText)
        nutriments.fatsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.fatsPer100g::setText)

        nutriments.proteinsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.proteinsPerServing::setText)
        nutriments.proteinsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.proteinsPer100g::setText)

        nutriments.carbohydratesPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.carbsPerServing::setText)
        nutriments.carbohydratesPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.carbsPer100g::setText)

        nutriments.saltPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.saltPerServing::setText)
        nutriments.saltPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(binding.saltPer100g::setText)
    }

}