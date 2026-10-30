package pl.handsome.club.feature.favourites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.ListRowCard
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.SearchField
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictPill
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcon
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnItemClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnQueryChanged
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnRetryClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnSearchSubmitted
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import pl.handsome.club.feature.favourites.ui.state.FavouriteItemState
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Empty
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Error
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.NoResults
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.ShowItems
import pl.handsome.club.feature.favourites.ui.state.FavouritesState

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
                val centered = Modifier.fillMaxSize()
                when (content) {
                    InitialLoad -> Box(centered, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    Empty -> Box(centered, contentAlignment = Alignment.Center) {
                        EmptyContent()
                    }

                    is NoResults -> Box(
                        centered.padding(horizontal = AppTheme.spacing.l),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = content.message.resolve(),
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.text.muted,
                            textAlign = TextAlign.Center,
                        )
                    }

                    is Error -> Box(centered, contentAlignment = Alignment.Center) {
                        ErrorContent(content, onAction)
                    }

                    is ShowItems -> Items(content.items, onAction)
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
private fun EmptyContent() {
    Column(
        modifier = Modifier.padding(horizontal = AppTheme.spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.favourites_empty_title),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.favourites_empty_body),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Items(items: PersistentList<FavouriteItemState>, onAction: (FavouritesAction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        items(items, key = { it.barcode }) { item ->
            Ui.ListRowCard(
                title = item.name,
                subtitle = item.brand,
                onClick = { onAction(OnItemClicked(item.barcode)) },
                leading = {
                    Ui.IconTile(item.icon.imageVector)
                },
                trailing = {
                    Ui.VerdictPill(
                        rating = item.rating,
                        text = item.carbs,
                        contentDescription = item.verdictDescription,
                    )
                },
            )
        }
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
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.l),
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.favourites_retry), style = AppTheme.typography.body)
        }
    }
}

private fun FavouriteItemState.Companion.example(
    barcode: String,
    name: String,
    icon: AppIcon,
    brand: String?,
    rating: Rating,
    carbs: Double?,
) = FavouriteItemState(
    barcode = barcode,
    name = UiText.Raw(name),
    icon = icon,
    brand = brand?.let(UiText::Raw),
    rating = rating,
    carbs = if (carbs == null) UiText.Res(R.string.favourites_carbs_missing)
    else UiText.Res(R.string.favourites_carbs, UiArg.Numeric.Grams(carbs)),
    verdictDescription = UiText.Res(R.string.favourites_verdict_good_keto),
)

@Preview(showBackground = true)
@Composable
private fun FavouritesShowItemsPreview() {
    val items = persistentListOf(
        FavouriteItemState.example("5900000000001", "Peanut butter", AppIcons.Jar, "Nuts & Co", Rating.Positive, 14.0),
        FavouriteItemState.example("5900000000002", "Oat cookies", AppIcons.Cookie, null, Rating.Neutral, 52.3),
        FavouriteItemState.example("5900000000003", "Cola", AppIcons.Can, "Fizz", Rating.Negative, 10.6),
        FavouriteItemState.example("5900000000004", "Mystery bar", AppIcons.Bag, null, Rating.Unknown, null),
    )
    AppTheme {
        ScreenContent(FavouritesState(query = "", showSearch = true, content = ShowItems(items))) {}
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
