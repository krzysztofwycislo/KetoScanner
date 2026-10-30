package pl.handsome.club.feature.preferences.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.RadioCard
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.RatingRow
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.SegmentedToggle
import pl.handsome.club.core.designsystem.component.TextInput
import pl.handsome.club.core.designsystem.component.TopBar
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnCustomModeSelected
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnInputChanged
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnOptionSelected
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import pl.handsome.club.feature.preferences.ui.state.CustomProfileState
import pl.handsome.club.feature.preferences.ui.state.DietModeOptionState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Error
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Form
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesState
import pl.handsome.club.feature.preferences.ui.state.DietProfileOption
import pl.handsome.club.feature.preferences.ui.state.DietProfileOptionState
import pl.handsome.club.feature.preferences.ui.state.RatingGuideRowState
import pl.handsome.club.feature.preferences.ui.state.RatingGuideState

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
            if (state.ratingGuide != null) {
                RatingGuide(state.ratingGuide)
            }
        }
        Ui.PrimaryButton(
            text = stringResource(R.string.preferences_save),
            onClick = { onAction(OnSaveClicked) },
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.l)
                .testTag("preferencesSaveButton"),
        )
    }
}

@Composable
private fun OptionList(options: List<DietProfileOptionState>, onAction: (DietPreferencesAction) -> Unit) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        options.forEach { option ->
            Ui.RadioCard(
                title = option.title.resolve(),
                note = option.note.resolve(),
                trailing = option.trailing?.resolve(),
                selected = option.isSelected,
                onClick = { onAction(OnOptionSelected(option.option)) },
                modifier = Modifier.testTag("preferencesOption_${option.option.name}"),
            )
        }
    }
}

@Composable
private fun CustomProfile(state: CustomProfileState, onAction: (DietPreferencesAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Ui.SegmentedToggle(
            options = state.modes.map { it.label.resolve() },
            selectedIndex = state.modes.indexOfFirst { it.isSelected },
            onSelect = { index -> onAction(OnCustomModeSelected(state.modes[index].mode)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Ui.TextInput(
            value = state.input,
            onValueChange = { onAction(OnInputChanged(it)) },
            placeholder = stringResource(R.string.preferences_input_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preferencesMaxCarbsInput"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(OnSaveClicked) }),
        )
        if (state.inputError != null) {
            Text(
                text = state.inputError.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.verdict.negative.content,
            )
        }
    }
}

@Composable
private fun RatingGuide(state: RatingGuideState) {
    val shape = AppTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, shape)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.preferences_rating_title),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = state.subtitle.resolve(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
        )
        state.rows.forEach { row ->
            Ui.RatingRow(
                rating = row.rating,
                title = row.label.resolve(),
                value = row.value.resolve(),
            )
        }
        Text(
            text = state.footnote.resolve(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
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

private val previewTitles = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto,
    DietProfileOption.KETO to R.string.preferences_preset_keto,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb,
    DietProfileOption.CUSTOM to R.string.preferences_preset_custom,
)

private val previewNotes = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto_note,
    DietProfileOption.KETO to R.string.preferences_preset_keto_note,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb_note,
    DietProfileOption.CUSTOM to R.string.preferences_preset_custom_note,
)

private fun DietProfileOptionState.Companion.example(option: DietProfileOption, selected: Boolean, grams: Int?) =
    DietProfileOptionState(
        option = option,
        title = UiText.Res(previewTitles.getValue(option)),
        note = UiText.Res(previewNotes.getValue(option)),
        trailing = grams?.let { UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(it.toLong())) },
        isSelected = selected,
    )

private fun previewOptions(selected: DietProfileOption, customGrams: Int?) = DietProfileOption.entries.map {
    DietProfileOptionState.example(
        option = it,
        selected = it == selected,
        grams = it.preset?.preferences?.maxDailyCarbsAmount ?: customGrams,
    )
}.toPersistentList()

private fun DietModeOptionState.Companion.example(mode: DietMode, selected: Boolean) = DietModeOptionState(
    mode = mode,
    label = UiText.Res(
        when (mode) {
            DietMode.KETO -> R.string.preferences_diet_mode_keto
            DietMode.LOW_CARB -> R.string.preferences_diet_mode_low_carb
        },
    ),
    isSelected = selected,
)

private fun previewCustom(input: String, inputError: UiText?) = CustomProfileState(
    modes = persistentListOf(
        DietModeOptionState.example(DietMode.KETO, selected = true),
        DietModeOptionState.example(DietMode.LOW_CARB, selected = false),
    ),
    input = input,
    inputError = inputError,
)

private fun RatingGuideRowState.Companion.example(rating: Rating, label: Int, value: Int, grams: Double) =
    RatingGuideRowState(
        rating = rating,
        label = UiText.Res(label),
        value = UiText.Res(value, UiArg.Numeric.Grams(grams)),
    )

private fun previewRatingGuide(dailyCarbs: Int) = RatingGuideState(
    subtitle = UiText.Res(R.string.preferences_rating_subtitle, UiArg.Numeric.Count(dailyCarbs.toLong())),
    rows = persistentListOf(
        RatingGuideRowState.example(
            Rating.Positive,
            R.string.preferences_rating_good_keto,
            R.string.preferences_rating_up_to,
            dailyCarbs / 3.0,
        ),
        RatingGuideRowState.example(
            Rating.Neutral,
            R.string.preferences_rating_neutral,
            R.string.preferences_rating_up_to,
            dailyCarbs / 2.0,
        ),
        RatingGuideRowState.example(
            Rating.Negative,
            R.string.preferences_rating_not_advised_keto,
            R.string.preferences_rating_over,
            dailyCarbs / 2.0,
        ),
    ),
    footnote = UiText.Res(R.string.preferences_rating_footnote_keto),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DietPreferencesPresetPreview() {
    AppTheme {
        ScreenContent(
            DietPreferencesState(
                Form(
                    options = previewOptions(DietProfileOption.KETO, customGrams = null),
                    custom = null,
                    ratingGuide = previewRatingGuide(40),
                    isSaving = false,
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
                    ratingGuide = previewRatingGuide(35),
                    isSaving = false,
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
                    custom = previewCustom(
                        input = "0",
                        inputError = UiText.Res(
                            R.string.preferences_input_error,
                            UiArg.Numeric.Count(1),
                            UiArg.Numeric.Count(999),
                        ),
                    ),
                    ratingGuide = null,
                    isSaving = false,
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
