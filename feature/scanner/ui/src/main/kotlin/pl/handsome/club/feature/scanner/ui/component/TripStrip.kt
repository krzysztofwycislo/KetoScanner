package pl.handsome.club.feature.scanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.designsystem.component.FadeVisibility
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.cameraColor
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.scanner.ui.R
import pl.handsome.club.feature.scanner.ui.ScanScreenPreview
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTripItemClicked
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype
import pl.handsome.club.feature.scanner.ui.state.TripItemState

@Composable
internal fun BoxScope.TripOverlay(
    trip: ImmutableList<TripItemState>,
    vertical: Boolean,
    onAction: (ScanAction) -> Unit,
    stripModifier: Modifier,
) {
    Ui.FadeVisibility(
        value = trip.takeIf { it.isNotEmpty() },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        TripStrip(
            trip = it,
            vertical = vertical,
            onAction = onAction,
            modifier = stripModifier,
        )
    }
}

@Composable
private fun TripStrip(
    trip: ImmutableList<TripItemState>,
    vertical: Boolean,
    onAction: (ScanAction) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scanTripStrip"),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.scan_trip_title).uppercase(),
            style = AppTheme.typography.label,
            color = AppTheme.colors.text.onHero,
        )
        if (vertical) {
            trip.forEach { item ->
                TripChip(item, onAction, Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                trip.forEach { item ->
                    TripChip(item, onAction, Modifier.weight(1f))
                }
                repeat(ScanStatePrototype.TRIP_LIMIT - trip.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TripChip(item: TripItemState, onAction: (ScanAction) -> Unit, modifier: Modifier) {
    val description = item.description.resolve()
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.control)
            .background(AppTheme.colors.camera.chip)
            .clickable(role = Role.Button) { onAction(OnTripItemClicked(item.barcode)) }
            .padding(AppTheme.spacing.xs)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon.imageVector,
            contentDescription = null,
            tint = AppTheme.colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
        Column {
            Text(
                text = item.name.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.text.inverse,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.carbs.resolve(),
                style = AppTheme.typography.caption,
                fontWeight = FontWeight.ExtraBold,
                color = item.rating.cameraColor(),
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

@Preview
@Composable
private fun ScanTripPreview() {
    ScanScreenPreview(previewState(trip = previewTrip))
}

@Preview
@Composable
private fun ScanTripSingleItemPreview() {
    ScanScreenPreview(
        previewState(
            trip = persistentListOf(
                previewTripItem(
                    name = "Nutella hazelnut spread with cocoa",
                    icon = AppIcons.Jar,
                    rating = Rating.Negative,
                    carbs = 57.5,
                    verdict = R.string.scan_verdict_not_advised_keto,
                ),
            ),
        )
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanTripLandscapePreview() {
    ScanTripPreview()
}

@Preview(widthDp = 640, heightDp = 300)
@Composable
private fun ScanTripSmallLandscapePreview() {
    ScanTripPreview()
}
