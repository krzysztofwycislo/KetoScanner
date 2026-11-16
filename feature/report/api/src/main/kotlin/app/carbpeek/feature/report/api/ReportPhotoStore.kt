package app.carbpeek.feature.report.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.report.api.model.ReportPhoto

interface ReportPhotoStore {
    suspend fun importPhoto(sourceUri: String): Outcome<ReportPhoto>

    suspend fun newPhoto(): Outcome<ReportPhoto>

    suspend fun delete(photos: List<ReportPhoto>): Outcome<Unit>

    suspend fun retainOnly(photos: List<ReportPhoto>): Outcome<List<ReportPhoto>>
}
