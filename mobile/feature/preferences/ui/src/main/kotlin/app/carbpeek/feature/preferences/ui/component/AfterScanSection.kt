package app.carbpeek.feature.preferences.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.SwitchCard
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.preferences.ui.R
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnDetailsSwitchChanged
import app.carbpeek.feature.preferences.ui.state.DetailsSwitchState

@Composable
internal fun AfterScanSection(
    detailsSwitch: DetailsSwitchState,
    onAction: (DietPreferencesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.preferences_after_scan),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
            modifier = Modifier.semantics { heading() },
        )
        Ui.SwitchCard(
            title = stringResource(R.string.preferences_details_title),
            note = stringResource(R.string.preferences_details_note),
            checked = detailsSwitch.isOn,
            onCheckedChange = { onAction(OnDetailsSwitchChanged(it)) },
            modifier = Modifier.testTag("preferencesDetailsSwitch"),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AfterScanSectionPreview() {
    AppTheme {
        AfterScanSection(
            detailsSwitch = DetailsSwitchState(isOn = true),
            onAction = {},
            modifier = Modifier.padding(AppTheme.spacing.l),
        )
    }
}
