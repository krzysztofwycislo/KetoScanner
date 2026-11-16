package app.carbpeek.feature.onboarding.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.preferences.api.model.DietPreset

internal class OnboardingResources {

    fun title(preset: DietPreset): UiText = when (preset) {
        DietPreset.STRICT_KETO -> UiText.Res(R.string.onboarding_preset_strict_keto)
        DietPreset.KETO -> UiText.Res(R.string.onboarding_preset_keto)
        DietPreset.LOW_CARB -> UiText.Res(R.string.onboarding_preset_low_carb)
    }

    fun note(preset: DietPreset): UiText = when (preset) {
        DietPreset.STRICT_KETO -> UiText.Res(R.string.onboarding_preset_strict_keto_note)
        DietPreset.KETO -> UiText.Res(R.string.onboarding_preset_keto_note)
        DietPreset.LOW_CARB -> UiText.Res(R.string.onboarding_preset_low_carb_note)
    }

    fun limit(grams: Int): UiText = UiText.Res(R.string.onboarding_grams, UiArg.Numeric.Count(grams.toLong()))

    fun suggested(): UiText = UiText.Res(R.string.onboarding_suggested)

    fun saveFailed(error: AppError): UiText = error.toUiText()
}
