package pl.morning.tower.karbo.feature.report.ui.capture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.ProductImage
import pl.morning.tower.karbo.core.designsystem.component.SecondaryButton
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnRetakeClicked
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnUsePhotoClicked
import pl.morning.tower.karbo.feature.report.ui.capture.state.CaptureStepState

@Composable
internal fun PreviewStep(
    step: CaptureStepState.Preview,
    innerPadding: PaddingValues,
    onAction: (PhotoCaptureAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Ui.ProductImage(
            url = step.imageUri,
            contentDescription = UiText.Res(R.string.report_photo_preview),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppTheme.spacing.xs),
            verticalArrangement = Arrangement.Bottom,
        ) {
            PreviewCard(step, onAction)
        }
    }
}

@Composable
private fun PreviewCard(step: CaptureStepState.Preview, onAction: (PhotoCaptureAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.large)
            .background(AppTheme.colors.background.surface)
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Text(
            text = step.title.resolve(),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = step.body.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Ui.SecondaryButton(
                text = stringResource(R.string.report_retake),
                onClick = { onAction(OnRetakeClicked) },
                modifier = Modifier.weight(1f),
            )
            Ui.PrimaryButton(
                text = stringResource(R.string.report_use_photo),
                onClick = { onAction(OnUsePhotoClicked) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
