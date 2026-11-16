package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    minLines: Int = 1,
) {
    InputField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        containerColor = AppTheme.colors.background.default,
        leadingIcon = null,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        enabled = enabled,
        minLines = minLines,
    )
}

@Composable
internal fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    containerColor: Color,
    leadingIcon: ImageVector?,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    minLines: Int = 1,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val textStyle = AppTheme.typography.bodyLarge
    val isMultiLine = minLines > 1
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle.copy(color = AppTheme.colors.text.default),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = minLines,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(AppTheme.colors.accent),
        decorationBox = { innerTextField ->
            val containerModifier = Modifier
                .heightIn(min = AppTheme.sizes.control.m)
                .background(containerColor, AppTheme.shapes.control)
                .border(
                    width = AppTheme.borders.control,
                    color = if (isFocused) AppTheme.colors.accent else AppTheme.colors.border.strong,
                    shape = AppTheme.shapes.control,
                )
                .padding(horizontal = AppTheme.spacing.s)
            val textBox: @Composable (Modifier) -> Unit = { boxModifier ->
                Box(
                    modifier = if (isMultiLine) {
                        boxModifier.padding(vertical = AppTheme.spacing.s)
                    } else {
                        boxModifier
                    },
                    contentAlignment = if (isMultiLine) Alignment.TopStart else Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = AppTheme.colors.text.muted)
                    }
                    innerTextField()
                }
            }
            if (leadingIcon == null) {
                textBox(containerModifier)
            } else {
                Row(
                    modifier = containerModifier,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = AppTheme.colors.text.muted,
                        modifier = Modifier.size(AppTheme.sizes.icon.s),
                    )
                    textBox(Modifier.weight(1f))
                }
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

@Preview(showBackground = true)
@Composable
private fun TextInputMultiLinePreview() {
    AppTheme {
        Ui.TextInput(
            value = "",
            onValueChange = {},
            placeholder = stringResource(R.string.designsystem_preview_body),
            modifier = Modifier.padding(AppTheme.spacing.m),
            singleLine = false,
            minLines = 3,
        )
    }
}
