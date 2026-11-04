package pl.morning.tower.karbo.feature.scanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.TopBar
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.scanner.ui.R
import pl.morning.tower.karbo.feature.scanner.ui.ScanScreenPreview
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraState
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanState

@Composable
internal fun PermissionContent(state: ScanState, camera: CameraState.PermissionPanel, onAction: (ScanAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Ui.TopBar(stringResource(R.string.scan_title))
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                val contentModifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, bottom = AppTheme.spacing.m)
                if (maxWidth > maxHeight) {
                    Row(
                        modifier = contentModifier,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
                    ) {
                        PermissionCard(
                            camera = camera,
                            onAction = onAction,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        BarcodeEntryCard(state.query, state.isLookUpEnabled, onAction, Modifier.weight(1f))
                    }
                } else {
                    Column(
                        modifier = contentModifier,
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
                    ) {
                        PermissionCard(camera, onAction, Modifier.weight(1f))
                        BarcodeEntryCard(state.query, state.isLookUpEnabled, onAction, Modifier.fillMaxWidth())
                    }
                }
            }
            FoundOverlay(state.found, innerPadding, onAction)
            NotFoundOverlay(state.notFound, innerPadding, onAction)
        }
    }
}

@Composable
private fun BarcodeEntryCard(
    query: String,
    isLookUpEnabled: Boolean,
    onAction: (ScanAction) -> Unit,
    modifier: Modifier,
) {
    PanelCard(
        modifier = modifier,
        contentPadding = AppTheme.spacing.m,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.scan_type_title),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        BarcodeEntry(query = query, isLookUpEnabled = isLookUpEnabled, onAction = onAction)
    }
}

@Composable
private fun PermissionCard(camera: CameraState.PermissionPanel, onAction: (ScanAction) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, AppTheme.shapes.large)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.large)
            .testTag("cameraPermissionPanel"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.iconDisc)
                    .background(AppTheme.colors.background.subtle, AppTheme.shapes.circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.CameraOff.imageVector,
                    contentDescription = null,
                    tint = AppTheme.colors.text.muted,
                    modifier = Modifier.size(AppTheme.sizes.icon.xl),
                )
            }
            Text(
                text = camera.title.resolve(),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.text.default,
                textAlign = TextAlign.Center,
            )
            Text(
                text = camera.message.resolve(),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.muted,
                textAlign = TextAlign.Center,
            )
            Ui.PrimaryButton(text = camera.buttonLabel.resolve(), onClick = { onAction(OnPermissionButtonClicked) })
        }
    }
}

@Composable
private fun PanelCard(
    modifier: Modifier,
    contentPadding: Dp,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.background.surface, AppTheme.shapes.large)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.large)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

@Preview
@Composable
private fun ScanRationalePreview() {
    ScanScreenPreview(
        previewPermissionState(
            title = R.string.scan_camera_permission_rationale_title,
            message = R.string.scan_camera_permission_rationale,
            buttonLabel = R.string.scan_camera_permission_allow,
        )
    )
}

@Preview
@Composable
private fun ScanDeniedPreview() {
    ScanScreenPreview(
        previewPermissionState(
            title = R.string.scan_camera_permission_denied_title,
            message = R.string.scan_camera_permission_denied,
            buttonLabel = R.string.scan_camera_permission_open_settings,
        )
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanDeniedLandscapePreview() {
    ScanDeniedPreview()
}
