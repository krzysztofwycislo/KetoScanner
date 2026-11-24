package app.carbpeek.feature.preferences.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.RadioCard
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnOptionSelected
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import app.carbpeek.feature.preferences.ui.state.DietProfileOptionState

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

@Preview(showBackground = true)
@Composable
private fun OptionListPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            OptionList(previewOptions(DietProfileOption.KETO)) {}
        }
    }
}
