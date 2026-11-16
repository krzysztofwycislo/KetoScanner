package pl.morning.tower.karbo.feature.scanner.ui.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.ScanFrameMode
import pl.morning.tower.karbo.core.designsystem.icon.icon
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.OnboardingTip
import pl.morning.tower.karbo.feature.scanner.ui.ScanResources
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanStatePrototype as Prototype

internal class ScanStateFactory(private val resources: ScanResources) {

    fun createFor(prototype: Prototype): ScanState = ScanState(
        query = prototype.query,
        isLookUpEnabled = !prototype.showProgress,
        camera = prototype.camera(),
        found = prototype.foundAnalysis?.toFoundCard(showVerdictTip = prototype.isVerdictTipVisible),
        notFound = prototype.notFoundBarcode?.let {
            val isCameraGranted = prototype.cameraPermission == CameraPermission.Granted
            NotFoundCardState(
                barcode = resources.notFoundBarcode(it),
                message = resources.notFoundMessage(),
                showTypeIt = isCameraGranted,
                dismissLabel = if (isCameraGranted) resources.scanNext() else resources.close(),
            )
        },
    )

    private val Prototype.showProgress: Boolean
        get() = isSearching || (foundBarcode != null && foundAnalysis == null)

    private fun Prototype.camera(): CameraState = when (cameraPermission) {
        CameraPermission.Granted -> CameraState.Preview(
            isPaused = isScanBlocked,
            isTorchOn = hasTorch && isTorchOn,
            torchButton = torchButton(),
            isTypeSheetOpen = isTypeSheetOpen,
            trip = trip(),
            frame = frame(),
            hint = hint(),
        )

        CameraPermission.Rationale -> CameraState.PermissionPanel(
            title = resources.rationaleTitle(),
            message = resources.permissionRationale(),
            buttonLabel = resources.allowCamera(),
        )

        CameraPermission.Denied -> CameraState.PermissionPanel(
            title = resources.deniedTitle(),
            message = resources.permissionDenied(),
            buttonLabel = resources.openSettings(),
        )

        CameraPermission.Unchecked, CameraPermission.Requesting -> CameraState.Hidden
    }

    private fun Prototype.hint(): CameraHintState = when {
        showProgress -> CameraHintState(text = resources.searchingHint(), supportingText = null)

        OnboardingTip.FIRST_SCAN_HINT in pendingTips -> CameraHintState(
            text = resources.firstScanHintTitle(),
            supportingText = resources.firstScanHintBody((dietPreferences ?: DietPreferences.Default).dietMode),
        )

        else -> CameraHintState(text = resources.aimHint(), supportingText = null)
    }

    private fun Prototype.frame(): ScanFrameMode = when {
        foundAnalysis != null -> ScanFrameMode.Found
        notFoundBarcode != null -> ScanFrameMode.NotFound
        showProgress -> ScanFrameMode.Searching
        isScanBlocked -> ScanFrameMode.Idle
        else -> ScanFrameMode.Aiming
    }

    private fun Prototype.torchButton(): TorchButtonState? {
        if (!hasTorch) return null
        return TorchButtonState(
            isActive = isTorchOn,
            contentDescription = if (isTorchOn) resources.torchOff() else resources.torchOn(),
        )
    }

    private fun ProductAnalysis.toFoundCard(showVerdictTip: Boolean): FoundCardState {
        val name = resources.productName(product.name)
        val brand = resources.brand(product.brand)
        val icon = product.category.icon
        return when (this) {
            is ProductAnalysis.Rated -> FoundCardState(
                name = name,
                icon = icon,
                brand = brand,
                rating = macronutrients.rate.toRating(),
                title = resources.verdictTitle(macronutrients.rate, macronutrients.dietMode),
                detail = resources.foundDetail(macronutrients),
                showVerdictTip = showVerdictTip,
            )

            is ProductAnalysis.InsufficientData -> FoundCardState(
                name = name,
                icon = icon,
                brand = brand,
                rating = Rating.Unknown,
                title = resources.insufficientData(),
                detail = resources.insufficientDataDetail(),
                showVerdictTip = showVerdictTip,
            )
        }
    }

    private fun Prototype.trip(): ImmutableList<TripItemState> {
        if (isTypeSheetOpen || foundBarcode != null || notFoundBarcode != null) return persistentListOf()
        val mode = (dietPreferences ?: DietPreferences.Default).dietMode
        val windowStart = now - Prototype.TRIP_WINDOW
        return tripEntries
            .filter { it.updateTime >= windowStart }
            .take(Prototype.TRIP_LIMIT)
            .map { it.toTripItem(mode) }
            .toImmutableList()
    }

    private fun ProductAnalysisHistoryEntry.toTripItem(mode: DietMode) = TripItemState(
        barcode = productBarcode,
        name = resources.productName(productName),
        icon = category.icon,
        rating = dietRate.toRating(),
        carbs = resources.tripCarbs(carbsPer100g),
        description = resources.tripDescription(dietRate, mode, carbsPer100g),
    )

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }
}
