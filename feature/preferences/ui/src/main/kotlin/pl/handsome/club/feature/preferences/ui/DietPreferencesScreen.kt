package pl.handsome.club.feature.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.TopBar
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnInputChanged
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Error
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Form
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesState

@Composable
internal fun DietPreferencesScreen(onNavigation: (DietPreferencesNavigation) -> Unit) {
    val viewModel: DietPreferencesViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is DietPreferencesNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: DietPreferencesState, onAction: (DietPreferencesAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Ui.TopBar(
                title = stringResource(R.string.preferences_title),
                onBackClick = { onAction(OnBackClicked) },
            )
        },
    ) { innerPadding ->
        val centered = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        Ui.ContentCrossfade(targetState = state.content) { content ->
            when (content) {
                InitialLoad -> Box(centered, contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                is Error -> Box(centered, contentAlignment = Alignment.Center) { ErrorContent(content, onAction) }

                is Form -> FormContent(
                    state = content,
                    onAction = onAction,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .imePadding(),
                )
            }
        }
    }
}

@Composable
private fun FormContent(state: Form, onAction: (DietPreferencesAction) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Text(
            text = state.summary.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
        )
        TextField(
            value = state.input,
            onValueChange = { onAction(OnInputChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preferencesMaxCarbsInput"),
            textStyle = AppTheme.typography.body,
            label = { Text(text = stringResource(R.string.preferences_input_label), style = AppTheme.typography.caption) },
            isError = state.inputError != null,
            supportingText = state.inputError?.let { error ->
                { Text(text = error.resolve(), style = AppTheme.typography.caption) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(OnSaveClicked) }),
            shape = AppTheme.shapes.small,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppTheme.colors.background.default,
                unfocusedContainerColor = AppTheme.colors.background.default,
                errorContainerColor = AppTheme.colors.background.default,
                focusedTextColor = AppTheme.colors.text.default,
                unfocusedTextColor = AppTheme.colors.text.default,
                focusedIndicatorColor = AppTheme.colors.accent,
                unfocusedIndicatorColor = AppTheme.colors.border.default,
                cursorColor = AppTheme.colors.accent,
            ),
        )
        Button(
            onClick = { onAction(OnSaveClicked) },
            modifier = Modifier
                .align(Alignment.End)
                .testTag("preferencesSaveButton"),
        ) {
            Text(text = stringResource(R.string.preferences_save), style = AppTheme.typography.body)
        }
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (DietPreferencesAction) -> Unit) {
    Column(
        modifier = Modifier.padding(AppTheme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.preferences_retry), style = AppTheme.typography.body)
        }
    }
}

private fun summaryExample() = UiText.Res(R.string.preferences_summary, UiArg.Numeric.Count(40))

@Preview(showBackground = true)
@Composable
private fun DietPreferencesFormPreview() {
    AppTheme {
        ScreenContent(DietPreferencesState(Form(summary = summaryExample(), input = "40", inputError = null))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun DietPreferencesInvalidInputPreview() {
    AppTheme {
        ScreenContent(
            DietPreferencesState(
                Form(
                    summary = summaryExample(),
                    input = "0",
                    inputError = UiText.Res(R.string.preferences_input_error, UiArg.Numeric.Count(1), UiArg.Numeric.Count(999)),
                )
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun DietPreferencesInitialLoadPreview() {
    AppTheme {
        ScreenContent(DietPreferencesState(InitialLoad)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun DietPreferencesErrorPreview() {
    AppTheme {
        ScreenContent(DietPreferencesState(Error(AppError.Storage.toUiText()))) {}
    }
}
