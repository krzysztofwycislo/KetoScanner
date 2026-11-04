package pl.morning.tower.karbo.feature.about.ui.event

sealed interface AboutEvent {

    sealed interface AboutNavigation : AboutEvent {
        data object NavigateBack : AboutNavigation
    }
}
