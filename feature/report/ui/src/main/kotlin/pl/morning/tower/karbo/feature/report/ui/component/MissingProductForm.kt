package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import pl.morning.tower.karbo.core.designsystem.component.InfoChip
import pl.morning.tower.karbo.core.designsystem.component.NoteCard
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.TopBar
import pl.morning.tower.karbo.core.designsystem.component.TopBarNavigation
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnCloseClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnSendClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnSlotClicked
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductContentState.Form

@Composable
internal fun MissingProductForm(
    state: Form,
    onAction: (MissingProductAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Ui.TopBar(
            title = stringResource(R.string.report_missing_title),
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
            Text(
                text = stringResource(R.string.report_missing_intro),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text.subtle,
            )
            Ui.InfoChip(
                text = state.barcode.resolve(),
                icon = AppIcons.Barcode.imageVector,
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                state.slots.forEach { slot ->
                    PhotoSlotCard(
                        state = slot,
                        isEnabled = state.isEditable,
                        onClick = { onAction(OnSlotClicked(slot.slot)) },
                    )
                }
            }
            Ui.NoteCard(text = stringResource(R.string.report_missing_tip))
        }
        Column(
            modifier = Modifier.padding(AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Ui.PrimaryButton(
                text = state.sendButton.label.resolve(),
                onClick = { onAction(OnSendClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.sendButton.isEnabled,
                loading = state.sendButton.isLoading,
            )
            Text(
                text = stringResource(R.string.report_missing_footer),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.text.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
