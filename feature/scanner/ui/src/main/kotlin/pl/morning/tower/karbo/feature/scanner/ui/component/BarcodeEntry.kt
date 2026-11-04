package pl.handsome.club.feature.scanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.TextInput
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.scanner.ui.R
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnSearchSubmitted

@Composable
internal fun BarcodeEntry(
    query: String,
    isLookUpEnabled: Boolean,
    onAction: (ScanAction) -> Unit,
    inputModifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.TextInput(
            value = query,
            onValueChange = { onAction(OnQueryChanged(it)) },
            placeholder = stringResource(R.string.scan_barcode_placeholder),
            modifier = inputModifier
                .weight(1f)
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
