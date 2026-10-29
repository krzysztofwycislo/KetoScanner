package pl.handsome.club.feature.scanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.scanner.ui.camera.CameraPreview
import pl.handsome.club.feature.scanner.ui.camera.rememberCameraPermissionHandler
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnSearchSubmitted
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnStart
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.ScanState

@Composable
internal fun ScanScreen(onNavigation: (ScanNavigation) -> Unit) {
    val viewModel: ScanViewModel = koinViewModel()
    val permissionHandler = rememberCameraPermissionHandler { check ->
        viewModel.onAction(OnCameraPermissionResult(check))
    }

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart(permissionHandler.check()))
        onStopOrDispose {}
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ScanNavigation -> onNavigation(event)
            RequestCameraPermission -> permissionHandler.request()
            OpenAppSettings -> permissionHandler.openSettings()
        }
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: ScanState, onAction: (ScanAction) -> Unit) {
    Ui.Screen { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CameraArea(state.camera, onAction)
            if (state.showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(innerPadding)
                        .padding(AppTheme.spacing.m)
                        .fillMaxWidth(),
                )
            }
            SearchInput(
                query = state.query,
                onAction = onAction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(innerPadding)
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun CameraArea(state: CameraState, onAction: (ScanAction) -> Unit) {
    when (state) {
        is CameraState.Preview -> CameraPreview(
            isPaused = state.isPaused,
            onBarcodeScanned = { onAction(OnBarcodeScanned(it)) },
            modifier = Modifier
                .fillMaxSize()
                .testTag("cameraPreviewView"),
        )

        is CameraState.PermissionPanel -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PermissionPanel(state, onAction)
        }

        CameraState.Hidden -> Unit
    }
}

@Composable
private fun PermissionPanel(state: CameraState.PermissionPanel, onAction: (ScanAction) -> Unit) {
    Surface(
        modifier = Modifier
            .padding(AppTheme.spacing.m)
            .testTag("cameraPermissionPanel"),
        shape = AppTheme.shapes.small,
        color = AppTheme.colors.background.default,
        contentColor = AppTheme.colors.text.default,
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Text(
                text = state.message.resolve(),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text.default,
                textAlign = TextAlign.Center,
            )
            Button(onClick = { onAction(OnPermissionButtonClicked) }) {
                Text(text = state.buttonLabel.resolve(), style = AppTheme.typography.body)
            }
        }
    }
}

@Composable
private fun SearchInput(query: String, onAction: (ScanAction) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = { onAction(OnQueryChanged(it)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.m)
            .testTag("searchInput"),
        textStyle = AppTheme.typography.body,
        placeholder = { Text(text = stringResource(R.string.scan_enter_barcode), style = AppTheme.typography.body) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onAction(OnSearchSubmitted) }),
        shape = AppTheme.shapes.small,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background.default,
            unfocusedContainerColor = AppTheme.colors.background.default,
            focusedTextColor = AppTheme.colors.text.default,
            unfocusedTextColor = AppTheme.colors.text.default,
            focusedIndicatorColor = AppTheme.colors.accent,
            unfocusedIndicatorColor = AppTheme.colors.border.default,
            cursorColor = AppTheme.colors.accent,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScanPreviewPreview() {
    AppTheme {
        ScreenContent(ScanState(query = "", showProgress = false, camera = CameraState.Preview(isPaused = false))) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanRationalePreview() {
    AppTheme {
        ScreenContent(
            ScanState(
                query = "",
                showProgress = false,
                camera = CameraState.PermissionPanel(
                    message = UiText.Res(R.string.scan_camera_permission_rationale),
                    buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
                ),
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanDeniedPreview() {
    AppTheme {
        ScreenContent(
            ScanState(
                query = "",
                showProgress = false,
                camera = CameraState.PermissionPanel(
                    message = UiText.Res(R.string.scan_camera_permission_denied),
                    buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
                ),
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanSearchingPreview() {
    AppTheme {
        ScreenContent(
            ScanState(query = "5900000000000", showProgress = true, camera = CameraState.Preview(isPaused = true))
        ) {}
    }
}
