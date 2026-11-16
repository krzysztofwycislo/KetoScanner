package app.carbpeek.feature.scanner.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.CardOverlay
import app.carbpeek.core.designsystem.component.CoachMark
import app.carbpeek.core.designsystem.component.IconTile
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.ScanFrameMode
import app.carbpeek.core.designsystem.component.SecondaryButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.VerdictBanner
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.R
import app.carbpeek.feature.scanner.ui.ScanScreenPreview
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnDetailsClicked
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnScanNextClicked
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnVerdictTipDismissed
import app.carbpeek.feature.scanner.ui.state.FoundCardState

@Composable
internal fun BoxScope.FoundOverlay(
    found: FoundCardState?,
    innerPadding: PaddingValues,
    onAction: (ScanAction) -> Unit,
) {
    BackHandler(enabled = found != null) { onAction(OnScanNextClicked) }
    Ui.CardOverlay(
        value = found,
        onDismiss = { onAction(OnScanNextClicked) },
        contentPadding = innerPadding,
        modifier = Modifier.matchParentSize(),
    ) { card ->
        Column(
            modifier = Modifier
                .widthIn(max = AppTheme.sizes.overlayCardMaxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Ui.CoachMark(
                visible = card.showVerdictTip,
                title = stringResource(R.string.scan_verdict_tip_title),
                body = stringResource(R.string.scan_verdict_tip_body),
                actionLabel = stringResource(R.string.scan_verdict_tip_action),
                onAction = { onAction(OnVerdictTipDismissed) },
            )
            FoundCard(card, onAction)
        }
    }
}

@Composable
private fun FoundCard(found: FoundCardState, onAction: (ScanAction) -> Unit) {
    val title = found.title.resolve()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { paneTitle = title }
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .pointerInput(Unit) {}
            .padding(AppTheme.spacing.l)
            .testTag("scanFoundCard"),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ui.IconTile(found.icon.imageVector)
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                Text(
                    text = found.name.resolve(),
                    style = AppTheme.typography.section,
                    color = AppTheme.colors.text.default,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                found.brand?.let {
                    Text(
                        text = it.resolve(),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.text.muted,
                    )
                }
            }
        }
        Ui.VerdictBanner(
            rating = found.rating,
            title = found.title,
            details = listOf(found.detail),
            highlighted = found.showVerdictTip,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Ui.SecondaryButton(
                text = stringResource(R.string.scan_next),
                onClick = { onAction(OnScanNextClicked) },
                modifier = Modifier.weight(1f),
            )
            Ui.PrimaryButton(
                text = stringResource(R.string.scan_details),
                onClick = { onAction(OnDetailsClicked) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun ScanFoundRatedPreview() {
    ScanScreenPreview(previewState(isPaused = true, frame = ScanFrameMode.Found, found = previewFound))
}

@Preview
@Composable
private fun ScanFoundInsufficientDataPreview() {
    ScanScreenPreview(
        previewState(
            isPaused = true,
            frame = ScanFrameMode.Found,
            found = FoundCardState(
                name = UiText.Res(R.string.scan_found_unnamed),
                icon = AppIcons.Bag,
                brand = null,
                rating = Rating.Unknown,
                title = UiText.Res(R.string.scan_verdict_insufficient_data),
                detail = UiText.Res(R.string.scan_found_insufficient_detail),
                showVerdictTip = false,
            ),
        )
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanFoundLandscapePreview() {
    ScanFoundRatedPreview()
}

@Preview
@Composable
private fun ScanFoundVerdictTipPreview() {
    ScanScreenPreview(
        previewState(isPaused = true, frame = ScanFrameMode.Found, found = previewFound.copy(showVerdictTip = true)),
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanFoundVerdictTipLandscapePreview() {
    ScanFoundVerdictTipPreview()
}
