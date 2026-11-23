package app.carbpeek.feature.scanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.TextInput
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.R
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnQueryChanged
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnSearchSubmitted

private const val INPUT_MIN_WIDTH_IN_FONT_SIZES = 10

@Composable
internal fun BarcodeEntry(
    query: String,
    isLookUpEnabled: Boolean,
    onAction: (ScanAction) -> Unit,
    inputModifier: Modifier = Modifier,
) {
    val fontSize = with(LocalDensity.current) { AppTheme.typography.bodyLarge.fontSize.toDp() }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.TextInput(
            value = query,
            onValueChange = { onAction(OnQueryChanged(it)) },
            placeholder = stringResource(R.string.scan_barcode_placeholder),
            modifier = inputModifier
                .weight(1f)
                .width(fontSize * INPUT_MIN_WIDTH_IN_FONT_SIZES)
                .testTag("searchInput"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onAction(OnSearchSubmitted) }),
        )
        Ui.PrimaryButton(
            text = stringResource(R.string.scan_look_up),
            onClick = { onAction(OnSearchSubmitted) },
            enabled = isLookUpEnabled,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeEntryPreview() {
    AppTheme {
        Row(modifier = Modifier.padding(AppTheme.spacing.m)) {
            BarcodeEntry(query = "5900512300320", isLookUpEnabled = true, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeEntrySearchingPreview() {
    AppTheme {
        Row(modifier = Modifier.padding(AppTheme.spacing.m)) {
            BarcodeEntry(query = "5900512300320", isLookUpEnabled = false, onAction = {})
        }
    }
}
