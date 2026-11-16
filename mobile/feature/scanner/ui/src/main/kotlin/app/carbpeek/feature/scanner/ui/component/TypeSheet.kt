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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.CircleIconButton
import app.carbpeek.core.designsystem.component.ScanFrameMode
import app.carbpeek.core.designsystem.component.SheetOverlay
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.R
import app.carbpeek.feature.scanner.ui.ScanScreenPreview
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnTypeSheetDismissed

@Composable
internal fun BoxScope.TypeSheet(
    query: String,
    isLookUpEnabled: Boolean,
    visible: Boolean,
    innerPadding: PaddingValues,
    onAction: (ScanAction) -> Unit,
) {
    Ui.SheetOverlay(
        visible = visible,
        onDismiss = { onAction(OnTypeSheetDismissed) },
        contentPadding = innerPadding,
        modifier = Modifier.matchParentSize(),
    ) {
        TypeCard(query, isLookUpEnabled, visible, onAction)
    }
}

@Composable
private fun TypeCard(query: String, isLookUpEnabled: Boolean, visible: Boolean, onAction: (ScanAction) -> Unit) {
    BackHandler(enabled = visible) { onAction(OnTypeSheetDismissed) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Column(
        modifier = Modifier
            .widthIn(max = AppTheme.sizes.overlayCardMaxWidth)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.m)
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .pointerInput(Unit) {}
            .padding(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.scan_type_title),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
            Ui.CircleIconButton(
                icon = AppIcons.Cross.imageVector,
                contentDescription = UiText.Res(R.string.scan_close),
                onClick = { onAction(OnTypeSheetDismissed) },
            )
        }
        Text(
            text = stringResource(R.string.scan_type_helper),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        BarcodeEntry(
            query = query,
            isLookUpEnabled = isLookUpEnabled,
            onAction = onAction,
            inputModifier = Modifier.focusRequester(focusRequester),
        )
    }
}

@Preview
@Composable
private fun ScanTypeSheetPreview() {
    ScanScreenPreview(
        previewState(
            query = "5900512300320",
            isPaused = true,
            isTypeSheetOpen = true,
            frame = ScanFrameMode.Idle,
        ),
    )
}

@Preview
@Composable
private fun ScanSearchingPreview() {
    ScanScreenPreview(
        previewState(
            query = "5900512300320",
            isLookUpEnabled = false,
            isPaused = true,
            isTypeSheetOpen = true,
            frame = ScanFrameMode.Searching,
            hint = previewResources.searchingHint(),
        ),
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanTypeSheetLandscapePreview() {
    ScanTypeSheetPreview()
}
