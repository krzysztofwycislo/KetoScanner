package app.carbpeek.feature.favourites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.ContentCrossfade
import app.carbpeek.core.designsystem.component.EmptyState
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.SearchField
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.bottomBarClearance
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.favourites.ui.component.Groups
import app.carbpeek.feature.favourites.ui.component.previewGroups
import app.carbpeek.feature.favourites.ui.component.previewSearchGroups
import app.carbpeek.feature.favourites.ui.event.FavouritesAction
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnQueryChanged
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnRetryClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnSearchSubmitted
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnStartScanningClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.Empty
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.Error
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.NoResults
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.ShowGroups
import app.carbpeek.feature.favourites.ui.state.FavouritesState
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FavouritesScreen(onNavigation: (FavouritesNavigation) -> Unit) {
    val viewModel: FavouritesViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is FavouritesNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: FavouritesState, onAction: (FavouritesAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Header()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
        ) {
            if (state.showSearch) {
                Ui.SearchField(
                    value = state.query,
                    onValueChange = { onAction(OnQueryChanged(it)) },
                    placeholder = stringResource(R.string.favourites_search_placeholder),
                    onSearch = { onAction(OnSearchSubmitted) },
                    modifier = Modifier
                        .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, top = AppTheme.spacing.s)
                        .testTag("favouritesSearchInput"),
                )
            }
            Ui.ContentCrossfade(
                targetState = state.content,
                modifier = Modifier.weight(1f),
            ) { content ->
                val centered = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomBarClearance())
                when (content) {
                    InitialLoad -> Box(centered, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    Empty -> Box(centered, contentAlignment = Alignment.Center) {
                        Ui.EmptyState(
                            icon = AppIcons.Heart,
                            title = stringResource(R.string.favourites_empty_title),
                            body = stringResource(R.string.favourites_empty_body),
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.xxl),
                            actionText = stringResource(R.string.favourites_start_scanning),
                            onAction = { onAction(OnStartScanningClicked) },
                        )
                    }

                    is NoResults -> Box(
                        centered.padding(horizontal = AppTheme.spacing.l),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = content.message.resolve(),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.text.muted,
                            textAlign = TextAlign.Center,
                        )
                    }

                    is Error -> Box(centered, contentAlignment = Alignment.Center) {
                        ErrorContent(content, onAction)
                    }

                    is ShowGroups -> Groups(content.groups, onAction)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, top = AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.favourites_title),
            style = AppTheme.typography.display,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = stringResource(R.string.favourites_subtitle),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (FavouritesAction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.l),
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.favourites_retry), style = AppTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesShowGroupsPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", showSearch = true, content = ShowGroups(previewGroups))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesSearchGroupsPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "nut", showSearch = true, content = ShowGroups(previewSearchGroups))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesInitialLoadPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", showSearch = false, content = InitialLoad)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesEmptyPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", showSearch = false, content = Empty)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesNoResultsPreview() {
    AppTheme {
        ScreenContent(
            FavouritesState(
                query = "cola",
                showSearch = true,
                content = NoResults(UiText.Res(R.string.favourites_no_results, UiArg.Text("cola"))),
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesErrorPreview() {
    AppTheme {
        ScreenContent(
            FavouritesState(query = "", showSearch = false, content = Error(AppError.Storage.toUiText())),
        ) {}
    }
}
