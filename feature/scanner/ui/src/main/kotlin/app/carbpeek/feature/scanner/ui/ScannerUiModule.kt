package app.carbpeek.feature.scanner.ui

import app.carbpeek.core.common.APPLICATION_SCOPE
import app.carbpeek.feature.scanner.ui.state.ScanStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val scannerUiModule = module {
    viewModel {
        ScanViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            applicationScope = get(named(APPLICATION_SCOPE)),
            initialQuery = if (BuildConfig.DEBUG) DEBUG_BARCODE else "",
        )
    }
    factoryOf(::ScanStateFactory)
    factoryOf(::ScanResources)
}

private const val DEBUG_BARCODE = "04963406"
