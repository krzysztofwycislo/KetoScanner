package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.SecondaryButton
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.component.color
import pl.morning.tower.karbo.core.designsystem.component.containerColor
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnBackToReportClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnHomeClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnScanNextClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnTryAgainClicked

@Composable
internal fun SentContent(onAction: (IssueReportAction) -> Unit, modifier: Modifier = Modifier) {
    ReportResult(
        icon = AppIcons.Check,
        tone = Rating.Positive,
        title = stringResource(R.string.report_sent_title),
        body = stringResource(R.string.report_sent_body),
        modifier = modifier,
        buttons = {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Ui.SecondaryButton(
                    text = stringResource(R.string.report_home),
                    onClick = { onAction(OnHomeClicked) },
                    modifier = Modifier.weight(1f),
                )
                Ui.PrimaryButton(
                    text = stringResource(R.string.report_scan_next),
                    onClick = { onAction(OnScanNextClicked) },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Step(number = 1, label = stringResource(R.string.report_step_sent), isDone = true)
            Step(number = 2, label = stringResource(R.string.report_step_check), isDone = false)
            Step(number = 3, label = stringResource(R.string.report_step_fix), isDone = false)
        }
    }
}

@Composable
internal fun FailedContent(onAction: (IssueReportAction) -> Unit, modifier: Modifier = Modifier) {
    ReportResult(
        icon = AppIcons.WifiOff,
        tone = Rating.Negative,
        title = stringResource(R.string.report_failed_title),
        body = stringResource(R.string.report_failed_body),
        modifier = modifier,
        buttons = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Ui.PrimaryButton(
                    text = stringResource(R.string.report_try_again),
                    onClick = { onAction(OnTryAgainClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppTheme.sizes.control.m)
                        .clip(AppTheme.shapes.control)
                        .clickable(role = Role.Button) { onAction(OnBackToReportClicked) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.report_back_to_report),
                        style = AppTheme.typography.button,
                        color = AppTheme.colors.text.subtle,
                    )
                }
            }
        },
    )
}

@Composable
private fun ReportResult(
    icon: AppIcon,
    tone: Rating,
    title: String,
    body: String,
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.l),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppTheme.spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
            ) {
                Box(
                    modifier = Modifier
                        .size(AppTheme.sizes.iconDiscLarge)
                        .background(tone.containerColor(), AppTheme.shapes.circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon.imageVector,
                        contentDescription = null,
                        tint = tone.color(),
                        modifier = Modifier.size(AppTheme.sizes.icon.xl),
                    )
                }
                Text(
                    text = title,
                    style = AppTheme.typography.display,
                    color = AppTheme.colors.text.default,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = body,
                    style = AppTheme.typography.bodyStrong,
                    color = AppTheme.colors.text.muted,
                    textAlign = TextAlign.Center,
                )
                content()
            }
        }
        buttons()
    }
}

@Composable
private fun Step(number: Int, label: String, isDone: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, AppTheme.shapes.control)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.control)
            .padding(AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.control.xs)
                .background(
                    color = if (isDone) AppTheme.colors.accent else AppTheme.colors.background.subtle,
                    shape = AppTheme.shapes.circle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = AppTheme.typography.label,
                color = if (isDone) AppTheme.colors.text.inverse else AppTheme.colors.text.subtle,
            )
        }
        Text(
            text = label,
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
        )
    }
}
