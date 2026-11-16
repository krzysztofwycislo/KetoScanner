package app.carbpeek.feature.report.ui.capture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.SecondaryButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnGalleryClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPermissionButtonClicked
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState

@Composable
internal fun BlockedStep(
    step: CaptureStepState.Blocked,
    innerPadding: PaddingValues,
    onAction: (PhotoCaptureAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        TopRowSpacer()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(AppTheme.spacing.m),
            contentAlignment = Alignment.Center,
        ) {
            BlockedCard(step, onAction)
        }
    }
}

@Composable
private fun BlockedCard(step: CaptureStepState.Blocked, onAction: (PhotoCaptureAction) -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = AppTheme.sizes.overlayCardMaxWidth)
            .fillMaxWidth()
            .background(color = AppTheme.colors.background.surface, shape = AppTheme.shapes.large)
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.iconDiscCompact)
                .background(color = AppTheme.colors.background.subtle, shape = AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.CameraOff.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.muted,
                modifier = Modifier.size(AppTheme.sizes.icon.m),
            )
        }
        Text(
            text = step.title.resolve(),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        Text(
            text = step.body.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
        Ui.PrimaryButton(
            text = step.primaryLabel.resolve(),
            onClick = { onAction(OnPermissionButtonClicked) },
            modifier = Modifier.fillMaxWidth(),
        )
        Ui.SecondaryButton(
            text = stringResource(R.string.report_choose_from_gallery),
            onClick = { onAction(OnGalleryClicked) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
