package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(AppTheme.sizes.control.m),
        enabled = enabled,
        shape = AppTheme.shapes.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
            contentColor = AppTheme.colors.text.inverse,
        ),
    ) {
        Text(text = text, style = AppTheme.typography.button)
    }
}

@Composable
fun Ui.SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(AppTheme.sizes.control.m),
        enabled = enabled,
        shape = AppTheme.shapes.control,
        border = BorderStroke(AppTheme.borders.control, AppTheme.colors.border.strong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.background.surface,
            contentColor = AppTheme.colors.text.default,
        ),
    ) {
        Text(text = text, style = AppTheme.typography.button)
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    AppTheme {
        val text = stringResource(R.string.designsystem_preview_button)
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Ui.PrimaryButton(text = text, onClick = {})
            Ui.PrimaryButton(text = text, onClick = {}, enabled = false)
            Ui.SecondaryButton(text = text, onClick = {})
            Ui.SecondaryButton(text = text, onClick = {}, enabled = false)
        }
    }
}
