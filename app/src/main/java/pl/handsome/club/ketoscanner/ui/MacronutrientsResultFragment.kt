package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.macronutrients_result_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.*
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel


class MacronutrientsResultFragment : Fragment(R.layout.macronutrients_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analysisState = analyzeProductViewModel.getProductAnalysisState().value
        if (analysisState is ProductAnalysisState.Success) {
            val macronutrientAnalysisResult = analysisState.result.macronutrientAnalysisResult
            if (macronutrientAnalysisResult != null)
                showMacronutrientAnalysisResult(macronutrientAnalysisResult)
            else
                showLackOfMacronutrientAnalysisResult()
        }
    }

    private fun showLackOfMacronutrientAnalysisResult() {
        maxServingResultContainer.visibility = View.GONE

        carbsResultImage.setImageResource(R.drawable.question_outline_negative_24dp)

        lackOfCarbsResultSummary.visibility = View.VISIBLE
    }

    private fun showMacronutrientAnalysisResult(result: MacronutrientAnalysisResult) =
        with(result) {
            ContextCompat.getColor(requireContext(), getColorIdForDietRate(carbsRate))
                .also(carbsResultSummary::setTextColor)
                .also(maxServingResultSummary::setTextColor)

            getSummaryResultImageIdForDietRate(carbsRate)
                .also(carbsResultImage::setImageResource)

            getSummaryTextIdForDietRate(carbsRate)
                .also(carbsResultSummary::setText)

            getString(
                getDetailTextIdForDietRate(carbsRate),
                maxRateCarbAmount
            ).also(carbsResultDetails::setText)

            getMaxServingImageIdForDietRate(carbsRate)
                .also(maxServingResultImage::setImageResource)

            getString(
                R.string.maxServingResultDetailsText,
                maxProductPortion,
                dailyCarbConsumption
            ).also(maxServingResultDetails::setText)

        }

}