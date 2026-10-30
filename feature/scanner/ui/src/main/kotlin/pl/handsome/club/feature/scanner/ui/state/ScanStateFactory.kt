package pl.handsome.club.feature.scanner.ui.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.scanner.ui.ScanResources
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

internal class ScanStateFactory(private val resources: ScanResources) {

    fun createFor(prototype: Prototype): ScanState = ScanState(
        query = prototype.query,
        showProgress = prototype.isSearching || (prototype.foundBarcode != null && prototype.foundAnalysis == null),
        camera = prototype.camera(),
        found = prototype.foundAnalysis?.toFoundCard(),
    )

    private fun Prototype.camera(): CameraState = when (cameraPermission) {
        CameraPermission.Granted -> CameraState.Preview(
            isPaused = isScanBlocked,
            torch = torch(),
            isTypeSheetOpen = isTypeSheetOpen,
            trip = trip(),
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

    private fun Prototype.torch(): Torch = when {
        !hasTorch -> Torch.Unavailable
        isTorchOn -> Torch.On
        else -> Torch.Off
    }

    private fun ProductAnalysis.toFoundCard(): FoundCardState {
        val name = resources.productName(product.name)
        val brand = resources.brand(product.brand)
        return when (this) {
            is ProductAnalysis.Rated -> FoundCardState(
                name = name,
                brand = brand,
                rating = macronutrients.rate.toRating(),
                title = resources.verdictTitle(macronutrients.rate, macronutrients.dietMode),
                detail = resources.carbsPer100g(macronutrients.carbsPer100g),
            )

            is ProductAnalysis.InsufficientData -> FoundCardState(
                name = name,
                brand = brand,
                rating = Rating.Unknown,
                title = resources.insufficientData(),
                detail = resources.insufficientDataDetail(),
            )
        }
    }

    private fun Prototype.trip(): ImmutableList<TripItemState> {
        if (isTypeSheetOpen || foundBarcode != null) return persistentListOf()
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
