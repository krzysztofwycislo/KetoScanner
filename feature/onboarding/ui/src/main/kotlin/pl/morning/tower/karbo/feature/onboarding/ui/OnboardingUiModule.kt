package pl.morning.tower.karbo.feature.onboarding.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.onboarding.ui.state.OnboardingStateFactory

val onboardingUiModule = module {
    viewModelOf(::OnboardingViewModel)
    factoryOf(::OnboardingStateFactory)
    factoryOf(::OnboardingResources)
}
