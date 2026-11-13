package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R

@Composable
internal fun OptionalLabel(text: String, modifier: Modifier = Modifier) {
    val optional = stringResource(R.string.report_optional)
    val mutedColor = AppTheme.colors.text.muted
    Text(
        text = buildAnnotatedString {
            append(text)
            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = mutedColor)) {
                append(optional)
            }
        },
        style = AppTheme.typography.body.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colors.text.default,
        modifier = modifier,
    )
}
