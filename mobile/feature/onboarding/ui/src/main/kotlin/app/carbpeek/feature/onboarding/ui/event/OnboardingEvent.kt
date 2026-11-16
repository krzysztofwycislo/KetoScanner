package app.carbpeek.feature.onboarding.ui.event

sealed interface OnboardingEvent {

    sealed interface OnboardingNavigation : OnboardingEvent {
        data object NavigateToScan : OnboardingNavigation
    }
}
