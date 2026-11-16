package app.carbpeek

import android.app.Application
import app.carbpeek.core.database.databaseModule
import app.carbpeek.core.datastore.dataStoreModule
import app.carbpeek.di.appModules
import app.carbpeek.feature.about.ui.aboutUiModule
import app.carbpeek.feature.analysis.domain.analysisDomainModule
import app.carbpeek.feature.favourites.data.favouritesDataModule
import app.carbpeek.feature.favourites.ui.favouritesUiModule
import app.carbpeek.feature.history.data.historyDataModule
import app.carbpeek.feature.history.ui.historyUiModule
import app.carbpeek.feature.home.ui.homeUiModule
import app.carbpeek.feature.onboarding.ui.onboardingUiModule
import app.carbpeek.feature.preferences.data.preferencesDataModule
import app.carbpeek.feature.preferences.ui.preferencesUiModule
import app.carbpeek.feature.product.data.productDataModule
import app.carbpeek.feature.product.ui.productUiModule
import app.carbpeek.feature.report.data.reportDataModule
import app.carbpeek.feature.report.ui.reportUiModule
import app.carbpeek.feature.scanner.ui.scannerUiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


@Suppress("unused")
class CarbPeekApplication : Application() {

    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@CarbPeekApplication)
            modules(
                appModules,
                analysisDomainModule,
                databaseModule,
                dataStoreModule,
                historyDataModule,
                historyUiModule,
                homeUiModule,
                productDataModule,
                productUiModule,
                scannerUiModule,
                preferencesDataModule,
                preferencesUiModule,
                favouritesDataModule,
                favouritesUiModule,
                aboutUiModule,
                onboardingUiModule,
                reportDataModule,
                reportUiModule,
            )
        }
    }

}
