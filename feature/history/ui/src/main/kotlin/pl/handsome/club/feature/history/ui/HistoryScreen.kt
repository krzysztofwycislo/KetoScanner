package pl.handsome.club.feature.history.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.RatingIcon
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.history.ui.event.HistoryAction
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnAboutClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnDietPreferencesClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnEntryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnMenuClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnMenuDismissed
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnRetryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnStart
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.feature.history.ui.state.HistoryContentState
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Empty
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Error
import pl.handsome.club.feature.history.ui.state.HistoryContentState.InitialLoad
import pl.handsome.club.feature.history.ui.state.HistoryContentState.ShowItems
import pl.handsome.club.feature.history.ui.state.HistoryItemState
import pl.handsome.club.feature.history.ui.state.HistoryState

@Composable
internal fun HistoryScreen(onNavigation: (HistoryNavigation) -> Unit) {
    val viewModel: HistoryViewModel = koinViewModel()

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart)
        onStopOrDispose {}
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is HistoryNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: HistoryState, onAction: (HistoryAction) -> Unit) {
    Ui.Screen(
        topBar = { TopBar(state, onAction) },
    ) { innerPadding ->
        val centered = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        Ui.ContentCrossfade(targetState = state.content) { content ->
            when (content) {
                InitialLoad -> Box(
                    centered,
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                Empty -> Box(centered, contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.text.default,
                    )
                }

                is Error -> Box(centered, contentAlignment = Alignment.Center) {
                    ErrorContent(
                        content,
                        onAction
                    )
                }

                is ShowItems -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                ) {
                    items(content.items, key = { it.id }) { item ->
                        HistoryRow(item, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(state: HistoryState, onAction: (HistoryAction) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AppTheme.spacing.m, end = AppTheme.spacing.xs, top = AppTheme.spacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.history_welcome),
                style = AppTheme.typography.display,
                color = AppTheme.colors.text.default,
                modifier = Modifier.testTag("welcomeText"),
            )
            Menu(state.isMenuOpen, onAction)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = AppTheme.typography.title,
                color = AppTheme.colors.text.default,
            )
            state.subtitle?.let { subtitle ->
                Text(
                    text = subtitle.resolve(),
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.text.default,
                )
            }
        }
    }
}

@Composable
private fun Menu(isOpen: Boolean, onAction: (HistoryAction) -> Unit) {
    Box {
        IconButton(onClick = { onAction(OnMenuClicked) }) {
            Icon(
                imageVector = AppIcons.More,
                contentDescription = stringResource(R.string.history_menu),
                tint = AppTheme.colors.text.default,
            )
        }
        DropdownMenu(expanded = isOpen, onDismissRequest = { onAction(OnMenuDismissed) }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.history_menu_diet_preferences), style = AppTheme.typography.body) },
                onClick = { onAction(OnDietPreferencesClicked) },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.history_menu_about), style = AppTheme.typography.body) },
                onClick = { onAction(OnAboutClicked) },
            )
        }
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (HistoryAction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.history_retry), style = AppTheme.typography.body)
        }
    }
}

@Composable
private fun HistoryRow(state: HistoryItemState, onAction: (HistoryAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(OnEntryClicked(state.barcode)) },
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            ) {
                Text(
                    text = state.brand.resolve(),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text.default,
                )
                Text(
                    text = state.name.resolve(),
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.text.default,
                )
            }
            Ui.RatingIcon(
                rating = state.rating,
                contentDescription = state.ratingDescription,
                modifier = Modifier.padding(start = AppTheme.spacing.xs),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = AppTheme.spacing.m),
            thickness = AppTheme.borders.hairline,
            color = AppTheme.colors.border.default,
        )
    }
}

private fun HistoryItemState.Companion.example(
    id: Long,
    name: String,
    brand: String,
    rating: Rating,
) = HistoryItemState(
    id = id,
    barcode = "590000000000$id",
    name = UiText.Raw(name),
    brand = UiText.Raw(brand),
    rating = rating,
    ratingDescription = UiText.Res(R.string.history_rating_good),
)


@Preview(showBackground = true)
@Composable
private fun HistoryShowItemsPreview() {
    val items = persistentListOf(
        HistoryItemState.example(1, "Peanut butter", "Nuts & Co", Rating.Positive),
        HistoryItemState.example(2, "Oat cookies", "Bakery", Rating.Neutral),
        HistoryItemState.example(3, "Cola", "Drinks Inc", Rating.Negative),
        HistoryItemState.example(4, "Mystery bar", "Unknown", Rating.Unknown),
    )
    AppTheme {
        ScreenContent(
            HistoryState(
                UiText.Plural(R.plurals.history_last_searches, items.size, UiArg.Numeric.Count(items.size.toLong())),
                ShowItems(items),
                isMenuOpen = false,
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryInitialLoadPreview() {
    AppTheme {
        ScreenContent(HistoryState(null, InitialLoad, isMenuOpen = false)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryEmptyPreview() {
    AppTheme {
        ScreenContent(HistoryState(null, Empty, isMenuOpen = false)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryErrorPreview() {
    AppTheme {
        ScreenContent(
            HistoryState(
                null,
                Error(AppError.Storage.toUiText()),
                isMenuOpen = false,
            )
        ) {}
    }
}
