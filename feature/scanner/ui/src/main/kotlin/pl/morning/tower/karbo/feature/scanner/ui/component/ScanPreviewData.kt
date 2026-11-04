package pl.handsome.club.feature.scanner.ui.component

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.ScanFrameMode
import pl.handsome.club.core.designsystem.icon.AppIcon
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.scanner.ui.R
import pl.handsome.club.feature.scanner.ui.ScanResources
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.FoundCardState
import pl.handsome.club.feature.scanner.ui.state.NotFoundCardState
import pl.handsome.club.feature.scanner.ui.state.ScanState
import pl.handsome.club.feature.scanner.ui.state.TorchButtonState
import pl.handsome.club.feature.scanner.ui.state.TripItemState

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
        hint = hint,
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
)

internal val previewNotFound = NotFoundCardState(
    message = previewResources.notFoundMessage("5900512300320"),
    showTypeIt = true,
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
