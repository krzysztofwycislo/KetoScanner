package pl.morning.tower.karbo.feature.report.data

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.getOrThrow
import pl.morning.tower.karbo.core.testing.TestAppDispatchers
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ReportPhotoStoreImplTest {

    private val root = Files.createTempDirectory("report-photos-test").toFile()
    private val directory = File(root, "report-photos")
    private val sources = mutableMapOf<String, ByteArray>()

    @AfterTest
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun `when a photo is imported then its bytes are copied into a new file inside the directory`() = runTest {
        sources[SOURCE] = byteArrayOf(1, 2, 3)

        val photo = store().importPhoto(SOURCE).getOrThrow()

        assertTrue(photo.uri.startsWith("file://"))
        val file = photo.file()
        assertEquals(directory.absolutePath, file.parentFile?.absolutePath)
        assertContentEquals(byteArrayOf(1, 2, 3), file.readBytes())
    }

    @Test
    fun `when the same source is imported twice then two different files are created`() = runTest {
        sources[SOURCE] = byteArrayOf(1)
        val store = store()

        val first = store.importPhoto(SOURCE).getOrThrow()
        val second = store.importPhoto(SOURCE).getOrThrow()

        assertNotEquals(first, second)
        assertEquals(2, directory.listFiles()?.size)
    }

    @Test
    fun `when the source cannot be opened then import fails with storage and leaves no file`() = runTest {
        val outcome = store().importPhoto(SOURCE)

        assertEquals(Outcome.Failure(AppError.Storage), outcome)
        assertEquals(0, directory.listFiles()?.size ?: 0)
    }

    @Test
    fun `when a new photo is created then an empty file is created directly inside the directory`() = runTest {
        val photo = store().newPhoto().getOrThrow()

        assertTrue(photo.uri.startsWith("file://"))
        val file = photo.file()
        assertEquals(directory.absolutePath, file.parentFile?.absolutePath)
        assertTrue(file.isFile)
        assertEquals(0L, file.length())
    }

    @Test
    fun `when two new photos are created then they are different files`() = runTest {
        val store = store()

        val first = store.newPhoto().getOrThrow()
        val second = store.newPhoto().getOrThrow()

        assertNotEquals(first, second)
        assertEquals(2, directory.listFiles()?.size)
    }

    @Test
    fun `when a new photo is not retained then its file is deleted`() = runTest {
        val store = store()
        val photo = store.newPhoto().getOrThrow()

        val outcome = store.retainOnly(emptyList())

        assertEquals(Outcome.Success(emptyList()), outcome)
        assertFalse(photo.file().exists())
    }

    @Test
    fun `when photos are deleted then their files are removed and other uris and missing files are ignored`() =
        runTest {
            sources[SOURCE] = byteArrayOf(1)
            val store = store()
            val deleted = store.importPhoto(SOURCE).getOrThrow()
            val kept = store.importPhoto(SOURCE).getOrThrow()
            val outside = File(root, "outside").apply { writeBytes(byteArrayOf(9)) }
            val missing = ReportPhoto("file://" + File(directory, "missing").absolutePath)

            val outcome = store.delete(
                listOf(deleted, ReportPhoto("file://" + outside.absolutePath), missing, ReportPhoto(SOURCE)),
            )

            assertEquals(Outcome.Success(Unit), outcome)
            assertFalse(deleted.file().exists())
            assertTrue(kept.file().exists())
            assertTrue(outside.exists())
        }

    @Test
    fun `when only some photos are retained then other files are deleted and existing photos return in order`() =
        runTest {
            sources[SOURCE] = byteArrayOf(1)
            val store = store()
            val first = store.importPhoto(SOURCE).getOrThrow()
            val second = store.importPhoto(SOURCE).getOrThrow()
            val leftover = store.importPhoto(SOURCE).getOrThrow()
            val missing = ReportPhoto("file://" + File(directory, "missing").absolutePath)

            val outcome = store.retainOnly(listOf(second, missing, first))

            assertEquals(Outcome.Success(listOf(second, first)), outcome)
            assertFalse(leftover.file().exists())
            assertTrue(first.file().exists())
            assertTrue(second.file().exists())
        }

    @Test
    fun `when nothing was stored yet then retaining returns an empty list`() = runTest {
        val photo = ReportPhoto("file://" + File(directory, "missing").absolutePath)

        val outcome = store().retainOnly(listOf(photo))

        assertEquals(Outcome.Success(emptyList()), outcome)
    }

    private fun TestScope.store() = ReportPhotoStoreImpl(
        directory = directory,
        openSource = ::openSource,
        dispatchers = TestAppDispatchers(testScheduler),
    )

    private fun openSource(uri: String): InputStream? = sources[uri]?.let(::ByteArrayInputStream)

    private fun ReportPhoto.file(): File = File(uri.removePrefix("file://"))

    private companion object {
        const val SOURCE = "content://media/picker/0/42"
    }
}
