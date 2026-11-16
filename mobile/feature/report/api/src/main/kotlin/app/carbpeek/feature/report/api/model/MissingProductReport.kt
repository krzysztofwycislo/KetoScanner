package app.carbpeek.feature.report.api.model

import app.carbpeek.core.model.Barcode

data class MissingProductReport(
    val barcode: Barcode,
    val front: ReportPhoto,
    val nutritionTable: ReportPhoto,
    val ingredients: ReportPhoto?,
)
