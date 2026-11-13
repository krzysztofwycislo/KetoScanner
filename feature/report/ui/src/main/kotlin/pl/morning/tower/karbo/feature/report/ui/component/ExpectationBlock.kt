package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.color
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnExpectedRateClicked
import pl.morning.tower.karbo.feature.report.ui.state.ExpectationState
import pl.morning.tower.karbo.feature.report.ui.state.ExpectedRateOptionState
import pl.morning.tower.karbo.feature.report.ui.state.RatedAsState

@Composable
internal fun ExpectationBlock(
    state: ExpectationState,
    isEditable: Boolean,
    onAction: (IssueReportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        OptionalLabel(text = stringResource(R.string.report_expectation))
        if (state.ratedAs != null) {
            RatedAs(state.ratedAs)
        }
        state.options.forEach { option ->
            ExpectedRateRow(
                state = option,
                isEditable = isEditable,
                onClick = { onAction(OnExpectedRateClicked(option.rate)) },
            )
        }
    }
}

@Composable
private fun RatedAs(state: RatedAsState) {
    val prefix = stringResource(R.string.report_rated_as)
    val label = state.label.resolve()
    val ratingColor = state.rating.color()
    Text(
        text = buildAnnotatedString {
            append(prefix)
            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = ratingColor)) {
                append(label)
            }
        },
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.text.muted,
    )
}

@Composable
private fun ExpectedRateRow(state: ExpectedRateOptionState, isEditable: Boolean, onClick: () -> Unit) {
    val shape = AppTheme.shapes.control
    val border = if (state.isSelected) {
        AppTheme.borders.selected
    } else {
        AppTheme.borders.control
    }
    val borderColor = if (state.isSelected) {
        AppTheme.colors.accent
    } else {
        AppTheme.colors.border.default
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.sizes.control.m)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .border(border, borderColor, shape)
            .selectable(
                selected = state.isSelected,
                enabled = isEditable,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.icon.m / 2)
                .background(state.rating.color(), AppTheme.shapes.circle),
        )
        Text(
            text = state.label.resolve(),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.default,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpectationBlockPreview() {
    AppTheme {
        ExpectationBlock(
            state = ExpectationState.example(
                ratedAs = RatedAsState.example(rate = DietRate.NOT_ADVISED, rating = Rating.Negative),
                selected = DietRate.NEUTRAL,
            ),
            isEditable = true,
            onAction = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
