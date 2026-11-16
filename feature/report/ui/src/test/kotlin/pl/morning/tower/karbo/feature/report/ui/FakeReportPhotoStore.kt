package pl.morning.tower.karbo.feature.report.ui

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.ReportPhotoStore
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto

internal class FakeReportPhotoStore : ReportPhotoStore {

    var importResult: Outcome<ReportPhoto>? = null
    var newPhotoResult: Outcome<ReportPhoto>? = null
    var retainResult: Outcome<List<ReportPhoto>>? = null
    var existing: Set<ReportPhoto>? = null
    val imported = mutableListOf<String>()
    val created = mutableListOf<ReportPhoto>()
    val deleted = mutableListOf<ReportPhoto>()
    val retained = mutableListOf<List<ReportPhoto>>()

    override suspend fun importPhoto(sourceUri: String): Outcome<ReportPhoto> {
        imported += sourceUri
        return importResult ?: Outcome.Success(ReportPhoto("file:///photos/${imported.size}"))
    }

    override suspend fun newPhoto(): Outcome<ReportPhoto> {
        newPhotoResult?.let { return it }
        val photo = ReportPhoto("file:///photos/new-${created.size + 1}")
        created += photo
        return Outcome.Success(photo)
    }

    override suspend fun delete(photos: List<ReportPhoto>): Outcome<Unit> {
        deleted += photos
        return Outcome.Success(Unit)
    }

    override suspend fun retainOnly(photos: List<ReportPhoto>): Outcome<List<ReportPhoto>> {
        retained += photos
        return retainResult ?: Outcome.Success(photos.filter { photo -> existing?.contains(photo) ?: true })
    }
}
