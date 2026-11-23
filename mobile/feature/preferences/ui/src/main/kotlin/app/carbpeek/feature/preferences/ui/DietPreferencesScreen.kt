package app.carbpeek.feature.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.ContentCrossfade
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.TopBar
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.withBottomBarClearance
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.preferences.ui.component.CustomProfile
import app.carbpeek.feature.preferences.ui.component.OptionList
import app.carbpeek.feature.preferences.ui.component.previewCustom
import app.carbpeek.feature.preferences.ui.component.previewInputError
import app.carbpeek.feature.preferences.ui.component.previewOptions
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.Error
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.Form
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import app.carbpeek.feature.preferences.ui.state.DietPreferencesState
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import org.koin.compose.viewmodel.koinViewModel

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
    ) { screenPadding ->
        val innerPadding = screenPadding.withBottomBarClearance()
        val centered = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        Ui.ContentCrossfade(targetState = state.content) { content ->
            when (content) {
                InitialLoad -> Box(centered, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is Error -> Box(centered, contentAlignment = Alignment.Center) {
                    ErrorContent(content, onAction)
                }

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
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.l)
                .padding(top = AppTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            Text(
                text = stringResource(R.string.preferences_intro),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text.muted,
            )
            OptionList(state.options, onAction)
            if (state.custom != null) {
                CustomProfile(state.custom, onAction)
            }
        }
        Ui.PrimaryButton(
            text = stringResource(R.string.preferences_save),
            onClick = { onAction(OnSaveClicked) },
            enabled = state.isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.l)
                .testTag("preferencesSaveButton"),
        )
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DietPreferencesPresetPreview() {
    AppTheme {
        ScreenContent(
            DietPreferencesState(
                Form(
                    options = previewOptions(DietProfileOption.KETO, customGrams = null),
                    custom = null,
                    isSaveEnabled = true,
                ),
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun DietPreferencesCustomPreview() {
    AppTheme {
        ScreenContent(
            DietPreferencesState(
                Form(
                    options = previewOptions(DietProfileOption.CUSTOM, customGrams = 35),
                    custom = previewCustom("35", inputError = null),
                    isSaveEnabled = true,
                ),
            ),
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun DietPreferencesInvalidInputPreview() {
    AppTheme {
        ScreenContent(
            DietPreferencesState(
                Form(
                    options = previewOptions(DietProfileOption.CUSTOM, customGrams = null),
                    custom = previewCustom(input = "0", inputError = previewInputError),
                    isSaveEnabled = true,
                ),
            ),
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
