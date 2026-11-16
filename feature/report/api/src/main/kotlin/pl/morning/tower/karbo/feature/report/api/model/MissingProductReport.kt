package pl.morning.tower.karbo.feature.report.api.model

import pl.morning.tower.karbo.core.model.Barcode

data class MissingProductReport(
    val barcode: Barcode,
    val front: ReportPhoto,
    val nutritionTable: ReportPhoto,
    val ingredients: ReportPhoto?,
)
