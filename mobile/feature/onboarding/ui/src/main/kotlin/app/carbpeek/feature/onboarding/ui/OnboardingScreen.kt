package app.carbpeek.feature.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.designsystem.component.NoteCard
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.RadioCard
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnPresetSelected
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnSkipClicked
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnStartScanningClicked
import app.carbpeek.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation
import app.carbpeek.feature.onboarding.ui.state.OnboardingState
import app.carbpeek.feature.onboarding.ui.state.PresetOptionState
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingScreen(onNavigation: (OnboardingNavigation) -> Unit) {
    val viewModel: OnboardingViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is OnboardingNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: OnboardingState, onAction: (OnboardingAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Header(onAction)
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(AppTheme.spacing.l),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
            ) {
                Intro()
                PresetList(state.options, onAction)
                Ui.NoteCard(text = stringResource(R.string.onboarding_hint))
                Spacer(modifier = Modifier.weight(1f))
                Ui.PrimaryButton(
                    text = stringResource(R.string.onboarding_start_scanning),
                    onClick = { onAction(OnStartScanningClicked) },
                    enabled = !state.isSaving,
                    icon = AppIcons.Scan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboardingStart"),
                )
                Text(
                    text = stringResource(R.string.onboarding_disclaimer),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Header(onAction: (OnboardingAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.m, top = AppTheme.spacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.icon.l)
                    .background(AppTheme.colors.accent, AppTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = AppIcons.Logo.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.sizes.icon.s),
                )
            }
            Text(
                text = stringResource(R.string.onboarding_app_name),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
        }
        TextButton(
            onClick = { onAction(OnSkipClicked) },
            modifier = Modifier
                .heightIn(min = AppTheme.sizes.control.m)
                .testTag("onboardingSkip"),
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip),
                style = AppTheme.typography.button,
                color = AppTheme.colors.text.subtle,
            )
        }
    }
}

@Composable
private fun Intro() {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = AppTheme.typography.display,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = stringResource(R.string.onboarding_intro),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.muted,
        )
    }
}

@Composable
private fun PresetList(options: PersistentList<PresetOptionState>, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        options.forEach { option ->
            Ui.RadioCard(
                title = option.title.resolve(),
                note = option.note.resolve(),
                trailing = option.limit.resolve(),
                badge = option.badge?.resolve(),
                selected = option.isSelected,
                onClick = { onAction(OnPresetSelected(option.preset)) },
                modifier = Modifier.testTag("onboardingPreset_${option.preset.name}"),
            )
        }
    }
}

private fun PresetOptionState.Companion.example(preset: DietPreset, selected: DietPreset): PresetOptionState {
    val resources = OnboardingResources()
    return PresetOptionState(
        preset = preset,
        title = resources.title(preset),
        note = resources.note(preset),
        limit = resources.limit(preset.preferences.maxDailyCarbsAmount),
        badge = if (preset == DietPreset.KETO) resources.suggested() else null,
        isSelected = preset == selected,
    )
}

private fun previewState(selected: DietPreset, isSaving: Boolean = false) = OnboardingState(
    options = DietPreset.entries.map { PresetOptionState.example(it, selected) }.toPersistentList(),
    isSaving = isSaving,
)

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun OnboardingKetoPreview() {
    AppTheme {
        ScreenContent(previewState(DietPreset.KETO)) {}
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1200, fontScale = 1.5f)
@Composable
private fun OnboardingKetoLargeFontPreview() {
    OnboardingKetoPreview()
}

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun OnboardingStrictKetoPreview() {
    AppTheme {
        ScreenContent(previewState(DietPreset.STRICT_KETO)) {}
    }
}

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun OnboardingSavingPreview() {
    AppTheme {
        ScreenContent(previewState(DietPreset.KETO, isSaving = true)) {}
    }
}
