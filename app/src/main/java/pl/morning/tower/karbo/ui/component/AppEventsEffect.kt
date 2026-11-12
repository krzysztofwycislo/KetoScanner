package pl.morning.tower.karbo.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pl.morning.tower.karbo.core.designsystem.component.showAppMessage
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.core.designsystem.text.rememberUiTextResolver

@Composable
internal fun AppEventsEffect(events: Flow<AppEvent>, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    val resolver = rememberUiTextResolver()
    val hideKeyboard = rememberHideKeyboard()

    ObserveEvents(events) { event ->
        when (event) {
            is AppEvent.ShowMessage -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showAppMessage(resolver.resolve(event.text), event.tone)
            }

            AppEvent.HideKeyboard -> hideKeyboard()
        }
    }
}
