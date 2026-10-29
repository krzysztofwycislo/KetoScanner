package pl.handsome.club.feature.favourites.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.ProductImage
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.TopBar
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
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
fun FavouritesScreen(onNavigation: (FavouritesNavigation) -> Unit) {
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
            Column {
                Ui.TopBar(title = stringResource(R.string.favourites_title))
                SearchInput(state.query, onAction)
            }
        },
    ) { innerPadding ->
        val centered = Modifier
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
            .fillMaxSize()
        Ui.ContentCrossfade(targetState = state.content) { content ->
            when (content) {
                InitialLoad -> Box(centered, contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                Empty -> Box(centered, contentAlignment = Alignment.Center) {
                    Message(UiText.Res(R.string.favourites_empty))
                }

                is NoResults -> Box(centered, contentAlignment = Alignment.Center) {
                    Message(content.message)
                }

                is Error -> Box(centered, contentAlignment = Alignment.Center) {
                    ErrorContent(content, onAction)
                }

                is ShowItems -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars)),
                    contentPadding = innerPadding,
                ) {
                    items(content.items, key = { it.barcode }) { item ->
                        FavouriteRow(item, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInput(query: String, onAction: (FavouritesAction) -> Unit) {
    TextField(
        value = query,
        onValueChange = { onAction(OnQueryChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.xs)
            .testTag("favouritesSearchInput"),
        textStyle = AppTheme.typography.body,
        placeholder = { Text(text = stringResource(R.string.favourites_search_hint), style = AppTheme.typography.body) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onAction(OnSearchSubmitted) }),
        shape = AppTheme.shapes.small,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background.default,
            unfocusedContainerColor = AppTheme.colors.background.default,
            focusedTextColor = AppTheme.colors.text.default,
            unfocusedTextColor = AppTheme.colors.text.default,
            focusedIndicatorColor = AppTheme.colors.accent,
            unfocusedIndicatorColor = AppTheme.colors.border.default,
            cursorColor = AppTheme.colors.accent,
        ),
    )
}

@Composable
private fun Message(text: UiText) {
    Text(
        text = text.resolve(),
        style = AppTheme.typography.body,
        color = AppTheme.colors.text.default,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(AppTheme.spacing.m),
    )
}

@Composable
private fun ErrorContent(state: Error, onAction: (FavouritesAction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Message(state.message)
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.favourites_retry), style = AppTheme.typography.body)
        }
    }
}

@Composable
private fun FavouriteRow(state: FavouriteItemState, onAction: (FavouritesAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(OnItemClicked(state.barcode)) },
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            Ui.ProductImage(
                url = state.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.thumbnail),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            ) {
                state.brand?.let { brand ->
                    Text(
                        text = brand.resolve(),
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.text.default,
                    )
                }
                Text(
                    text = state.name.resolve(),
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.text.default,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = AppTheme.spacing.m),
            thickness = AppTheme.borders.hairline,
            color = AppTheme.colors.border.default,
        )
    }
}

private fun FavouriteItemState.Companion.example(barcode: String, name: String, brand: String?) = FavouriteItemState(
    barcode = barcode,
    name = UiText.Raw(name),
    brand = brand?.let(UiText::Raw),
    imageUrl = null,
)

@Preview(showBackground = true)
@Composable
private fun FavouritesShowItemsPreview() {
    AppTheme {
        ScreenContent(
            FavouritesState(
                query = "",
                content = ShowItems(
                    persistentListOf(
                        FavouriteItemState.example("5900000000001", "Peanut butter", "Nuts & Co"),
                        FavouriteItemState.example("5900000000002", "Oat cookies", null),
                    )
                ),
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesInitialLoadPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", content = InitialLoad)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesEmptyPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", content = Empty)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesNoResultsPreview() {
    AppTheme {
        ScreenContent(
            FavouritesState(
                query = "cola",
                content = NoResults(UiText.Res(R.string.favourites_no_results, UiArg.Text("cola"))),
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesErrorPreview() {
    AppTheme {
        ScreenContent(FavouritesState(query = "", content = Error(AppError.Storage.toUiText()))) {}
    }
}
