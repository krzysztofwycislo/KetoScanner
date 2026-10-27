package pl.handsome.club.ketoscanner

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pl.handsome.club.core.database.databaseModule
import pl.handsome.club.feature.analysis.domain.analysisDomainModule
import pl.handsome.club.feature.history.data.historyDataModule
import pl.handsome.club.feature.history.ui.historyUiModule
import pl.handsome.club.ketoscanner.di.appModules


@Suppress("unused")
class KetoSApplication : Application() {

    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@KetoSApplication)
            modules(appModules, analysisDomainModule, databaseModule, historyDataModule, historyUiModule)
        }
    }

}