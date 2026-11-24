package app.carbpeek.feature.scanner.ui.component

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
import app.carbpeek.core.designsystem.component.FadeVisibility
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.cameraColor
import app.carbpeek.core.designsystem.component.isTextSpaceTight
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.R
import app.carbpeek.feature.scanner.ui.ScanScreenPreview
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnTripItemClicked
import app.carbpeek.feature.scanner.ui.state.ScanStatePrototype
import app.carbpeek.feature.scanner.ui.state.TripItemState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text.onHero,
            modifier = Modifier
                .background(color = AppTheme.colors.camera.hint, shape = AppTheme.shapes.pill)
                .padding(horizontal = AppTheme.spacing.l / 2, vertical = AppTheme.spacing.xxs),
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
        if (!isTextSpaceTight()) {
            Icon(
                imageVector = item.icon.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.inverse,
                modifier = Modifier.size(AppTheme.sizes.icon.s),
            )
        }
        Column {
            Text(
                text = item.name.resolve(),
                style = AppTheme.typography.micro,
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
private fun ScanTripNoDietPreview() {
    ScanScreenPreview(previewState(trip = previewTripNoDiet))
}

@Preview
@Composable
private fun TripStripBrightPreview() {
    AppTheme {
        TripStrip(
            trip = previewTrip,
            vertical = false,
            onAction = {},
            modifier = Modifier
                .background(AppTheme.colors.background.surface)
                .padding(AppTheme.spacing.m),
        )
    }
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

@Preview(widthDp = 360, fontScale = 1.5f)
@Composable
private fun ScanTripLargeFontPreview() {
    ScanTripPreview()
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
