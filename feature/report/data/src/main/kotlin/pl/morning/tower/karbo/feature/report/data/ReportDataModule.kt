package pl.morning.tower.karbo.feature.report.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.report.api.ReportRepository

val reportDataModule = module {
    singleOf(::MockReportRepository) bind ReportRepository::class
}
