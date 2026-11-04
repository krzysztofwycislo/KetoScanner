package pl.handsome.club.feature.favourites.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.PersistentList
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.ListRowCard
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictPill
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnGroupClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnItemClicked
import pl.handsome.club.feature.favourites.ui.state.FavouriteGroupState
import pl.handsome.club.feature.favourites.ui.state.FavouriteItemState

@Composable
internal fun Groups(groups: PersistentList<FavouriteGroupState>, onAction: (FavouritesAction) -> Unit) {
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
    val toggleState = group.toggleState.resolve()
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
            .heightIn(min = AppTheme.sizes.control.m)
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
            modifier = Modifier.size(AppTheme.sizes.icon.m),
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
                    .size(AppTheme.sizes.icon.m)
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

@Preview(showBackground = true)
@Composable
private fun FavouriteGroupsPreview() {
    AppTheme {
        Groups(previewGroups) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouriteGroupsSearchPreview() {
    AppTheme {
        Groups(previewSearchGroups) {}
    }
}
