package pl.handsome.club.feature.history.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.FilterChip
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.ListRowCard
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.SearchField
import pl.handsome.club.core.designsystem.component.TopBar
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictPill
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.history.ui.event.HistoryAction
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnBackClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnEntryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnFilterClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnQueryChanged
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnRetryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnSearchSubmitted
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnStart
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Empty
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Error
import pl.handsome.club.feature.history.ui.state.HistoryContentState.InitialLoad
import pl.handsome.club.feature.history.ui.state.HistoryContentState.NoResults
import pl.handsome.club.feature.history.ui.state.HistoryContentState.ShowGroups
import pl.handsome.club.feature.history.ui.state.HistoryFilter
import pl.handsome.club.feature.history.ui.state.HistoryFilterState
import pl.handsome.club.feature.history.ui.state.HistoryGroupState
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
        topBar = {
            Ui.TopBar(
                title = stringResource(R.string.history_title),
                onBackClick = { onAction(OnBackClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (state.showControls) {
                Controls(state.query, state.filters, onAction)
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
                        Text(
                            text = stringResource(R.string.history_empty),
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.text.default,
                        )
                    }

                    NoResults -> Box(
                        centered.padding(horizontal = AppTheme.spacing.l),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.history_no_results),
                            style = AppTheme.typography.body,
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
private fun Controls(
    query: String,
    filters: PersistentList<HistoryFilterState>,
    onAction: (HistoryAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Ui.SearchField(
            value = query,
            onValueChange = { onAction(OnQueryChanged(it)) },
            placeholder = stringResource(R.string.history_search_placeholder),
            onSearch = { onAction(OnSearchSubmitted) },
        )
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            filters.forEach { filter ->
                Ui.FilterChip(
                    text = filter.label.resolve(),
                    selected = filter.selected,
                    onClick = { onAction(OnFilterClicked(filter.filter)) },
                )
            }
        }
    }
}

@Composable
private fun Groups(groups: PersistentList<HistoryGroupState>, onAction: (HistoryAction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        groups.forEachIndexed { index, group ->
            item(key = group.key) {
                Text(
                    text = group.title.resolve().uppercase(),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.text.muted,
                    modifier = if (index == 0) Modifier else Modifier.padding(top = AppTheme.spacing.xs),
                )
            }
            items(group.items, key = { it.id }) { item ->
                Ui.ListRowCard(
                    title = item.name,
                    subtitle = item.time,
                    onClick = { onAction(OnEntryClicked(item.barcode)) },
                    leading = {
                        Ui.IconTile(AppIcons.Bag.imageVector)
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

private fun HistoryItemState.Companion.example(id: Long, name: String, rating: Rating, carbs: Double?) =
    HistoryItemState(
        id = id,
        barcode = "590000000000$id",
        name = UiText.Raw(name),
        time = UiText.Raw("18:42"),
        rating = rating,
        carbs = if (carbs == null) UiText.Res(R.string.history_carbs_missing)
        else UiText.Res(R.string.history_carbs, UiArg.Numeric.Grams(carbs)),
        verdictDescription = UiText.Res(R.string.history_verdict_good_keto),
    )

private val previewFilters = HistoryFilter.entries.map { filter ->
    HistoryFilterState(
        filter = filter,
        label = when (filter) {
            HistoryFilter.ALL -> UiText.Res(R.string.history_filter_all)
            HistoryFilter.GOOD -> UiText.Res(R.string.history_filter_good_keto)
            HistoryFilter.NEUTRAL -> UiText.Res(R.string.history_filter_neutral)
            HistoryFilter.NOT_ADVISED -> UiText.Res(R.string.history_filter_not_advised_keto)
        },
        selected = filter == HistoryFilter.ALL,
    )
}.toPersistentList()

@Preview(showBackground = true)
@Composable
private fun HistoryShowGroupsPreview() {
    val groups = persistentListOf(
        HistoryGroupState(
            key = "today",
            title = UiText.Res(R.string.history_group_today),
            items = persistentListOf(
                HistoryItemState.example(1, "Peanut butter", Rating.Positive, 14.0),
                HistoryItemState.example(2, "Oat cookies", Rating.Neutral, 52.3),
            ),
        ),
        HistoryGroupState(
            key = "yesterday",
            title = UiText.Res(R.string.history_group_yesterday),
            items = persistentListOf(
                HistoryItemState.example(3, "Cola", Rating.Negative, 10.6),
                HistoryItemState.example(4, "Mystery bar", Rating.Unknown, null),
            ),
        ),
    )
    AppTheme {
        ScreenContent(HistoryState("", true, previewFilters, ShowGroups(groups))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryNoResultsPreview() {
    AppTheme {
        ScreenContent(HistoryState("xyz", true, previewFilters, NoResults)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryInitialLoadPreview() {
    AppTheme {
        ScreenContent(HistoryState("", false, previewFilters, InitialLoad)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryEmptyPreview() {
    AppTheme {
        ScreenContent(HistoryState("", false, previewFilters, Empty)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryErrorPreview() {
    AppTheme {
        ScreenContent(HistoryState("", false, previewFilters, Error(AppError.Storage.toUiText()))) {}
    }
}
