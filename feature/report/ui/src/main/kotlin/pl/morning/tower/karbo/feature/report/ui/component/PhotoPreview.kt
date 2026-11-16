package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.ProductImage
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.SecondaryButton
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.camera.LightStatusBarIconsEffect
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnPreviewCancelled
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnRetakeClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnUsePhotoClicked
import pl.morning.tower.karbo.feature.report.ui.state.PhotoPreviewState

@Composable
internal fun PhotoPreview(
    state: PhotoPreviewState,
    onAction: (IssueReportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Ui.Screen(
        modifier = modifier,
        containerColor = AppTheme.colors.camera.background,
    ) { innerPadding ->
        LightStatusBarIconsEffect()
        Box(modifier = Modifier.fillMaxSize()) {
            Ui.ProductImage(
                url = state.imageUri,
                contentDescription = UiText.Res(R.string.report_photo_preview),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppTheme.spacing.xs),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                PreviewTopRow(
                    counter = state.counter,
                    onCancel = { onAction(OnPreviewCancelled) },
                )
                PreviewCard(state, onAction)
            }
        }
    }
}

@Composable
private fun PreviewTopRow(counter: UiText, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CancelButton(onClick = onCancel)
        Box(
            modifier = Modifier
                .height(AppTheme.sizes.icon.xl)
                .clip(AppTheme.shapes.pill)
                .background(AppTheme.colors.camera.chip)
                .padding(horizontal = AppTheme.spacing.s),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = counter.resolve(),
                style = AppTheme.typography.bodyStrong,
                color = AppTheme.colors.text.inverse,
            )
        }
        Spacer(modifier = Modifier.size(AppTheme.sizes.control.m))
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.circle)
            .background(AppTheme.colors.camera.control)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Close.imageVector,
            contentDescription = stringResource(R.string.report_cancel_photo),
            tint = AppTheme.colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
    }
}

@Composable
private fun PreviewCard(state: PhotoPreviewState, onAction: (IssueReportAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.large)
            .background(AppTheme.colors.background.surface)
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Text(
            text = state.title.resolve(),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = state.body.resolve(),
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

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoPreviewPreview() {
    AppTheme {
        PhotoPreview(state = PhotoPreviewState.example(), onAction = {})
    }
}
