package app.carbpeek.feature.report.data

import androidx.core.net.toUri
import app.carbpeek.feature.report.api.ReportPhotoStore
import app.carbpeek.feature.report.api.ReportRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

val reportDataModule = module {
    singleOf(::MockReportRepository) bind ReportRepository::class
    single<ReportPhotoStore> {
        val context = androidContext()
        ReportPhotoStoreImpl(
            directory = File(context.filesDir, "report-photos"),
            openSource = { uri -> context.contentResolver.openInputStream(uri.toUri()) },
            dispatchers = get(),
        )
    }
}
