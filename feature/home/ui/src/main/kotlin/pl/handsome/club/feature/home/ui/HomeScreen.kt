package pl.handsome.club.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Chip
import pl.handsome.club.core.designsystem.component.CircleIconButton
import pl.handsome.club.core.designsystem.component.HeroCard
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.ListRowCard
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictPill
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.home.ui.event.HomeAction
import pl.handsome.club.feature.home.ui.event.HomeAction.OnAboutClicked
import pl.handsome.club.feature.home.ui.event.HomeAction.OnEntryClicked
import pl.handsome.club.feature.home.ui.event.HomeAction.OnProfileClicked
import pl.handsome.club.feature.home.ui.event.HomeAction.OnRetryClicked
import pl.handsome.club.feature.home.ui.event.HomeAction.OnSeeAllClicked
import pl.handsome.club.feature.home.ui.event.HomeAction.OnStart
import pl.handsome.club.feature.home.ui.event.HomeAction.OnStartScanningClicked
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation
import pl.handsome.club.feature.home.ui.state.HomeItemState
import pl.handsome.club.feature.home.ui.state.HomeRecentState
import pl.handsome.club.feature.home.ui.state.HomeRecentState.Empty
import pl.handsome.club.feature.home.ui.state.HomeRecentState.Error
import pl.handsome.club.feature.home.ui.state.HomeRecentState.InitialLoad
import pl.handsome.club.feature.home.ui.state.HomeRecentState.ShowItems
import pl.handsome.club.feature.home.ui.state.HomeState

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
        topBar = { Header(state.profileLabel, onAction) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.l, vertical = AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            item(key = KEY_HERO) {
                Ui.HeroCard(
                    eyebrow = stringResource(R.string.home_hero_eyebrow),
                    title = stringResource(R.string.home_hero_title),
                    body = stringResource(R.string.home_hero_body),
                    actionText = stringResource(R.string.home_hero_action),
                    actionIcon = AppIcons.Scan,
                    onClick = { onAction(OnStartScanningClicked) },
                    modifier = Modifier.padding(bottom = AppTheme.spacing.s),
                )
            }
            item(key = KEY_RECENT_TITLE) {
                RecentTitle(showSeeAll = state.recent is ShowItems, onAction = onAction)
            }
            recentContent(state.recent, onAction)
        }
    }
}

@Composable
private fun Header(profileLabel: UiText, onAction: (HomeAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, top = AppTheme.spacing.xxl),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.logoTile)
                    .background(AppTheme.colors.accent, AppTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Logo,
                    contentDescription = null,
                    tint = AppTheme.colors.text.inverse,
                    modifier = Modifier.size(AppTheme.sizes.logoIcon),
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
                icon = AppIcons.Info,
                contentDescription = UiText.Res(R.string.home_about),
                onClick = { onAction(OnAboutClicked) },
            )
            Ui.Chip(
                text = profileLabel.resolve(),
                icon = AppIcons.Sliders,
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
            .defaultMinSize(minHeight = AppTheme.sizes.touchTarget),
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
                    .defaultMinSize(minHeight = AppTheme.sizes.touchTarget)
                    .clickable(role = Role.Button) { onAction(OnSeeAllClicked) }
                    .padding(horizontal = AppTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_see_all),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.text.accent,
                )
                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.text.accent,
                    modifier = Modifier.size(AppTheme.sizes.chipIcon),
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
                CircularProgressIndicator(modifier = Modifier.size(AppTheme.sizes.iconL))
            }
        }

        Empty -> item(key = KEY_STATUS) { EmptyCard() }

        is Error -> item(key = KEY_STATUS) { ErrorContent(recent, onAction) }

        is ShowItems -> items(recent.items, key = { it.id }) { item ->
            Ui.ListRowCard(
                title = item.name,
                subtitle = item.time,
                onClick = { onAction(OnEntryClicked(item.barcode)) },
                leading = { Ui.IconTile(AppIcons.Bag) },
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, shape)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = stringResource(R.string.home_empty_body),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
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
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.home_retry), style = AppTheme.typography.body)
        }
    }
}

private const val KEY_HERO = "hero"
private const val KEY_RECENT_TITLE = "recentTitle"
private const val KEY_STATUS = "status"

private fun HomeItemState.Companion.example(id: Long, name: String, rating: Rating, carbs: Double?) = HomeItemState(
    id = id,
    barcode = "590000000000$id",
    name = UiText.Raw(name),
    time = UiText.Res(R.string.home_time_yesterday),
    rating = rating,
    carbs = if (carbs == null) UiText.Res(R.string.home_carbs_missing)
    else UiText.Res(R.string.home_carbs, UiArg.Numeric.Grams(carbs)),
    verdictDescription = UiText.Res(R.string.home_verdict_good_keto),
)

private val previewProfile = UiText.Res(
    R.string.home_profile_label,
    UiArg.Nested(UiText.Res(R.string.home_mode_keto)),
    UiArg.Numeric.Count(40),
)

@Preview(showBackground = true)
@Composable
private fun HomeShowItemsPreview() {
    val items = persistentListOf(
        HomeItemState.example(1, "Peanut butter", Rating.Positive, 14.0),
        HomeItemState.example(2, "Oat cookies", Rating.Neutral, 52.3),
        HomeItemState.example(3, "Cola", Rating.Negative, 10.6),
        HomeItemState.example(4, "Mystery bar", Rating.Unknown, null),
    )
    AppTheme {
        ScreenContent(HomeState(previewProfile, ShowItems(items))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeInitialLoadPreview() {
    AppTheme {
        ScreenContent(HomeState(UiText.Res(R.string.home_profile_default), InitialLoad)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    AppTheme {
        ScreenContent(HomeState(previewProfile, Empty)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeErrorPreview() {
    AppTheme {
        ScreenContent(HomeState(previewProfile, Error(AppError.Storage.toUiText()))) {}
    }
}
