package pl.handsome.club.feature.favourites.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import pl.handsome.club.core.designsystem.icon.icon
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.label
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnGroupClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnItemClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnQueryChanged
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnRetryClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnSearchSubmitted
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import pl.handsome.club.feature.favourites.ui.state.FavouriteGroupState
import pl.handsome.club.feature.favourites.ui.state.FavouriteItemState
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Empty
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Error
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.NoResults
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.ShowGroups
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
private fun Groups(groups: PersistentList<FavouriteGroupState>, onAction: (FavouritesAction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        groups.forEachIndexed { index, group ->
            item(key = "group_${group.category.name}") {
                GroupHeader(
                    group = group,
                    onAction = onAction,
                    modifier = Modifier
                        .animateItem()
                        .then(if (index == 0) Modifier else Modifier.padding(top = AppTheme.spacing.s)),
                )
            }
            if (group.expanded) {
                items(group.items, key = { it.barcode }) { item ->
                    Item(item, onAction, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: FavouriteGroupState, onAction: (FavouritesAction) -> Unit, modifier: Modifier) {
    val toggleState = stringResource(
        if (group.expanded) {
            R.string.favourites_group_expanded
        } else {
            R.string.favourites_group_collapsed
        },
    )
    val toggle = if (group.canToggle) {
        Modifier
            .clip(AppTheme.shapes.medium)
            .clickable(role = Role.Button) { onAction(OnGroupClicked(group.category)) }
            .semantics { stateDescription = toggleState }
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppTheme.sizes.touchTarget)
            .then(toggle)
            .padding(horizontal = AppTheme.spacing.xxs)
            .testTag("favouritesGroup_${group.category.name}"),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = group.icon.imageVector,
            contentDescription = null,
            tint = AppTheme.colors.text.subtle,
            modifier = Modifier.size(AppTheme.sizes.iconS),
        )
        Text(
            text = group.label.resolve(),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.default,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = group.count.resolve(),
            style = AppTheme.typography.label,
            color = AppTheme.colors.text.muted,
            modifier = Modifier
                .background(AppTheme.colors.background.subtle, AppTheme.shapes.pill)
                .padding(horizontal = AppTheme.spacing.xs, vertical = AppTheme.spacing.xxs),
        )
        if (group.canToggle) {
            Icon(
                imageVector = AppIcons.ChevronRight.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.subtle,
                modifier = Modifier
                    .size(AppTheme.sizes.iconS)
                    .rotate(if (group.expanded) 90f else 0f),
            )
        }
    }
}

@Composable
private fun Item(item: FavouriteItemState, onAction: (FavouritesAction) -> Unit, modifier: Modifier) {
    Ui.ListRowCard(
        title = item.name,
        subtitle = item.brand,
        onClick = { onAction(OnItemClicked(item.barcode)) },
        modifier = modifier,
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

private fun previewGroup(
    category: ProductCategory,
    expanded: Boolean,
    canToggle: Boolean,
    items: PersistentList<FavouriteItemState>,
) = FavouriteGroupState(
    category = category,
    icon = category.icon,
    label = category.label,
    count = UiText.Res(R.string.favourites_group_count, UiArg.Numeric.Count(items.size.toLong())),
    expanded = expanded,
    canToggle = canToggle,
    items = items,
)

private val previewSpreads = persistentListOf(
    FavouriteItemState.example("5900000000001", "Peanut butter", AppIcons.Jar, "Nuts & Co", Rating.Positive, 14.0),
    FavouriteItemState.example("5900000000005", "Hazelnut spread", AppIcons.Jar, null, Rating.Negative, 57.5),
)

private val previewBiscuits = persistentListOf(
    FavouriteItemState.example("5900000000002", "Oat cookies", AppIcons.Cookie, null, Rating.Neutral, 52.3),
)

private val previewDrinks = persistentListOf(
    FavouriteItemState.example("5900000000003", "Cola", AppIcons.Can, "Fizz", Rating.Negative, 10.6),
)

private val previewOther = persistentListOf(
    FavouriteItemState.example("5900000000004", "Mystery bar", AppIcons.Bag, null, Rating.Unknown, null),
)

@Preview(showBackground = true)
@Composable
private fun FavouritesShowGroupsPreview() {
    val groups = persistentListOf(
        previewGroup(ProductCategory.SPREADS, expanded = true, canToggle = true, items = previewSpreads),
        previewGroup(ProductCategory.DRINKS, expanded = false, canToggle = true, items = previewDrinks),
        previewGroup(ProductCategory.BISCUITS, expanded = true, canToggle = true, items = previewBiscuits),
        previewGroup(ProductCategory.OTHER, expanded = true, canToggle = true, items = previewOther),
    )
    AppTheme {
        ScreenContent(FavouritesState(query = "", showSearch = true, content = ShowGroups(groups))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesSearchGroupsPreview() {
    val groups = persistentListOf(
        previewGroup(ProductCategory.SPREADS, expanded = true, canToggle = false, items = previewSpreads),
    )
    AppTheme {
        ScreenContent(FavouritesState(query = "nut", showSearch = true, content = ShowGroups(groups))) {}
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
