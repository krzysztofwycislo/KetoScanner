package pl.morning.tower.karbo.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportStateFactory

val reportUiModule = module {
    viewModel {
        IssueReportViewModel(
            get<SavedStateHandle>().toRoute<ReportRoute>(),
            get(), get(), get(), get(), get(), get(), get(),
        )
    }
    factoryOf(::IssueReportStateFactory)
    factoryOf(::IssueReportResources)
}
