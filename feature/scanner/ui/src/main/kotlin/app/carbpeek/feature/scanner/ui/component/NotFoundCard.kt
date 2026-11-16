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
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.CardOverlay
import app.carbpeek.core.designsystem.component.IconTile
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.ScanFrameMode
import app.carbpeek.core.designsystem.component.SecondaryButton
import app.carbpeek.core.designsystem.component.TextButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.R
import app.carbpeek.feature.scanner.ui.ScanScreenPreview
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnAddItClicked
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnScanNextClicked
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import app.carbpeek.feature.scanner.ui.state.NotFoundCardState

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
            Column {
                Text(
                    text = title,
                    style = AppTheme.typography.section,
                    color = AppTheme.colors.text.default,
                )
                Text(
                    text = notFound.barcode.resolve(),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text.muted,
                )
            }
        }
        Text(
            text = notFound.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (notFound.showTypeIt) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
                ) {
                    Ui.SecondaryButton(
                        text = stringResource(R.string.scan_type_it),
                        onClick = { onAction(OnTypeItClicked) },
                        modifier = Modifier.weight(1f),
                    )
                    AddItButton(
                        onClick = { onAction(OnAddItClicked) },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                AddItButton(
                    onClick = { onAction(OnAddItClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Ui.TextButton(
                text = notFound.dismissLabel.resolve(),
                onClick = { onAction(OnScanNextClicked) },
            )
        }
    }
}

@Composable
private fun AddItButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Ui.PrimaryButton(
        text = stringResource(R.string.scan_add_it),
        onClick = onClick,
        modifier = modifier,
        icon = AppIcons.Camera,
    )
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
            notFound = previewNotFound.copy(showTypeIt = false, dismissLabel = previewResources.close()),
        )
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanNotFoundLandscapePreview() {
    ScanNotFoundPreview()
}
