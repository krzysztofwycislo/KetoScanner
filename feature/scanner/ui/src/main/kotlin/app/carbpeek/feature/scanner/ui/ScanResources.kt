package app.carbpeek.feature.scanner.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.preferences.api.model.DietMode

internal class ScanResources {

    fun rationaleTitle(): UiText = UiText.Res(R.string.scan_camera_permission_rationale_title)

    fun deniedTitle(): UiText = UiText.Res(R.string.scan_camera_permission_denied_title)

    fun permissionRationale(): UiText = UiText.Res(R.string.scan_camera_permission_rationale)

    fun allowCamera(): UiText = UiText.Res(R.string.scan_camera_permission_allow)

    fun permissionDenied(): UiText = UiText.Res(R.string.scan_camera_permission_denied)

    fun openSettings(): UiText = UiText.Res(R.string.scan_camera_permission_open_settings)

    fun aimHint(): UiText = UiText.Res(R.string.scan_aim_hint)

    fun searchingHint(): UiText = UiText.Res(R.string.scan_searching_hint)

    fun firstScanHintTitle(): UiText = UiText.Res(R.string.scan_first_hint_title)

    fun firstScanHintBody(mode: DietMode): UiText {
        val id = when (mode) {
            DietMode.KETO -> R.string.scan_first_hint_body_keto
            DietMode.LOW_CARB -> R.string.scan_first_hint_body_low_carb
        }
        return UiText.Res(id)
    }

    fun torchOn(): UiText = UiText.Res(R.string.scan_torch_on)

    fun torchOff(): UiText = UiText.Res(R.string.scan_torch_off)

    fun searchFailed(error: AppError): UiText = error.toUiText()

    fun notFoundMessage(): UiText = UiText.Res(R.string.scan_not_found_message)

    fun notFoundBarcode(barcode: String): UiText = UiText.Res(R.string.scan_not_found_barcode, UiArg.Text(barcode))

    fun scanNext(): UiText = UiText.Res(R.string.scan_next)

    fun close(): UiText = UiText.Res(R.string.scan_close)

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.scan_found_unnamed) else UiText.Raw(name)

    fun brand(brand: String): UiText? = if (brand.isBlank()) null else UiText.Raw(brand)

    fun verdictTitle(rate: DietRate, mode: DietMode): UiText {
        val id = when (rate) {
            DietRate.GOOD -> when (mode) {
                DietMode.KETO -> R.string.scan_verdict_good_keto
                DietMode.LOW_CARB -> R.string.scan_verdict_good_low_carb
            }
            DietRate.NEUTRAL -> R.string.scan_verdict_neutral
            DietRate.NOT_ADVISED -> when (mode) {
                DietMode.KETO -> R.string.scan_verdict_not_advised_keto
                DietMode.LOW_CARB -> R.string.scan_verdict_not_advised_low_carb
            }
        }
        return UiText.Res(id)
    }

    fun insufficientData(): UiText = UiText.Res(R.string.scan_verdict_insufficient_data)

    fun insufficientDataDetail(): UiText = UiText.Res(R.string.scan_found_insufficient_detail)

    fun carbsPer100g(grams: Double): UiText = UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(grams))

    fun foundDetail(result: MacronutrientAnalysisResult): UiText =
        result.reasons.firstOrNull()?.let(::reason) ?: carbsPer100g(result.carbsPer100g)

    private fun reason(reason: RatingReason): UiText = when (reason) {
        is RatingReason.HighCarbs ->
            carbsPer100g(reason.gramsPer100g)

        is RatingReason.HighCarbEnergyShare ->
            UiText.Res(
                R.string.scan_found_reason_carb_energy_share,
                UiArg.Numeric.Percent(reason.share),
            )

        is RatingReason.LowFatAndProteinToCarbRatio ->
            UiText.Res(R.string.scan_found_reason_low_fat_and_protein_to_carb_ratio)

        is RatingReason.HighSugars ->
            UiText.Res(
                R.string.scan_found_reason_high_sugars,
                UiArg.Numeric.Grams(reason.gramsPer100g),
            )

        is RatingReason.HighCarbsPerServing ->
            UiText.Res(
                R.string.scan_found_reason_high_carbs_per_serving,
                UiArg.Numeric.Grams(reason.carbsGrams),
                UiArg.Numeric.Grams(reason.servingGrams),
            )
    }

    fun tripCarbs(grams: Double?): UiText =
        if (grams == null) UiText.Res(R.string.scan_trip_carbs_missing)
        else UiText.Res(R.string.scan_trip_carbs, UiArg.Numeric.Grams(grams))

    fun tripDescription(rate: DietRate?, mode: DietMode, carbs: Double?): UiText {
        val verdict = if (rate == null) insufficientData() else verdictTitle(rate, mode)
        return if (carbs == null) verdict
        else UiText.Res(
            R.string.scan_trip_verdict_with_carbs,
            UiArg.Nested(verdict),
            UiArg.Nested(carbsPer100g(carbs)),
        )
    }
}
