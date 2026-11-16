package app.carbpeek.feature.onboarding.ui

import app.carbpeek.feature.onboarding.ui.state.OnboardingStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingUiModule = module {
    viewModelOf(::OnboardingViewModel)
    factoryOf(::OnboardingStateFactory)
    factoryOf(::OnboardingResources)
}
