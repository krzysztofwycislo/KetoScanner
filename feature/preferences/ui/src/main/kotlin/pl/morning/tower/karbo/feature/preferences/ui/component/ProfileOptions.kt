package pl.handsome.club.feature.preferences.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.component.RadioCard
import pl.handsome.club.core.designsystem.component.SegmentedToggle
import pl.handsome.club.core.designsystem.component.TextInput
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.preferences.ui.R
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnCustomModeSelected
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnInputChanged
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnOptionSelected
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import pl.handsome.club.feature.preferences.ui.state.CustomProfileState
import pl.handsome.club.feature.preferences.ui.state.DietProfileOption
import pl.handsome.club.feature.preferences.ui.state.DietProfileOptionState

@Composable
internal fun OptionList(options: List<DietProfileOptionState>, onAction: (DietPreferencesAction) -> Unit) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        options.forEach { option ->
            Ui.RadioCard(
                title = option.title.resolve(),
                note = option.note.resolve(),
                trailing = option.trailing?.resolve(),
                selected = option.isSelected,
                onClick = { onAction(OnOptionSelected(option.option)) },
                modifier = Modifier.testTag("preferencesOption_${option.option.name}"),
            )
        }
    }
}

@Composable
internal fun CustomProfile(state: CustomProfileState, onAction: (DietPreferencesAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Ui.SegmentedToggle(
            options = state.modes.map { it.label.resolve() },
            selectedIndex = state.modes.indexOfFirst { it.isSelected },
            onSelect = { index -> onAction(OnCustomModeSelected(state.modes[index].mode)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Ui.TextInput(
            value = state.input,
            onValueChange = { onAction(OnInputChanged(it)) },
            placeholder = stringResource(R.string.preferences_input_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preferencesMaxCarbsInput"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(OnSaveClicked) }),
        )
        if (state.inputError != null) {
            Text(
                text = state.inputError.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.verdict.negative.content,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionListPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            OptionList(previewOptions(DietProfileOption.KETO, customGrams = null)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomProfilePreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            CustomProfile(previewCustom("35", inputError = null)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomProfileInvalidInputPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            CustomProfile(previewCustom("0", inputError = previewInputError)) {}
        }
    }
}
