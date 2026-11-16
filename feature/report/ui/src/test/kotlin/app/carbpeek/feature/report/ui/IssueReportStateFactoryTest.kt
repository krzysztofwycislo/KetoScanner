package app.carbpeek.feature.report.ui

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.capture.PhotoCaptureResources
import app.carbpeek.feature.report.ui.capture.state.CameraPermission
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCapturePrototype
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.state.IssueReportContentState
import app.carbpeek.feature.report.ui.state.IssueReportState
import app.carbpeek.feature.report.ui.state.IssueReportStateFactory
import app.carbpeek.feature.report.ui.state.PhotoThumbnailState
import app.carbpeek.feature.report.ui.state.RatedAsState
import app.carbpeek.feature.report.ui.state.ReportStatus
import app.carbpeek.feature.report.ui.state.SendButtonState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import app.carbpeek.feature.report.ui.state.IssueReportStatePrototype as Prototype

class IssueReportStateFactoryTest {

    private val factory = IssueReportStateFactory(
        resources = IssueReportResources(),
        captureStateFactory = PhotoCaptureStateFactory(PhotoCaptureResources()),
    )

    @Test
    fun `when no analysis yet then product summary shows the bag icon and the raw barcode only`() {
        val form = form(Prototype(barcode = Barcode(BARCODE)))

        assertEquals(AppIcons.Bag, form.product.icon)
        assertEquals(UiText.Raw(BARCODE), form.product.name)
        assertNull(form.product.subtitle)
    }

    @Test
    fun `when product name is blank then summary shows unnamed product`() {
        val form = form(prototype(analysis = rated(product.copy(name = " "))))

        assertEquals(UiText.Res(R.string.report_unnamed_product), form.product.name)
    }

    @Test
    fun `when brand is blank then subtitle is the raw barcode`() {
        val form = form(prototype(analysis = rated(product.copy(brand = ""))))

        assertEquals(UiText.Raw(BARCODE), form.product.subtitle)
    }

    @Test
    fun `when product has name and brand then summary shows category icon name and brand with barcode`() {
        val form = form(prototype(analysis = rated(product)))

        assertEquals(AppIcons.Can, form.product.icon)
        assertEquals(UiText.Raw("Coca-Cola Original Taste"), form.product.name)
        assertEquals(
            UiText.Res(R.string.report_product_subtitle, UiArg.Text("Coca-Cola"), UiArg.Text(BARCODE)),
            form.product.subtitle,
        )
    }

    @Test
    fun `when each reason is picked then the matching placeholder is shown`() {
        val expected = mapOf(
            IssueReason.RATING to R.string.report_placeholder_rating,
            IssueReason.NUTRITION to R.string.report_placeholder_nutrition,
            IssueReason.PRODUCT to R.string.report_placeholder_product,
            IssueReason.OTHER to R.string.report_placeholder_other,
        )

        expected.forEach { (reason, id) ->
            assertEquals(UiText.Res(id), form(prototype(reason = reason)).detailsPlaceholder)
        }
    }

    @Test
    fun `when no reason is picked then the generic placeholder is shown`() {
        assertEquals(UiText.Res(R.string.report_placeholder_other), form(prototype()).detailsPlaceholder)
    }

    @Test
    fun `when a reason is picked then four chips are shown with only that one selected`() {
        val reasons = form(prototype(reason = IssueReason.PRODUCT)).reasons

        assertEquals(IssueReason.entries, reasons.map { it.reason })
        assertEquals(listOf(false, false, true, false), reasons.map { it.isSelected })
        assertEquals(UiText.Res(R.string.report_reason_product), reasons[2].label)
    }

    @Test
    fun `when reason is not rating then expectation is absent`() {
        listOf(IssueReason.NUTRITION, IssueReason.PRODUCT, IssueReason.OTHER, null).forEach { reason ->
            assertNull(form(prototype(reason = reason, expectedRate = DietRate.GOOD)).expectation)
        }
    }

    @Test
    fun `when rating reason in keto mode then keto option labels are shown`() {
        val expectation = assertNotNull(form(prototype(reason = IssueReason.RATING)).expectation)

        assertEquals(DietRate.entries, expectation.options.map { it.rate })
        assertEquals(
            listOf(
                UiText.Res(R.string.report_verdict_good_keto),
                UiText.Res(R.string.report_verdict_neutral),
                UiText.Res(R.string.report_verdict_not_advised_keto),
            ),
            expectation.options.map { it.label },
        )
        assertEquals(listOf(Rating.Positive, Rating.Neutral, Rating.Negative), expectation.options.map { it.rating })
    }

    @Test
    fun `when rating reason in low carb mode then low carb option labels are shown`() {
        val expectation = assertNotNull(
            form(prototype(reason = IssueReason.RATING, dietMode = DietMode.LOW_CARB)).expectation,
        )

        assertEquals(
            listOf(
                UiText.Res(R.string.report_verdict_good_low_carb),
                UiText.Res(R.string.report_verdict_neutral),
                UiText.Res(R.string.report_verdict_not_advised_low_carb),
            ),
            expectation.options.map { it.label },
        )
    }

    @Test
    fun `when an expected rate is picked then only that option is selected`() {
        val expectation = assertNotNull(
            form(prototype(reason = IssueReason.RATING, expectedRate = DietRate.NEUTRAL)).expectation,
        )

        assertEquals(listOf(false, true, false), expectation.options.map { it.isSelected })
    }

    @Test
    fun `when product was rated then rated as shows its verdict`() {
        val expectation = assertNotNull(
            form(prototype(analysis = rated(product), reason = IssueReason.RATING)).expectation,
        )

        assertEquals(
            RatedAsState(label = UiText.Res(R.string.report_verdict_not_advised_keto), rating = Rating.Negative),
            expectation.ratedAs,
        )
    }

    @Test
    fun `when product has insufficient data then rated as is absent`() {
        val expectation = assertNotNull(
            form(
                prototype(analysis = ProductAnalysis.InsufficientData(product), reason = IssueReason.RATING),
            ).expectation,
        )

        assertNull(expectation.ratedAs)
    }

    @Test
    fun `when no reason is picked then send button asks to pick a reason and is disabled`() {
        assertEquals(
            SendButtonState(label = UiText.Res(R.string.report_pick_reason), isEnabled = false, isLoading = false),
            form(prototype()).sendButton,
        )
    }

    @Test
    fun `when a reason is picked then send button is enabled`() {
        assertEquals(
            SendButtonState(label = UiText.Res(R.string.report_send), isEnabled = true, isLoading = false),
            form(prototype(reason = IssueReason.OTHER)).sendButton,
        )
    }

    @Test
    fun `when sending then send button is loading and the form is not editable`() {
        val form = form(prototype(reason = IssueReason.OTHER, status = ReportStatus.SENDING))

        assertEquals(
            SendButtonState(label = UiText.Res(R.string.report_send), isEnabled = true, isLoading = true),
            form.sendButton,
        )
        assertFalse(form.isEditable)
    }

    @Test
    fun `when editing then the form is editable and shows the details`() {
        val form = form(prototype(details = "Recipe changed"))

        assertTrue(form.isEditable)
        assertEquals("Recipe changed", form.details)
    }

    @Test
    fun `when sent then sent content is shown`() {
        assertEquals(IssueReportContentState.Sent, state(prototype(status = ReportStatus.SENT)).content)
    }

    @Test
    fun `when failed then failed content is shown`() {
        assertEquals(
            IssueReportContentState.Failed,
            state(prototype(status = ReportStatus.FAILED)).content,
        )
    }

    @Test
    fun `when reason is rating then the photos row is absent`() {
        assertNull(form(prototype(reason = IssueReason.RATING, photos = photos(1))).photos)
    }

    @Test
    fun `when reason is not rating then the photos row is shown`() {
        listOf(IssueReason.NUTRITION, IssueReason.PRODUCT, IssueReason.OTHER, null).forEach { reason ->
            assertNotNull(form(prototype(reason = reason)).photos)
        }
    }

    @Test
    fun `when photos are attached then thumbnails are numbered from one with their labels`() {
        val row = assertNotNull(form(prototype(reason = IssueReason.NUTRITION, photos = photos(2))).photos)

        assertEquals(
            listOf(
                PhotoThumbnailState(
                    uri = "file:///photos/1",
                    contentDescription = UiText.Res(R.string.report_photo_attached, UiArg.Numeric.Count(1)),
                    removeLabel = UiText.Res(R.string.report_remove_photo, UiArg.Numeric.Count(1)),
                ),
                PhotoThumbnailState(
                    uri = "file:///photos/2",
                    contentDescription = UiText.Res(R.string.report_photo_attached, UiArg.Numeric.Count(2)),
                    removeLabel = UiText.Res(R.string.report_remove_photo, UiArg.Numeric.Count(2)),
                ),
            ),
            row.items,
        )
    }

    @Test
    fun `when fewer than three photos are attached then the add tile is shown and at three it is hidden`() {
        listOf(0, 1, 2).forEach { count ->
            assertEquals(true, form(prototype(photos = photos(count))).photos?.showAddTile)
        }
        assertEquals(false, form(prototype(photos = photos(3))).photos?.showAddTile)
    }

    @Test
    fun `when the capture is closed then the photo capture is absent`() {
        assertNull(state(prototype(photos = photos(1))).photoCapture)
    }

    @Test
    fun `when sending with an open capture then the photo capture is absent`() {
        val state = state(
            prototype = prototype(reason = IssueReason.OTHER, status = ReportStatus.SENDING),
            capture = openCapture,
        )

        assertNull(state.photoCapture)
    }

    @Test
    fun `when the capture is open then the counter counts the next photo after the attached photos`() {
        listOf(0, 2).forEach { attached ->
            val capture = assertNotNull(
                state(prototype = prototype(photos = photos(attached)), capture = openCapture).photoCapture,
            )

            assertEquals(
                UiText.Res(
                    R.string.report_photo_counter,
                    UiArg.Numeric.Count(attached + 1L),
                    UiArg.Numeric.Count(3),
                ),
                capture.counter,
            )
        }
    }

    @Test
    fun `when the reason is nutrition then the frame hint asks for the table`() {
        val capture = assertNotNull(
            state(prototype = prototype(reason = IssueReason.NUTRITION), capture = openCapture).photoCapture,
        )

        val camera = assertIs<CaptureStepState.Camera>(capture.step)
        assertEquals(UiText.Res(R.string.report_capture_frame_nutrition), camera.frameHint)
    }

    @Test
    fun `when the reason is product, other or none then the frame hint asks for the label`() {
        listOf(IssueReason.PRODUCT, IssueReason.OTHER, null).forEach { reason ->
            val capture = assertNotNull(
                state(prototype = prototype(reason = reason), capture = openCapture).photoCapture,
            )

            val camera = assertIs<CaptureStepState.Camera>(capture.step)
            assertEquals(UiText.Res(R.string.report_capture_frame_label), camera.frameHint)
        }
    }

    private fun state(
        prototype: Prototype,
        capture: PhotoCapturePrototype = PhotoCapturePrototype(),
    ): IssueReportState = factory.createFor(prototype = prototype, capture = capture)

    private fun form(prototype: Prototype): IssueReportContentState.Form =
        assertIs<IssueReportContentState.Form>(state(prototype).content)

    private companion object {
        const val BARCODE = "5449000000996"

        val openCapture = PhotoCapturePrototype(isOpen = true, cameraPermission = CameraPermission.Granted)

        val nutriments = ProductNutriments(
            energyPerServing = null,
            energyPer100g = null,
            fatsPerServing = null,
            fatsPer100g = null,
            proteinsPerServing = null,
            proteinsPer100g = null,
            carbohydratesPerServing = null,
            carbohydratesPer100g = 10.6,
            saltPerServing = null,
            saltPer100g = null,
        )

        val product = Product(
            name = "Coca-Cola Original Taste",
            barcode = BARCODE,
            brand = "Coca-Cola",
            nutriments = nutriments,
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.DRINKS,
        )

        fun rated(product: Product) = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                dietMode = DietMode.KETO,
                rate = DietRate.NOT_ADVISED,
                reasons = emptyList(),
                carbsPer100g = 10.6,
                dailyCarbConsumption = 40,
            ),
        )

        fun prototype(
            analysis: ProductAnalysis? = null,
            dietMode: DietMode = DietMode.KETO,
            reason: IssueReason? = null,
            details: String = "",
            expectedRate: DietRate? = null,
            status: ReportStatus = ReportStatus.EDITING,
            photos: List<ReportPhoto> = emptyList(),
        ) = Prototype(
            barcode = Barcode(BARCODE),
            analysis = analysis,
            dietMode = dietMode,
            reason = reason,
            details = details,
            expectedRate = expectedRate,
            status = status,
            photos = photos,
        )

        fun photos(count: Int) = (1..count).map { ReportPhoto("file:///photos/$it") }
    }
}
