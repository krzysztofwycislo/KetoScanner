package app.carbpeek.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.ProductImage
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.event.IssueReportAction
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnAddPhotoClicked
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnRemovePhotoClicked
import app.carbpeek.feature.report.ui.state.PhotoThumbnailState
import app.carbpeek.feature.report.ui.state.PhotosState

@Composable
internal fun PhotosRow(
    state: PhotosState,
    isEditable: Boolean,
    onAction: (IssueReportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        OptionalLabel(
            text = stringResource(R.string.report_photos),
            suffix = stringResource(R.string.report_photos_limit),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            state.items.forEach { item ->
                PhotoThumbnail(
                    state = item,
                    isEditable = isEditable,
                    onRemove = { onAction(OnRemovePhotoClicked(item.uri)) },
                )
            }
            if (state.showAddTile) {
                AddPhotoTile(
                    isEditable = isEditable,
                    onClick = { onAction(OnAddPhotoClicked) },
                )
            }
        }
        Text(
            text = stringResource(R.string.report_photos_hint),
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.text.muted,
        )
    }
}

@Composable
private fun PhotoThumbnail(state: PhotoThumbnailState, isEditable: Boolean, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(AppTheme.sizes.control.xl)) {
        Ui.ProductImage(
            url = state.uri,
            contentDescription = state.contentDescription,
            modifier = Modifier
                .matchParentSize()
                .clip(AppTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .minimumInteractiveComponentSize()
                .size(AppTheme.sizes.control.xs)
                .clip(AppTheme.shapes.circle)
                .background(AppTheme.colors.camera.chip)
                .clickable(
                    enabled = isEditable,
                    role = Role.Button,
                    onClick = onRemove,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Close.imageVector,
                contentDescription = state.removeLabel.resolve(),
                tint = AppTheme.colors.text.inverse,
                modifier = Modifier.size(AppTheme.sizes.icon.xs),
            )
        }
    }
}

@Composable
private fun AddPhotoTile(isEditable: Boolean, onClick: () -> Unit) {
    val shape = AppTheme.shapes.medium
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.control.xl)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .dashedBorder(
                width = AppTheme.borders.control,
                color = AppTheme.colors.border.strong,
                shape = shape,
                dash = AppTheme.spacing.xxs,
            )
            .clickable(
                enabled = isEditable,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Plus.imageVector,
            contentDescription = stringResource(R.string.report_add_photo),
            tint = AppTheme.colors.text.muted,
            modifier = Modifier.size(AppTheme.sizes.icon.m),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotosRowPreview() {
    AppTheme {
        PhotosRow(
            state = PhotosState.example(count = 2),
            isEditable = true,
            onAction = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotosRowFullPreview() {
    AppTheme {
        PhotosRow(
            state = PhotosState.example(count = 3),
            isEditable = true,
            onAction = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
