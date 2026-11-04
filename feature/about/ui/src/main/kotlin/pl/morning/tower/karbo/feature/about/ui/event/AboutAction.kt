package pl.morning.tower.karbo.feature.about.ui.event

internal sealed interface AboutAction {
    data object OnBackClicked : AboutAction
}
