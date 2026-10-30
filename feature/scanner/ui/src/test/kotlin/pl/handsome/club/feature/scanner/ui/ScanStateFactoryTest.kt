package pl.handsome.club.feature.scanner.ui

import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.scanner.ui.state.CameraPermission
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.FoundCardState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import pl.handsome.club.feature.scanner.ui.state.Torch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
    fun `when an analysis with insufficient data is found then the card shows unknown with the insufficient texts`() {
        assertEquals(
            FoundCardState(
                name = UiText.Raw("Nutella"),
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

    private fun found(analysis: ProductAnalysis): FoundCardState? =
        factory.createFor(granted().copy(foundBarcode = BARCODE, foundAnalysis = analysis)).found

    private fun rated(rate: DietRate, mode: DietMode) = ProductAnalysis.Rated(
        product = product,
        macronutrients = MacronutrientAnalysisResult(mode, rate, emptyList(), 57.5, null, 40),
    )

    private fun granted() = Prototype(cameraPermission = CameraPermission.Granted)

    private fun preview(isPaused: Boolean = false, torch: Torch = Torch.Unavailable, isTypeSheetOpen: Boolean = false) =
        CameraState.Preview(isPaused = isPaused, torch = torch, isTypeSheetOpen = isTypeSheetOpen)

    private fun camera(prototype: Prototype): CameraState = factory.createFor(prototype).camera

    private companion object {
        const val BARCODE = "590"

        val product = Product(
            name = "Nutella",
            barcode = BARCODE,
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = null,
            servingAmount = null,
        )
    }
}
