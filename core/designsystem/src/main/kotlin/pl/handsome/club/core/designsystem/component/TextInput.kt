package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val textStyle = AppTheme.typography.bodyLarge
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(color = AppTheme.colors.text.default),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(AppTheme.colors.accent),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .heightIn(min = AppTheme.sizes.control)
                    .background(AppTheme.colors.background.default, AppTheme.shapes.control)
                    .border(
                        width = AppTheme.borders.control,
                        color = if (isFocused) AppTheme.colors.accent else AppTheme.colors.border.strong,
                        shape = AppTheme.shapes.control,
                    )
                    .padding(horizontal = AppTheme.spacing.s),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle, color = AppTheme.colors.text.muted)
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TextInputPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            val text = stringResource(R.string.designsystem_preview_body)
            Ui.TextInput(value = "", onValueChange = {}, placeholder = text)
            Ui.TextInput(value = text, onValueChange = {}, placeholder = "")
        }
    }
}
