package pl.handsome.club.feature.scanner.ui

import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.scanner.ui.state.CameraPermission
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.FoundCardState
import pl.handsome.club.feature.scanner.ui.state.NotFoundCardState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import pl.handsome.club.feature.scanner.ui.state.Torch
import pl.handsome.club.feature.scanner.ui.state.TripItemState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

class ScanStateFactoryTest {

    private val factory = ScanStateFactory(ScanResources())

    @Test
    fun `when permission is unchecked or requesting then camera is hidden`() {
        assertEquals(CameraState.Hidden, camera(Prototype(cameraPermission = CameraPermission.Unchecked)))
        assertEquals(CameraState.Hidden, camera(Prototype(cameraPermission = CameraPermission.Requesting)))
    }

    @Test
    fun `when permission is granted and nothing pauses then preview is not paused`() {
        assertEquals(preview(isPaused = false), camera(granted()))
    }

    @Test
    fun `when permission is granted and scan is paused then preview is paused`() {
        assertEquals(preview(isPaused = true), camera(granted().copy(isScanPaused = true)))
    }

    @Test
    fun `when permission is granted and a search runs then preview is paused`() {
        assertEquals(preview(isPaused = true), camera(granted().copy(isSearching = true)))
    }

    @Test
    fun `when the type sheet is open then preview shows it and is paused`() {
        assertEquals(
            preview(isPaused = true, isTypeSheetOpen = true),
            camera(granted().copy(isTypeSheetOpen = true)),
        )
    }

    @Test
    fun `when the device has no torch then torch is unavailable even if it was turned on`() {
        assertEquals(preview(torch = Torch.Unavailable), camera(granted().copy(hasTorch = false, isTorchOn = true)))
    }

    @Test
    fun `when the device has a torch then torch follows its on state`() {
        assertEquals(preview(torch = Torch.Off), camera(granted().copy(hasTorch = true, isTorchOn = false)))
        assertEquals(preview(torch = Torch.On), camera(granted().copy(hasTorch = true, isTorchOn = true)))
    }

    @Test
    fun `when permission needs rationale then panel shows rationale title message and allow camera`() {
        assertEquals(
            CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_rationale_title),
                message = UiText.Res(R.string.scan_camera_permission_rationale),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
            ),
            camera(Prototype(cameraPermission = CameraPermission.Rationale)),
        )
    }

    @Test
    fun `when permission is denied then panel shows denied title message and open settings`() {
        assertEquals(
            CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_denied_title),
                message = UiText.Res(R.string.scan_camera_permission_denied),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
            ),
            camera(Prototype(cameraPermission = CameraPermission.Denied)),
        )
    }

    @Test
    fun `when searching or not then progress follows`() {
        assertTrue(factory.createFor(Prototype(isSearching = true)).showProgress)
        assertFalse(factory.createFor(Prototype(isSearching = false)).showProgress)
    }

    @Test
    fun `when query is set then it is passed through unchanged`() {
        assertEquals(" 590 ", factory.createFor(Prototype(query = " 590 ")).query)
    }

    @Test
    fun `when a barcode is found without an analysis then progress shows the preview is paused and no card shows`() {
        val state = factory.createFor(granted().copy(foundBarcode = BARCODE))

        assertTrue(state.showProgress)
        assertNull(state.found)
        assertEquals(preview(isPaused = true), state.camera)
    }

    @Test
    fun `when nothing is found then there is no card`() {
        assertNull(factory.createFor(granted()).found)
        assertNull(factory.createFor(granted()).notFound)
    }

    @Test
    fun `when a barcode is not found then the not found card shows its message and the preview is paused`() {
        val state = factory.createFor(granted().copy(notFoundBarcode = BARCODE))

        assertEquals(
            NotFoundCardState(UiText.Res(R.string.scan_not_found_message, UiArg.Text(BARCODE))),
            state.notFound,
        )
        assertNull(state.found)
        assertFalse(state.showProgress)
        assertEquals(preview(isPaused = true), state.camera)
    }

    @Test
    fun `when a rated analysis is found in keto mode then each rate maps to its rating title and carbs`() {
        val expected = mapOf(
            DietRate.GOOD to (Rating.Positive to R.string.scan_verdict_good_keto),
            DietRate.NEUTRAL to (Rating.Neutral to R.string.scan_verdict_neutral),
            DietRate.NOT_ADVISED to (Rating.Negative to R.string.scan_verdict_not_advised_keto),
        )
        expected.forEach { (rate, ratingAndTitle) ->
            assertEquals(
                FoundCardState(
                    name = UiText.Raw("Nutella"),
                    icon = AppIcons.Jar,
                    brand = UiText.Raw("Ferrero"),
                    rating = ratingAndTitle.first,
                    title = UiText.Res(ratingAndTitle.second),
                    detail = UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(57.5)),
                ),
                found(rated(rate, DietMode.KETO)),
            )
        }
    }

    @Test
    fun `when a rated analysis is found in low carb mode then titles are low carb`() {
        assertEquals(
            UiText.Res(R.string.scan_verdict_good_low_carb),
            found(rated(DietRate.GOOD, DietMode.LOW_CARB))?.title,
        )
        assertEquals(
            UiText.Res(R.string.scan_verdict_neutral),
            found(rated(DietRate.NEUTRAL, DietMode.LOW_CARB))?.title,
        )
        assertEquals(
            UiText.Res(R.string.scan_verdict_not_advised_low_carb),
            found(rated(DietRate.NOT_ADVISED, DietMode.LOW_CARB))?.title,
        )
    }

    @Test
    fun `when a rated analysis has reasons then the detail shows the first reason`() {
        val reasons = listOf(
            RatingReason.HighCarbEnergyShare(1.0, DietRate.NOT_ADVISED),
            RatingReason.HighSugars(10.6, DietRate.NOT_ADVISED),
        )

        assertEquals(
            UiText.Res(R.string.scan_found_reason_carb_energy_share, UiArg.Numeric.Percent(1.0)),
            found(rated(DietRate.NOT_ADVISED, DietMode.KETO, reasons))?.detail,
        )
    }

    @Test
    fun `when a rated analysis has a reason then each reason maps to its text`() {
        val resources = ScanResources()
        val expected = mapOf(
            RatingReason.HighCarbs(13.5, DietRate.NOT_ADVISED) to
                UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(13.5)),
            RatingReason.HighCarbEnergyShare(0.8, DietRate.NEUTRAL) to
                UiText.Res(R.string.scan_found_reason_carb_energy_share, UiArg.Numeric.Percent(0.8)),
            RatingReason.LowFatAndProteinToCarbRatio(0.4, DietRate.NOT_ADVISED) to
                UiText.Res(R.string.scan_found_reason_low_fat_and_protein_to_carb_ratio, UiArg.Numeric.Ratio(0.4)),
            RatingReason.HighSugars(10.6, DietRate.NEUTRAL) to
                UiText.Res(R.string.scan_found_reason_high_sugars, UiArg.Numeric.Grams(10.6)),
            RatingReason.HighCarbsPerServing(330.0, 35.0, DietRate.NOT_ADVISED) to
                UiText.Res(
                    R.string.scan_found_reason_high_carbs_per_serving,
                    UiArg.Numeric.Grams(35.0),
                    UiArg.Numeric.Grams(330.0),
                ),
        )
        expected.forEach { (reason, text) ->
            val result = MacronutrientAnalysisResult(DietMode.KETO, reason.rate, listOf(reason), 57.5, null, 40)
            assertEquals(text, resources.foundDetail(result))
        }
    }

    @Test
    fun `when an analysis with insufficient data is found then the card shows unknown with the insufficient texts`() {
        assertEquals(
            FoundCardState(
                name = UiText.Raw("Nutella"),
                icon = AppIcons.Jar,
                brand = UiText.Raw("Ferrero"),
                rating = Rating.Unknown,
                title = UiText.Res(R.string.scan_verdict_insufficient_data),
                detail = UiText.Res(R.string.scan_found_insufficient_detail),
            ),
            found(ProductAnalysis.InsufficientData(product)),
        )
    }

    @Test
    fun `when the found product has a blank name and brand then it is unnamed without a brand`() {
        val card = found(ProductAnalysis.InsufficientData(product.copy(name = " ", brand = "")))

        assertEquals(UiText.Res(R.string.scan_found_unnamed), card?.name)
        assertNull(card?.brand)
    }

    @Test
    fun `when a found analysis is shown then progress is hidden`() {
        assertFalse(
            factory.createFor(
                granted().copy(foundBarcode = BARCODE, foundAnalysis = ProductAnalysis.InsufficientData(product))
            ).showProgress
        )
    }

    @Test
    fun `when trip entries are inside the window then each maps to an item with its category icon`() {
        val items = trip(
            withTrip(
                entry(1, rate = DietRate.GOOD, carbs = 3.6, category = ProductCategory.CHEESE),
                entry(2, name = " ", rate = DietRate.NEUTRAL, carbs = null),
                entry(3, rate = null, carbs = 1.0),
            )
        )

        assertEquals(
            listOf(
                TripItemState(
                    barcode = "barcode-1",
                    name = UiText.Raw("Name 1"),
                    icon = AppIcons.Cheese,
                    rating = Rating.Positive,
                    carbs = UiText.Res(R.string.scan_trip_carbs, UiArg.Numeric.Grams(3.6)),
                    description = UiText.Res(
                        R.string.scan_trip_verdict_with_carbs,
                        UiArg.Nested(UiText.Res(R.string.scan_verdict_good_keto)),
                        UiArg.Nested(UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(3.6))),
                    ),
                ),
                TripItemState(
                    barcode = "barcode-2",
                    name = UiText.Res(R.string.scan_found_unnamed),
                    icon = AppIcons.Bag,
                    rating = Rating.Neutral,
                    carbs = UiText.Res(R.string.scan_trip_carbs_missing),
                    description = UiText.Res(R.string.scan_verdict_neutral),
                ),
                TripItemState(
                    barcode = "barcode-3",
                    name = UiText.Raw("Name 3"),
                    icon = AppIcons.Bag,
                    rating = Rating.Unknown,
                    carbs = UiText.Res(R.string.scan_trip_carbs, UiArg.Numeric.Grams(1.0)),
                    description = UiText.Res(
                        R.string.scan_trip_verdict_with_carbs,
                        UiArg.Nested(UiText.Res(R.string.scan_verdict_insufficient_data)),
                        UiArg.Nested(UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(1.0))),
                    ),
                ),
            ),
            items,
        )
    }

    @Test
    fun `when a not advised entry is in the trip then its rating is negative`() {
        assertEquals(Rating.Negative, trip(withTrip(entry(1, rate = DietRate.NOT_ADVISED))).single().rating)
    }

    @Test
    fun `when an entry is older than the window then it is dropped and the boundary is kept`() {
        val items = trip(
            withTrip(
                entry(1, age = 1.hours),
                entry(2, age = Prototype.TRIP_WINDOW),
                entry(3, age = Prototype.TRIP_WINDOW + 1.seconds),
            )
        )

        assertEquals(listOf("barcode-1", "barcode-2"), items.map { it.barcode })
    }

    @Test
    fun `when more entries than the limit are given then at most three are shown in the given order`() {
        val items = trip(withTrip(entry(4), entry(2), entry(9), entry(1)))

        assertEquals(listOf("barcode-4", "barcode-2", "barcode-9"), items.map { it.barcode })
        assertEquals(3, Prototype.TRIP_LIMIT)
        assertEquals(3.hours, Prototype.TRIP_WINDOW)
    }

    @Test
    fun `when the type sheet is open or a found or not found barcode is set then the trip is empty`() {
        assertTrue(trip(withTrip(entry(1)).copy(isTypeSheetOpen = true)).isEmpty())
        assertTrue(trip(withTrip(entry(1)).copy(foundBarcode = BARCODE)).isEmpty())
        assertTrue(trip(withTrip(entry(1)).copy(notFoundBarcode = BARCODE)).isEmpty())
        assertTrue(
            trip(
                withTrip(entry(1)).copy(
                    foundBarcode = BARCODE,
                    foundAnalysis = ProductAnalysis.InsufficientData(product),
                )
            ).isEmpty()
        )
    }

    @Test
    fun `when the diet mode changes then the trip description follows it`() {
        val keto = DietPreferences(40, DietMode.KETO)
        val lowCarb = DietPreferences(40, DietMode.LOW_CARB)

        assertEquals(
            UiText.Res(R.string.scan_verdict_good_keto),
            trip(withTrip(entry(1, carbs = null), preferences = keto)).single().description,
        )
        assertEquals(
            UiText.Res(R.string.scan_verdict_good_low_carb),
            trip(withTrip(entry(1, carbs = null), preferences = lowCarb)).single().description,
        )
        assertEquals(
            UiText.Res(R.string.scan_verdict_not_advised_low_carb),
            trip(withTrip(entry(1, rate = DietRate.NOT_ADVISED, carbs = null), preferences = lowCarb))
                .single().description,
        )
        assertEquals(
            UiText.Res(R.string.scan_verdict_good_keto),
            trip(withTrip(entry(1, carbs = null), preferences = null)).single().description,
        )
    }

    @Test
    fun `when the rate is missing and carbs are missing then the description is not enough data`() {
        assertEquals(
            UiText.Res(R.string.scan_verdict_insufficient_data),
            trip(withTrip(entry(1, rate = null, carbs = null))).single().description,
        )
    }

    private fun found(analysis: ProductAnalysis): FoundCardState? =
        factory.createFor(granted().copy(foundBarcode = BARCODE, foundAnalysis = analysis)).found

    private fun rated(rate: DietRate, mode: DietMode, reasons: List<RatingReason> = emptyList()) =
        ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(mode, rate, reasons, 57.5, null, 40),
        )

    private fun granted() = Prototype(cameraPermission = CameraPermission.Granted)

    private fun preview(isPaused: Boolean = false, torch: Torch = Torch.Unavailable, isTypeSheetOpen: Boolean = false) =
        CameraState.Preview(
            isPaused = isPaused,
            torch = torch,
            isTypeSheetOpen = isTypeSheetOpen,
            trip = persistentListOf(),
        )

    private fun camera(prototype: Prototype): CameraState = factory.createFor(prototype).camera

    private fun trip(prototype: Prototype): List<TripItemState> =
        assertIs<CameraState.Preview>(camera(prototype)).trip

    private fun withTrip(vararg entries: ProductAnalysisHistoryEntry, preferences: DietPreferences? = null) =
        granted().copy(tripEntries = entries.toList(), now = NOW, dietPreferences = preferences)

    private fun entry(
        id: Long,
        age: Duration = 10.minutes,
        name: String = "Name $id",
        rate: DietRate? = DietRate.GOOD,
        carbs: Double? = 3.6,
        category: ProductCategory = ProductCategory.OTHER,
    ) = ProductAnalysisHistoryEntry(
        id = id,
        productName = name,
        productBarcode = "barcode-$id",
        productBrand = "Brand",
        updateTime = NOW - age,
        dietRate = rate,
        carbsPer100g = carbs,
        category = category,
    )

    private companion object {
        const val BARCODE = "590"
        val NOW = Instant.parse("2026-10-30T12:00:00Z")

        val product = Product(
            name = "Nutella",
            barcode = BARCODE,
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.SPREADS,
        )
    }
}
