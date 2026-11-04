package pl.handsome.club.feature.scanner.ui.component

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
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.component.CardOverlay
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.ScanFrameMode
import pl.handsome.club.core.designsystem.component.SecondaryButton
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.scanner.ui.R
import pl.handsome.club.feature.scanner.ui.ScanScreenPreview
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnScanNextClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import pl.handsome.club.feature.scanner.ui.state.NotFoundCardState

@Composable
internal fun BoxScope.NotFoundOverlay(
    notFound: NotFoundCardState?,
    innerPadding: PaddingValues,
    onAction: (ScanAction) -> Unit,
) {
    BackHandler(enabled = notFound != null) { onAction(OnScanNextClicked) }
    Ui.CardOverlay(
        value = notFound,
        onDismiss = { onAction(OnScanNextClicked) },
        contentPadding = innerPadding,
        modifier = Modifier.matchParentSize(),
    ) {
        NotFoundCard(it, onAction)
    }
}

@Composable
private fun NotFoundCard(notFound: NotFoundCardState, onAction: (ScanAction) -> Unit) {
    val title = stringResource(R.string.scan_product_not_found)
    Column(
        modifier = Modifier
            .widthIn(max = AppTheme.sizes.overlayCardMaxWidth)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .semantics { paneTitle = title }
            .padding(AppTheme.spacing.m)
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .pointerInput(Unit) {}
            .padding(AppTheme.spacing.l)
            .testTag("scanNotFoundCard"),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ui.IconTile(AppIcons.Search.imageVector)
            Text(
                text = title,
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
        }
        Text(
            text = notFound.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        if (notFound.showTypeIt) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
                Ui.SecondaryButton(
                    text = stringResource(R.string.scan_type_it),
                    onClick = { onAction(OnTypeItClicked) },
                    modifier = Modifier.weight(1f),
                )
                Ui.PrimaryButton(
                    text = stringResource(R.string.scan_next),
                    onClick = { onAction(OnScanNextClicked) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Ui.PrimaryButton(
                text = stringResource(R.string.scan_close),
                onClick = { onAction(OnScanNextClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ScanNotFoundPreview() {
    ScanScreenPreview(previewState(isPaused = true, frame = ScanFrameMode.NotFound, notFound = previewNotFound))
}

@Preview
@Composable
private fun ScanNotFoundPermissionPanelPreview() {
    ScanScreenPreview(
        previewPermissionState(
            title = R.string.scan_camera_permission_denied_title,
            message = R.string.scan_camera_permission_denied,
            buttonLabel = R.string.scan_camera_permission_open_settings,
            query = "5900512300320",
            notFound = previewNotFound.copy(showTypeIt = false),
        )
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanNotFoundLandscapePreview() {
    ScanNotFoundPreview()
}
