package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.NoteCard(
    text: String,
    modifier: Modifier = Modifier,
    icon: AppIcon = AppIcons.Info,
) {
    val shape = AppTheme.shapes.card
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, shape)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            tint = AppTheme.colors.text.subtle,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
        Text(
            text = text,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.subtle,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteCardPreview() {
    AppTheme {
        Ui.NoteCard(
            text = stringResource(R.string.designsystem_preview_note),
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
