package app.carbpeek.feature.report.ui.state

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.ui.IssueReportResources
import app.carbpeek.feature.report.ui.capture.state.PhotoCapturePrototype
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Failed
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Form
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Sent
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.report.ui.state.IssueReportStatePrototype as Prototype

internal class IssueReportStateFactory(
    private val resources: IssueReportResources,
    private val captureStateFactory: PhotoCaptureStateFactory,
) {

    fun createFor(prototype: Prototype, capture: PhotoCapturePrototype): IssueReportState = IssueReportState(
        content = prototype.content(),
        photoCapture = if (prototype.status == ReportStatus.EDITING) {
            captureStateFactory.createFor(capture = capture, request = prototype.captureRequest())
        } else {
            null
        },
    )

    private fun Prototype.content(): IssueReportContentState = when (status) {
        ReportStatus.SENT -> Sent
        ReportStatus.FAILED -> Failed
        ReportStatus.EDITING, ReportStatus.SENDING -> Form(
            product = productSummary(),
            reasons = reasons(),
            details = details,
            detailsPlaceholder = resources.detailsPlaceholder(reason),
            expectation = expectation(),
            photos = photosRow(),
            isEditable = status == ReportStatus.EDITING,
            sendButton = sendButton(),
        )
    }

    private fun Prototype.productSummary(): ProductSummaryState {
        val product = analysis?.product
        return when {
            product == null -> ProductSummaryState(
                icon = AppIcons.Bag,
                name = resources.barcode(barcode.value),
                subtitle = null,
            )
            product.brand.isBlank() -> ProductSummaryState(
                icon = product.category.icon,
                name = resources.productName(product.name),
                subtitle = resources.barcode(barcode.value),
            )
            else -> ProductSummaryState(
                icon = product.category.icon,
                name = resources.productName(product.name),
                subtitle = resources.productSubtitle(brand = product.brand, barcode = barcode.value),
            )
        }
    }

    private fun Prototype.reasons() = IssueReason.entries.map { option ->
        ReasonChipState(
            reason = option,
            label = resources.reasonLabel(option),
            isSelected = option == reason,
        )
    }.toPersistentList()

    private fun Prototype.expectation(): ExpectationState? = when (reason) {
        IssueReason.RATING -> ExpectationState(
            ratedAs = ratedAs(),
            options = DietRate.entries.map { rate ->
                ExpectedRateOptionState(
                    rate = rate,
                    label = resources.verdictLabel(rate = rate, mode = dietMode),
                    rating = rate.toRating(),
                    isSelected = rate == expectedRate,
                )
            }.toPersistentList(),
        )
        else -> null
    }

    private fun Prototype.ratedAs(): RatedAsState? = when (val analysis = analysis) {
        is ProductAnalysis.Rated -> RatedAsState(
            label = resources.verdictLabel(
                rate = analysis.macronutrients.rate,
                mode = analysis.macronutrients.dietMode,
            ),
            rating = analysis.macronutrients.rate.toRating(),
        )
        is ProductAnalysis.InsufficientData, null -> null
    }

    private fun Prototype.photosRow(): PhotosState? {
        if (!showsPhotos) return null
        return PhotosState(
            items = photos.mapIndexed { index, photo ->
                PhotoThumbnailState(
                    uri = photo.uri,
                    contentDescription = resources.photoAttached(index + 1),
                    removeLabel = resources.removePhoto(index + 1),
                )
            }.toPersistentList(),
            showAddTile = canAddPhoto,
        )
    }

    private fun Prototype.sendButton(): SendButtonState = when {
        status == ReportStatus.SENDING -> SendButtonState(
            label = resources.sendLabel(),
            isEnabled = true,
            isLoading = true,
        )
        reason == null -> SendButtonState(
            label = resources.pickReasonLabel(),
            isEnabled = false,
            isLoading = false,
        )
        else -> SendButtonState(
            label = resources.sendLabel(),
            isEnabled = true,
            isLoading = false,
        )
    }

    private fun DietRate.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
    }
}
