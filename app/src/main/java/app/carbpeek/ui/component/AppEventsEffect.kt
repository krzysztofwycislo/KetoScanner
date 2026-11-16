package app.carbpeek.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import app.carbpeek.core.designsystem.component.showAppMessage
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.text.rememberUiTextResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
