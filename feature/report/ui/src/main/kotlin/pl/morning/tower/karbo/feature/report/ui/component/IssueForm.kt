package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.morning.tower.karbo.core.designsystem.component.FilterChip
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.TextInput
import pl.morning.tower.karbo.core.designsystem.component.TopBar
import pl.morning.tower.karbo.core.designsystem.component.TopBarNavigation
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnCloseClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnDetailsChanged
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnReasonClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnSendClicked
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Form

@Composable
internal fun IssueForm(state: Form, onAction: (IssueReportAction) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Ui.TopBar(
            title = stringResource(R.string.report_title),
            onBackClick = { onAction(OnCloseClicked) },
            navigationIcon = TopBarNavigation.Close,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.l)
                .padding(top = AppTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            ProductSummary(state.product)
            Text(
                text = stringResource(R.string.report_whats_wrong),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
            ReasonChips(state, onAction)
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                OptionalLabel(text = stringResource(R.string.report_details))
                Ui.TextInput(
                    value = state.details,
                    onValueChange = { onAction(OnDetailsChanged(it)) },
                    placeholder = state.detailsPlaceholder.resolve(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    enabled = state.isEditable,
                    minLines = 3,
                )
            }
            if (state.expectation != null) {
                ExpectationBlock(
                    state = state.expectation,
                    isEditable = state.isEditable,
                    onAction = onAction,
                )
            }
        }
        Ui.PrimaryButton(
            text = state.sendButton.label.resolve(),
            onClick = { onAction(OnSendClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.l),
            enabled = state.sendButton.isEnabled,
            loading = state.sendButton.isLoading,
        )
    }
}

@Composable
private fun ReasonChips(state: Form, onAction: (IssueReportAction) -> Unit) {
    FlowRow(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        state.reasons.forEach { chip ->
            Ui.FilterChip(
                text = chip.label.resolve(),
                selected = chip.isSelected,
                onClick = { onAction(OnReasonClicked(chip.reason)) },
                enabled = state.isEditable,
            )
        }
    }
}
