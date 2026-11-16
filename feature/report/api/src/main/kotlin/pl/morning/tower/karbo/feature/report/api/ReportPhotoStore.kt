package pl.morning.tower.karbo.feature.report.api

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto

interface ReportPhotoStore {
    suspend fun importPhoto(sourceUri: String): Outcome<ReportPhoto>

    suspend fun delete(photos: List<ReportPhoto>): Outcome<Unit>

    suspend fun retainOnly(photos: List<ReportPhoto>): Outcome<List<ReportPhoto>>
}
