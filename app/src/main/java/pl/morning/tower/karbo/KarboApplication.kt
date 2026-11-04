package pl.morning.tower.karbo

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pl.morning.tower.karbo.core.database.databaseModule
import pl.morning.tower.karbo.core.datastore.dataStoreModule
import pl.morning.tower.karbo.feature.about.ui.aboutUiModule
import pl.morning.tower.karbo.feature.analysis.domain.analysisDomainModule
import pl.morning.tower.karbo.feature.favourites.data.favouritesDataModule
import pl.morning.tower.karbo.feature.favourites.ui.favouritesUiModule
import pl.morning.tower.karbo.feature.history.data.historyDataModule
import pl.morning.tower.karbo.feature.history.ui.historyUiModule
import pl.morning.tower.karbo.feature.home.ui.homeUiModule
import pl.morning.tower.karbo.feature.preferences.data.preferencesDataModule
import pl.morning.tower.karbo.feature.preferences.ui.preferencesUiModule
import pl.morning.tower.karbo.feature.product.data.productDataModule
import pl.morning.tower.karbo.feature.product.ui.productUiModule
import pl.morning.tower.karbo.feature.scanner.ui.scannerUiModule
import pl.morning.tower.karbo.di.appModules


@Suppress("unused")
class KarboApplication : Application() {

    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@KarboApplication)
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
            )
        }
    }

}
