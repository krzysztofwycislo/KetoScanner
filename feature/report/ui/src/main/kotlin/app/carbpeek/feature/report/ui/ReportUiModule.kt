package app.carbpeek.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.carbpeek.core.common.APPLICATION_SCOPE
import app.carbpeek.feature.report.ui.capture.PhotoCaptureResources
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.state.IssueReportStateFactory
import app.carbpeek.feature.report.ui.state.MissingProductStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val reportUiModule = module {
    viewModel {
        val savedStateHandle = get<SavedStateHandle>()
        IssueReportViewModel(
            route = savedStateHandle.toRoute<ReportRoute>(),
            savedStateHandle = savedStateHandle,
            analyzeProduct = get(),
            dietPreferences = get(),
            reports = get(),
            photoStore = get(),
            appEvents = get(),
            stateFactory = get(),
            captureResources = get(),
            errorReporter = get(),
            applicationScope = get(named(APPLICATION_SCOPE)),
        )
    }
    viewModel {
        val savedStateHandle = get<SavedStateHandle>()
        MissingProductViewModel(
            route = savedStateHandle.toRoute<ReportRoute>(),
            savedStateHandle = savedStateHandle,
            reports = get(),
            photoStore = get(),
            appEvents = get(),
            stateFactory = get(),
            captureResources = get(),
            errorReporter = get(),
            applicationScope = get(named(APPLICATION_SCOPE)),
        )
    }
    factoryOf(::IssueReportStateFactory)
    factoryOf(::IssueReportResources)
    factoryOf(::MissingProductStateFactory)
    factoryOf(::MissingProductResources)
    factoryOf(::PhotoCaptureStateFactory)
    factoryOf(::PhotoCaptureResources)
}
