package pl.handsome.club.feature.scanner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.designsystem.component.CameraHint
import pl.handsome.club.core.designsystem.component.CameraIconButton
import pl.handsome.club.core.designsystem.component.CameraPillButton
import pl.handsome.club.core.designsystem.component.CircleIconButton
import pl.handsome.club.core.designsystem.component.IconTile
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.ScanFrame
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.SecondaryButton
import pl.handsome.club.core.designsystem.component.TextInput
import pl.handsome.club.core.designsystem.component.TopBar
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictBanner
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.scanner.ui.camera.CameraPreview
import pl.handsome.club.feature.scanner.ui.camera.LightStatusBarIconsEffect
import pl.handsome.club.feature.scanner.ui.camera.rememberCameraPermissionHandler
import pl.handsome.club.feature.scanner.ui.camera.rememberHasTorch
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnDetailsClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnScanNextClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnSearchSubmitted
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnStart
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTorchClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTypeSheetDismissed
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.FoundCardState
import pl.handsome.club.feature.scanner.ui.state.ScanState
import pl.handsome.club.feature.scanner.ui.state.Torch

@Composable
internal fun ScanScreen(onNavigation: (ScanNavigation) -> Unit) {
    val viewModel: ScanViewModel = koinViewModel()
    val permissionHandler = rememberCameraPermissionHandler { check ->
        viewModel.onAction(OnCameraPermissionResult(check))
    }
    val hasTorch = rememberHasTorch()

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart(permissionHandler.check(), hasTorch))
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
    when (val camera = state.camera) {
        is CameraState.Preview -> CameraContent(state, camera, onAction)
        is CameraState.PermissionPanel -> PermissionContent(state, camera, onAction)
        CameraState.Hidden -> Ui.Screen(containerColor = AppTheme.colors.camera.background) {
            LightStatusBarIconsEffect()
        }
    }
}

@Composable
private fun CameraContent(state: ScanState, camera: CameraState.Preview, onAction: (ScanAction) -> Unit) {
    Ui.Screen(containerColor = AppTheme.colors.camera.background) { innerPadding ->
        LightStatusBarIconsEffect()
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                isPaused = camera.isPaused,
                torchEnabled = camera.torch == Torch.On,
                onBarcodeScanned = { onAction(OnBarcodeScanned(it)) },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cameraPreviewView"),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CameraHeader(camera.torch, onAction)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppTheme.spacing.xl * 2),
                ) {
                    if (state.showProgress) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppTheme.spacing.m),
                        )
                    }
                }
                Ui.ScanFrame()
                Ui.CameraHint(
                    text = stringResource(R.string.scan_aim_hint),
                    modifier = Modifier.padding(top = AppTheme.spacing.m),
                )
            }
            TypeSheet(state, camera.isTypeSheetOpen, onAction)
            FoundOverlay(state.found, onAction)
        }
    }
}

@Composable
private fun BoxScope.FoundOverlay(found: FoundCardState?, onAction: (ScanAction) -> Unit) {
    BackHandler(enabled = found != null) { onAction(OnScanNextClicked) }
    AnimatedVisibility(
        visible = found != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.matchParentSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.camera.dim)
                .pointerInput(Unit) {},
        )
    }
    var lastFound by remember { mutableStateOf(found) }
    if (found != null) lastFound = found
    AnimatedVisibility(
        visible = found != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        lastFound?.let {
            FoundCard(it, onAction)
        }
    }
}

@Composable
private fun FoundCard(found: FoundCardState, onAction: (ScanAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.m)
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .pointerInput(Unit) {}
            .padding(AppTheme.spacing.l)
            .testTag("scanFoundCard"),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ui.IconTile(AppIcons.Bag)
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                Text(
                    text = found.name.resolve(),
                    style = AppTheme.typography.section,
                    color = AppTheme.colors.text.default,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                found.brand?.let {
                    Text(
                        text = it.resolve(),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.text.muted,
                    )
                }
            }
        }
        Ui.VerdictBanner(rating = found.rating, title = found.title, details = listOf(found.detail))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Ui.SecondaryButton(
                text = stringResource(R.string.scan_next),
                onClick = { onAction(OnScanNextClicked) },
                modifier = Modifier.weight(1f),
            )
            Ui.PrimaryButton(
                text = stringResource(R.string.scan_details),
                onClick = { onAction(OnDetailsClicked) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CameraHeader(torch: Torch, onAction: (ScanAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (torch == Torch.Unavailable) {
            Spacer(Modifier)
        } else {
            Ui.CameraIconButton(
                icon = AppIcons.Torch,
                contentDescription = UiText.Res(
                    if (torch == Torch.On) {
                        R.string.scan_torch_off
                    } else {
                        R.string.scan_torch_on
                    },
                ),
                active = torch == Torch.On,
                onClick = { onAction(OnTorchClicked) },
            )
        }
        Ui.CameraPillButton(
            text = stringResource(R.string.scan_type_it),
            icon = AppIcons.Keyboard,
            onClick = { onAction(OnTypeItClicked) },
        )
    }
}

@Composable
private fun BoxScope.TypeSheet(state: ScanState, visible: Boolean, onAction: (ScanAction) -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.matchParentSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.camera.dim)
                .clickable(interactionSource = null, indication = null) { onAction(OnTypeSheetDismissed) },
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .imePadding(),
    ) {
        TypeCard(state, visible, onAction)
    }
}

@Composable
private fun TypeCard(state: ScanState, visible: Boolean, onAction: (ScanAction) -> Unit) {
    BackHandler(enabled = visible) { onAction(OnTypeSheetDismissed) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.m)
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .pointerInput(Unit) {}
            .padding(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.scan_type_title),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
            Ui.CircleIconButton(
                icon = AppIcons.Cross,
                contentDescription = UiText.Res(R.string.scan_close),
                onClick = { onAction(OnTypeSheetDismissed) },
            )
        }
        Text(
            text = stringResource(R.string.scan_type_helper),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.muted,
        )
        BarcodeEntry(
            query = state.query,
            showProgress = state.showProgress,
            onAction = onAction,
            inputModifier = Modifier.focusRequester(focusRequester),
        )
    }
}

@Composable
private fun PermissionContent(state: ScanState, camera: CameraState.PermissionPanel, onAction: (ScanAction) -> Unit) {
    Ui.Screen(
        topBar = {
            Ui.TopBar(stringResource(R.string.scan_title))
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(start = AppTheme.spacing.l, end = AppTheme.spacing.l, bottom = AppTheme.spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
            ) {
                PermissionCard(camera, onAction, Modifier.weight(1f))
                PanelCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = AppTheme.spacing.m,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.scan_type_title),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.text.muted,
                    )
                    BarcodeEntry(query = state.query, showProgress = state.showProgress, onAction = onAction)
                }
            }
            FoundOverlay(state.found, onAction)
        }
    }
}

@Composable
private fun PermissionCard(camera: CameraState.PermissionPanel, onAction: (ScanAction) -> Unit, modifier: Modifier) {
    PanelCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cameraPermissionPanel"),
        contentPadding = AppTheme.spacing.xxl,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.messageIcon)
                .background(AppTheme.colors.background.subtle, AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.CameraOff,
                contentDescription = null,
                tint = AppTheme.colors.text.muted,
                modifier = Modifier.size(AppTheme.sizes.iconM),
            )
        }
        Text(
            text = camera.title.resolve(),
            style = AppTheme.typography.headline,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        Text(
            text = camera.message.resolve(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
        Ui.PrimaryButton(text = camera.buttonLabel.resolve(), onClick = { onAction(OnPermissionButtonClicked) })
    }
}

@Composable
private fun PanelCard(
    modifier: Modifier,
    contentPadding: Dp,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.background.surface, AppTheme.shapes.large)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.large)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
private fun BarcodeEntry(
    query: String,
    showProgress: Boolean,
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
            enabled = !showProgress,
        )
    }
}

@Composable
private fun PreviewContent(state: ScanState) {
    AppTheme {
        ScreenContent(state) {}
    }
}

private fun previewState(
    query: String = "",
    showProgress: Boolean = false,
    torch: Torch = Torch.Off,
    isTypeSheetOpen: Boolean = false,
    found: FoundCardState? = null,
) = ScanState(
    query = query,
    showProgress = showProgress,
    camera = CameraState.Preview(
        isPaused = showProgress || isTypeSheetOpen || found != null,
        torch = torch,
        isTypeSheetOpen = isTypeSheetOpen,
    ),
    found = found,
)

@Preview
@Composable
private fun ScanAimingPreview() {
    PreviewContent(previewState())
}

@Preview
@Composable
private fun ScanTorchOnPreview() {
    PreviewContent(previewState(torch = Torch.On))
}

@Preview
@Composable
private fun ScanTorchUnavailablePreview() {
    PreviewContent(previewState(torch = Torch.Unavailable))
}

@Preview
@Composable
private fun ScanTypeSheetPreview() {
    PreviewContent(previewState(query = "5900512300320", isTypeSheetOpen = true))
}

@Preview
@Composable
private fun ScanSearchingPreview() {
    PreviewContent(previewState(query = "5900512300320", showProgress = true, isTypeSheetOpen = true))
}

@Preview
@Composable
private fun ScanFoundRatedPreview() {
    PreviewContent(
        previewState(
            found = FoundCardState(
                name = UiText.Raw("Nutella hazelnut spread with cocoa"),
                brand = UiText.Raw("Ferrero"),
                rating = Rating.Negative,
                title = UiText.Res(R.string.scan_verdict_not_advised_keto),
                detail = UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(57.5)),
            )
        )
    )
}

@Preview
@Composable
private fun ScanFoundInsufficientDataPreview() {
    PreviewContent(
        previewState(
            found = FoundCardState(
                name = UiText.Res(R.string.scan_found_unnamed),
                brand = null,
                rating = Rating.Unknown,
                title = UiText.Res(R.string.scan_verdict_insufficient_data),
                detail = UiText.Res(R.string.scan_found_insufficient_detail),
            )
        )
    )
}

@Preview
@Composable
private fun ScanRationalePreview() {
    PreviewContent(
        ScanState(
            query = "",
            showProgress = false,
            camera = CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_rationale_title),
                message = UiText.Res(R.string.scan_camera_permission_rationale),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
            ),
            found = null,
        )
    )
}

@Preview
@Composable
private fun ScanDeniedPreview() {
    PreviewContent(
        ScanState(
            query = "",
            showProgress = false,
            camera = CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_denied_title),
                message = UiText.Res(R.string.scan_camera_permission_denied),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
            ),
            found = null,
        )
    )
}

@Preview
@Composable
private fun ScanHiddenPreview() {
    PreviewContent(ScanState(query = "", showProgress = false, camera = CameraState.Hidden, found = null))
}
