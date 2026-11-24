package app.carbpeek.feature.history.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.ContentCrossfade
import app.carbpeek.core.designsystem.component.EmptyState
import app.carbpeek.core.designsystem.component.FilterChip
import app.carbpeek.core.designsystem.component.IconTile
import app.carbpeek.core.designsystem.component.ListRowCard
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.SearchField
import app.carbpeek.core.designsystem.component.TopBar
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.VerdictPill
import app.carbpeek.core.designsystem.component.bottomBarClearance
import app.carbpeek.core.designsystem.component.withBottomBarClearance
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.history.ui.event.HistoryAction
import app.carbpeek.feature.history.ui.event.HistoryAction.OnBackClicked
import app.carbpeek.feature.history.ui.event.HistoryAction.OnEntryClicked
import app.carbpeek.feature.history.ui.event.HistoryAction.OnFilterClicked
import app.carbpeek.feature.history.ui.event.HistoryAction.OnQueryChanged
import app.carbpeek.feature.history.ui.event.HistoryAction.OnRetryClicked
import app.carbpeek.feature.history.ui.event.HistoryAction.OnSearchSubmitted
import app.carbpeek.feature.history.ui.event.HistoryAction.OnStart
import app.carbpeek.feature.history.ui.event.HistoryAction.OnStartScanningClicked
import app.carbpeek.feature.history.ui.event.HistoryEvent.HistoryNavigation
import app.carbpeek.feature.history.ui.state.HistoryContentState.Empty
import app.carbpeek.feature.history.ui.state.HistoryContentState.Error
import app.carbpeek.feature.history.ui.state.HistoryContentState.InitialLoad
import app.carbpeek.feature.history.ui.state.HistoryContentState.NoResults
import app.carbpeek.feature.history.ui.state.HistoryContentState.ShowGroups
import app.carbpeek.feature.history.ui.state.HistoryFilter
import app.carbpeek.feature.history.ui.state.HistoryFilterState
import app.carbpeek.feature.history.ui.state.HistoryGroupState
import app.carbpeek.feature.history.ui.state.HistoryItemState
import app.carbpeek.feature.history.ui.state.HistoryState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

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
                val centered = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomBarClearance())
                when (content) {
                    InitialLoad -> Box(centered, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    Empty -> Box(centered, contentAlignment = Alignment.Center) {
                        Ui.EmptyState(
                            icon = AppIcons.Clock,
                            title = stringResource(R.string.history_empty_title),
                            body = stringResource(R.string.history_empty_body),
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.xxl),
                            actionText = stringResource(R.string.history_start_scanning),
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
private fun Controls(
    query: String,
    filters: PersistentList<HistoryFilterState>,
    onAction: (HistoryAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Ui.SearchField(
            value = query,
            onValueChange = { onAction(OnQueryChanged(it)) },
            placeholder = stringResource(R.string.history_search_placeholder),
            onSearch = { onAction(OnSearchSubmitted) },
            modifier = Modifier.padding(horizontal = AppTheme.spacing.l),
        )
        if (filters.isNotEmpty()) {
            FilterRow(filters = filters, onAction = onAction)
        }
    }
}

@Composable
private fun FilterRow(filters: PersistentList<HistoryFilterState>, onAction: (HistoryAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup()
            .padding(horizontal = AppTheme.spacing.l)
            .testTag("historyFilterRow"),
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

@Composable
private fun Groups(groups: PersistentList<HistoryGroupState>, onAction: (HistoryAction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.spacing.l).withBottomBarClearance(),
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
}

@Composable
private fun ErrorContent(state: Error, onAction: (HistoryAction) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = AppTheme.spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.history_retry), style = AppTheme.typography.bodyLarge)
        }
    }
}

private fun HistoryItemState.Companion.example(
    id: Long,
    name: String,
    icon: AppIcon,
    rating: Rating?,
    carbs: Double?,
) =
    HistoryItemState(
        id = id,
        barcode = "590000000000$id",
        name = UiText.Raw(name),
        icon = icon,
        time = UiText.Raw("18:42"),
        rating = rating,
        carbs = when {
            carbs == null -> UiText.Res(R.string.history_carbs_missing)
            rating == null -> UiText.Res(R.string.history_carbs_named, UiArg.Numeric.Grams(carbs))
            else -> UiText.Res(R.string.history_carbs, UiArg.Numeric.Grams(carbs))
        },
        verdictDescription = UiText.Res(
            if (rating == null) R.string.history_verdict_unknown else R.string.history_verdict_good_keto,
        ),
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
                HistoryItemState.example(1, "Peanut butter", AppIcons.Jar, Rating.Positive, 14.0),
                HistoryItemState.example(2, "Oat cookies", AppIcons.Cookie, Rating.Neutral, 52.3),
            ),
        ),
        HistoryGroupState(
            key = "yesterday",
            title = UiText.Res(R.string.history_group_yesterday),
            items = persistentListOf(
                HistoryItemState.example(3, "Cola", AppIcons.Can, Rating.Negative, 10.6),
                HistoryItemState.example(4, "Mystery bar", AppIcons.Bag, Rating.Unknown, null),
            ),
        ),
    )
    AppTheme {
        ScreenContent(HistoryState("", true, previewFilters, ShowGroups(groups))) {}
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun HistoryShowGroupsLargeFontPreview() {
    HistoryShowGroupsPreview()
}

@Preview(showBackground = true)
@Composable
private fun HistoryNoResultsPreview() {
    AppTheme {
        ScreenContent(
            state = HistoryState(
                query = "xyz",
                showControls = true,
                filters = previewFilters,
                content = NoResults(UiText.Res(R.string.history_no_results)),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryNoDietPreview() {
    val groups = persistentListOf(
        HistoryGroupState(
            key = "today",
            title = UiText.Res(R.string.history_group_today),
            items = persistentListOf(
                HistoryItemState.example(
                    id = 1,
                    name = "Peanut butter",
                    icon = AppIcons.Jar,
                    rating = null,
                    carbs = 14.0,
                ),
                HistoryItemState.example(
                    id = 2,
                    name = "Hazelnut spread",
                    icon = AppIcons.Jar,
                    rating = null,
                    carbs = 57.5,
                ),
                HistoryItemState.example(
                    id = 3,
                    name = "Mystery bar",
                    icon = AppIcons.Bag,
                    rating = null,
                    carbs = null,
                ),
            ),
        ),
    )
    AppTheme {
        ScreenContent(
            state = HistoryState(
                query = "",
                showControls = true,
                filters = persistentListOf(),
                content = ShowGroups(groups),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun HistoryNoDietLargeFontPreview() {
    HistoryNoDietPreview()
}

@Preview(showBackground = true)
@Composable
private fun HistoryNoDietNoResultsPreview() {
    AppTheme {
        ScreenContent(
            state = HistoryState(
                query = "xyz",
                showControls = true,
                filters = persistentListOf(),
                content = NoResults(UiText.Res(R.string.history_no_results_no_filters)),
            ),
            onAction = {},
        )
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
