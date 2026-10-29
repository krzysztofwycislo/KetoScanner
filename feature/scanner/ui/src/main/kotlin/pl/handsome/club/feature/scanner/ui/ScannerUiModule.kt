package pl.handsome.club.feature.scanner.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory

val scannerUiModule = module {
    viewModel {
        ScanViewModel(
            get(), get(), get(), get(), get(), get(), get(),
            initialQuery = if (BuildConfig.DEBUG) DEBUG_BARCODE else "",
        )
    }
    factoryOf(::ScanStateFactory)
    factoryOf(::ScanResources)
}

private const val DEBUG_BARCODE = "04963406"
