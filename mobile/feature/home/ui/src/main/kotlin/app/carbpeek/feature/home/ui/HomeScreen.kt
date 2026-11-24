package app.carbpeek.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.Chip
import app.carbpeek.core.designsystem.component.CircleIconButton
import app.carbpeek.core.designsystem.component.EmptyState
import app.carbpeek.core.designsystem.component.HeroCard
import app.carbpeek.core.designsystem.component.IconTile
import app.carbpeek.core.designsystem.component.ListRowCard
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.VerdictPill
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
import app.carbpeek.feature.home.ui.event.HomeAction
import app.carbpeek.feature.home.ui.event.HomeAction.OnAboutClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnEntryClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnProfileClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnRetryClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnSeeAllClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnStart
import app.carbpeek.feature.home.ui.event.HomeAction.OnStartScanningClicked
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation
import app.carbpeek.feature.home.ui.state.HomeItemState
import app.carbpeek.feature.home.ui.state.HomeRecentState
import app.carbpeek.feature.home.ui.state.HomeRecentState.Empty
import app.carbpeek.feature.home.ui.state.HomeRecentState.Error
import app.carbpeek.feature.home.ui.state.HomeRecentState.InitialLoad
import app.carbpeek.feature.home.ui.state.HomeRecentState.ShowItems
import app.carbpeek.feature.home.ui.state.HomeState
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeScreen(onNavigation: (HomeNavigation) -> Unit) {
    val viewModel: HomeViewModel = koinViewModel()

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart)
        onStopOrDispose {}
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is HomeNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: HomeState, onAction: (HomeAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Header(state.profileLabel, onAction)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AppTheme.spacing.l,
                vertical = AppTheme.spacing.l,
            ).withBottomBarClearance(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            item(key = KEY_HERO) {
                Ui.HeroCard(
                    eyebrow = stringResource(R.string.home_hero_eyebrow),
                    title = stringResource(R.string.home_hero_title),
                    body = stringResource(R.string.home_hero_body),
                    actionText = stringResource(R.string.home_hero_action),
                    actionIcon = AppIcons.Scan.imageVector,
                    onClick = { onAction(OnStartScanningClicked) },
                    modifier = Modifier.padding(bottom = AppTheme.spacing.s),
                )
            }
            item(key = KEY_RECENT_TITLE) {
                RecentTitle(showSeeAll = state.showSeeAll, onAction = onAction)
            }
            recentContent(state.recent, onAction)
        }
    }
}

@Composable
private fun Header(profileLabel: UiText, onAction: (HomeAction) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, top = AppTheme.spacing.xxl),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(end = AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.icon.l)
                    .background(AppTheme.colors.accent, AppTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = AppIcons.Logo.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.s),
                )
            }
            Text(
                text = stringResource(R.string.home_app_name),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
                modifier = Modifier.testTag("homeTitle"),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ui.CircleIconButton(
                icon = AppIcons.Info.imageVector,
                contentDescription = UiText.Res(R.string.home_about),
                onClick = { onAction(OnAboutClicked) },
            )
            Ui.Chip(
                text = profileLabel.resolve(),
                icon = AppIcons.Sliders.imageVector,
                onClick = { onAction(OnProfileClicked) },
            )
        }
    }
}

@Composable
private fun RecentTitle(showSeeAll: Boolean, onAction: (HomeAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.sizes.control.m),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_recent_title),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
        )
        if (showSeeAll) {
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = AppTheme.sizes.control.m)
                    .clickable(role = Role.Button) { onAction(OnSeeAllClicked) }
                    .padding(horizontal = AppTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_see_all),
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.accent,
                )
                Icon(
                    imageVector = AppIcons.ChevronRight.imageVector,
                    contentDescription = null,
                    tint = AppTheme.colors.text.accent,
                    modifier = Modifier.size(AppTheme.sizes.icon.s),
                )
            }
        }
    }
}

private fun LazyListScope.recentContent(
    recent: HomeRecentState,
    onAction: (HomeAction) -> Unit,
) {
    when (recent) {
        InitialLoad -> item(key = KEY_STATUS) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.m),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(AppTheme.sizes.icon.l))
            }
        }

        Empty -> item(key = KEY_STATUS) {
            EmptyCard()
        }

        is Error -> item(key = KEY_STATUS) {
            ErrorContent(recent, onAction)
        }

        is ShowItems -> items(recent.items, key = { it.id }) { item ->
            Ui.ListRowCard(
                title = item.name,
                subtitle = item.time,
                subtitleMaxLines = 2,
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

@Composable
private fun EmptyCard() {
    val shape = AppTheme.shapes.card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, shape)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .padding(vertical = AppTheme.spacing.xl, horizontal = AppTheme.spacing.l),
        contentAlignment = Alignment.Center,
    ) {
        Ui.EmptyState(
            icon = AppIcons.Scan,
            title = stringResource(R.string.home_empty_title),
            body = stringResource(R.string.home_empty_body),
            compact = true,
        )
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (HomeAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
            Text(text = stringResource(R.string.home_retry), style = AppTheme.typography.bodyLarge)
        }
    }
}

private const val KEY_HERO = "hero"
private const val KEY_RECENT_TITLE = "recentTitle"
private const val KEY_STATUS = "status"

private fun HomeItemState.Companion.example(
    id: Long,
    name: String,
    icon: AppIcon,
    rating: Rating,
    carbs: Double?,
) = HomeItemState(
    id = id,
    barcode = "590000000000$id",
    name = UiText.Raw(name),
    icon = icon,
    time = UiText.Res(R.string.home_time_yesterday),
    rating = rating,
    carbs = if (carbs == null) UiText.Res(R.string.home_carbs_missing)
    else UiText.Res(R.string.home_carbs, UiArg.Numeric.Grams(carbs)),
    verdictDescription = UiText.Res(R.string.home_verdict_good_keto),
)

private val previewProfile = UiText.Res(R.string.home_preset_keto)

@Preview(showBackground = true)
@Composable
private fun HomeShowItemsPreview() {
    val items = persistentListOf(
        HomeItemState.example(1, "Peanut butter", AppIcons.Jar, Rating.Positive, 14.0),
        HomeItemState.example(2, "Oat cookies", AppIcons.Cookie, Rating.Neutral, 52.3),
        HomeItemState.example(3, "Cola", AppIcons.Can, Rating.Negative, 10.6),
        HomeItemState.example(4, "Mystery bar", AppIcons.Bag, Rating.Unknown, null),
    )
    AppTheme {
        ScreenContent(HomeState(previewProfile, ShowItems(items), showSeeAll = true)) {}
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.15f)
@Composable
private fun HomeShowItemsMediumFontPreview() {
    HomeShowItemsPreview()
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun HomeShowItemsLargeFontPreview() {
    HomeShowItemsPreview()
}

@Preview(showBackground = true)
@Composable
private fun HomeInitialLoadPreview() {
    AppTheme {
        ScreenContent(HomeState(UiText.Res(R.string.home_profile_default), InitialLoad, showSeeAll = false)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    AppTheme {
        ScreenContent(HomeState(previewProfile, Empty, showSeeAll = false)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeErrorPreview() {
    AppTheme {
        ScreenContent(HomeState(previewProfile, Error(AppError.Storage.toUiText()), showSeeAll = false)) {}
    }
}
