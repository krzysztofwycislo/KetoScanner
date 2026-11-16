package pl.morning.tower.karbo.feature.scanner.ui.component

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.ScanFrameMode
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.scanner.ui.R
import pl.morning.tower.karbo.feature.scanner.ui.ScanResources
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraHintState
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraState
import pl.morning.tower.karbo.feature.scanner.ui.state.FoundCardState
import pl.morning.tower.karbo.feature.scanner.ui.state.NotFoundCardState
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanState
import pl.morning.tower.karbo.feature.scanner.ui.state.TorchButtonState
import pl.morning.tower.karbo.feature.scanner.ui.state.TripItemState

internal val previewResources = ScanResources()

internal val previewTorchOff = TorchButtonState(isActive = false, contentDescription = previewResources.torchOn())

internal fun previewState(
    query: String = "",
    isLookUpEnabled: Boolean = true,
    isPaused: Boolean = false,
    isTorchOn: Boolean = false,
    torchButton: TorchButtonState? = previewTorchOff,
    isTypeSheetOpen: Boolean = false,
    frame: ScanFrameMode = ScanFrameMode.Aiming,
    hint: UiText = previewResources.aimHint(),
    hintSupportingText: UiText? = null,
    found: FoundCardState? = null,
    notFound: NotFoundCardState? = null,
    trip: ImmutableList<TripItemState> = persistentListOf(),
) = ScanState(
    query = query,
    isLookUpEnabled = isLookUpEnabled,
    camera = CameraState.Preview(
        isPaused = isPaused,
        isTorchOn = isTorchOn,
        torchButton = torchButton,
        isTypeSheetOpen = isTypeSheetOpen,
        trip = trip,
        frame = frame,
        hint = CameraHintState(text = hint, supportingText = hintSupportingText),
    ),
    found = found,
    notFound = notFound,
)

internal fun previewPermissionState(
    title: Int,
    message: Int,
    buttonLabel: Int,
    query: String = "",
    notFound: NotFoundCardState? = null,
) = ScanState(
    query = query,
    isLookUpEnabled = true,
    camera = CameraState.PermissionPanel(
        title = UiText.Res(title),
        message = UiText.Res(message),
        buttonLabel = UiText.Res(buttonLabel),
    ),
    found = null,
    notFound = notFound,
)

internal val previewFound = FoundCardState(
    name = UiText.Raw("Coca-Cola Original Taste"),
    icon = AppIcons.Bottle,
    brand = UiText.Raw("Coca-Cola"),
    rating = Rating.Negative,
    title = UiText.Res(R.string.scan_verdict_not_advised_keto),
    detail = UiText.Res(R.string.scan_found_reason_carb_energy_share, UiArg.Numeric.Percent(1.0)),
    showVerdictTip = false,
)

internal val previewNotFound = NotFoundCardState(
    barcode = previewResources.notFoundBarcode("5900512300320"),
    message = previewResources.notFoundMessage(),
    showTypeIt = true,
    dismissLabel = previewResources.scanNext(),
)

internal fun previewTripItem(
    name: String,
    icon: AppIcon,
    rating: Rating,
    carbs: Double?,
    verdict: Int,
) = TripItemState(
    barcode = name,
    name = UiText.Raw(name),
    icon = icon,
    rating = rating,
    carbs = previewResources.tripCarbs(carbs),
    description = UiText.Res(verdict),
)

internal val previewTrip = persistentListOf(
    previewTripItem(
        "Greek yogurt 10%",
        AppIcons.Cup,
        Rating.Positive,
        3.6,
        R.string.scan_verdict_good_keto,
    ),
    previewTripItem(
        "Dark chocolate 85%",
        AppIcons.Bar,
        Rating.Neutral,
        14.0,
        R.string.scan_verdict_neutral,
    ),
    previewTripItem(
        "Mineral water",
        AppIcons.Bottle,
        Rating.Unknown,
        null,
        R.string.scan_verdict_insufficient_data,
    ),
)
