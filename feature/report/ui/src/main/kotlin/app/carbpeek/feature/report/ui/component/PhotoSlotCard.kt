package app.carbpeek.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.ProductImage
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppColors
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.state.PhotoSlot
import app.carbpeek.feature.report.ui.state.PhotoSlotState
import app.carbpeek.feature.report.ui.state.SlotTagTone

@Composable
internal fun PhotoSlotCard(
    state: PhotoSlotState,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.card
    val outline = if (state.photoUri == null) {
        Modifier.dashedBorder(
            width = AppTheme.borders.control,
            color = AppTheme.colors.border.strong,
            shape = shape,
            dash = AppTheme.spacing.xxs,
        )
    } else {
        Modifier.border(
            width = AppTheme.borders.selected,
            color = AppTheme.colors.accent,
            shape = shape,
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.sizes.control.l)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .then(outline)
            .semantics(mergeDescendants = true) {}
            .clickable(
                enabled = isEnabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SlotTile(photoUri = state.photoUri)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.label.resolve(),
                    style = AppTheme.typography.bodyStrong,
                    color = AppTheme.colors.text.default,
                )
                TagPill(text = state.tag.resolve(), tone = state.tagTone)
            }
            Text(
                text = state.note.resolve(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.muted,
            )
        }
        Text(
            text = state.actionLabel.resolve(),
            style = AppTheme.typography.label,
            color = AppTheme.colors.text.accent,
        )
    }
}

@Composable
private fun SlotTile(photoUri: String?) {
    val tileModifier = Modifier
        .size(AppTheme.sizes.iconDiscCompact)
        .clip(AppTheme.shapes.control)
    if (photoUri == null) {
        Box(
            modifier = tileModifier.background(AppTheme.colors.background.subtle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Camera.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.muted,
                modifier = Modifier.size(AppTheme.sizes.icon.m),
            )
        }
    } else {
        Ui.ProductImage(
            url = photoUri,
            contentDescription = null,
            modifier = tileModifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun TagPill(text: String, tone: SlotTagTone) {
    val colors = tone.colors()
    Text(
        text = text,
        style = AppTheme.typography.label,
        color = colors.content,
        modifier = Modifier
            .background(colors.container, AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.xs, vertical = AppTheme.spacing.xxs),
    )
}

@Composable
private fun SlotTagTone.colors(): AppColors.Tone = when (this) {
    SlotTagTone.REQUIRED -> AppTheme.colors.tag.required
    SlotTagTone.RECOMMENDED -> AppTheme.colors.tag.recommended
}

@Composable
private fun PhotoSlotCardPreview(state: PhotoSlotState) {
    AppTheme {
        PhotoSlotCard(
            state = state,
            isEnabled = true,
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoSlotCardEmptyRequiredPreview() {
    PhotoSlotCardPreview(state = PhotoSlotState.example(slot = PhotoSlot.NUTRITION_TABLE))
}

@Preview(showBackground = true)
@Composable
private fun PhotoSlotCardEmptyRecommendedPreview() {
    PhotoSlotCardPreview(state = PhotoSlotState.example(slot = PhotoSlot.INGREDIENTS))
}

@Preview(showBackground = true)
@Composable
private fun PhotoSlotCardFilledPreview() {
    PhotoSlotCardPreview(state = PhotoSlotState.example(slot = PhotoSlot.FRONT, hasPhoto = true))
}
