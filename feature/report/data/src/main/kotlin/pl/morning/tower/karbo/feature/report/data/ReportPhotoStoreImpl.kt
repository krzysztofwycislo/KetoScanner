package pl.morning.tower.karbo.feature.report.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.onFailure
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.report.api.ReportPhotoStore
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import java.io.File
import java.io.InputStream
import kotlin.uuid.Uuid

internal class ReportPhotoStoreImpl(
    private val directory: File,
    private val openSource: (String) -> InputStream?,
    private val dispatchers: AppDispatchers,
) : ReportPhotoStore {

    private val mutex = Mutex()

    override suspend fun importPhoto(sourceUri: String): Outcome<ReportPhoto> =
        withContext(dispatchers.io) {
            mutex.withLock {
                val target = File(directory, Uuid.random().toString())
                outcomeOf(mapError = { AppError.Storage }) {
                    directory.mkdirs()
                    val source = checkNotNull(openSource(sourceUri))
                    source.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    target.toPhoto()
                }.onFailure { target.delete() }
            }
        }

    override suspend fun delete(photos: List<ReportPhoto>): Outcome<Unit> =
        withContext(dispatchers.io) {
            mutex.withLock {
                outcomeOf(mapError = { AppError.Storage }) {
                    photos.mapNotNull { it.toStoredFile() }.forEach { it.delete() }
                }
            }
        }

    override suspend fun retainOnly(photos: List<ReportPhoto>): Outcome<List<ReportPhoto>> =
        withContext(dispatchers.io) {
            mutex.withLock {
                outcomeOf(mapError = { AppError.Storage }) {
                    val kept = photos.map { it.uri }.toSet()
                    directory.listFiles()
                        ?.filter { it.toPhoto().uri !in kept }
                        ?.forEach { it.delete() }
                    photos.filter { photo -> photo.toStoredFile()?.isFile == true }
                }
            }
        }

    private fun File.toPhoto(): ReportPhoto = ReportPhoto(FILE_SCHEME + absolutePath)

    private fun ReportPhoto.toStoredFile(): File? {
        if (!uri.startsWith(FILE_SCHEME)) return null
        val file = File(uri.removePrefix(FILE_SCHEME))
        return file.takeIf { it.parentFile?.absolutePath == directory.absolutePath }
    }

    private companion object {
        const val FILE_SCHEME = "file://"
    }
}
