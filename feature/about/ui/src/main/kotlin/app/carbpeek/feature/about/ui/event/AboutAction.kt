package app.carbpeek.feature.about.ui.event

internal sealed interface AboutAction {
    data object OnBackClicked : AboutAction
}
