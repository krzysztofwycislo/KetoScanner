package pl.handsome.club.ketoscanner.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.MacronutrientsResultFragmentBinding
import pl.handsome.club.ketoscanner.util.*


class MacronutrientsResultFragment : Fragment(R.layout.macronutrients_result_fragment) {


    fun initializeResults(macronutrientAnalysisResult: MacronutrientAnalysisResult?) {
        val binding = MacronutrientsResultFragmentBinding.bind(requireView())

        if (macronutrientAnalysisResult != null)
            showMacronutrientAnalysisResult(binding, macronutrientAnalysisResult)
        else
            showLackOfMacronutrientAnalysisResult(binding)
    }

    private fun showLackOfMacronutrientAnalysisResult(binding: MacronutrientsResultFragmentBinding) {
        binding.maxServingResultContainer.visibility = View.GONE

        binding.carbsResultImage.setImageResource(R.drawable.question_outline_negative_24dp)

        binding.lackOfCarbsResultSummary.visibility = View.VISIBLE
    }

    private fun showMacronutrientAnalysisResult(
        binding: MacronutrientsResultFragmentBinding,
        result: MacronutrientAnalysisResult
    ) =
        with(result) {
            ContextCompat.getColor(requireContext(), getColorIdForDietRate(carbsRate))
                .also(binding.carbsResultSummary::setTextColor)
                .also(binding.maxServingResultSummary::setTextColor)

            getSummaryResultImageIdForDietRate(carbsRate)
                .also(binding.carbsResultImage::setImageResource)

            getSummaryTextIdForDietRate(carbsRate)
                .also(binding.carbsResultSummary::setText)

            getString(
                getDetailTextIdForDietRate(carbsRate),
                maxRateCarbAmount
            ).also(binding.carbsResultDetails::setText)

            getMaxServingImageIdForDietRate(carbsRate)
                .also(binding.maxServingResultImage::setImageResource)

            getString(
                R.string.maxServingResultDetailsText,
                maxProductPortion,
                dailyCarbConsumption
            ).also(binding.maxServingResultDetails::setText)

        }

}
