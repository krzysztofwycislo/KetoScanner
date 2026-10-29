package pl.handsome.club.ketoscanner

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pl.handsome.club.core.database.databaseModule
import pl.handsome.club.core.datastore.dataStoreModule
import pl.handsome.club.feature.about.ui.aboutUiModule
import pl.handsome.club.feature.analysis.domain.analysisDomainModule
import pl.handsome.club.feature.favourites.data.favouritesDataModule
import pl.handsome.club.feature.favourites.ui.favouritesUiModule
import pl.handsome.club.feature.history.data.historyDataModule
import pl.handsome.club.feature.history.ui.historyUiModule
import pl.handsome.club.feature.preferences.data.preferencesDataModule
import pl.handsome.club.feature.preferences.ui.preferencesUiModule
import pl.handsome.club.feature.product.data.productDataModule
import pl.handsome.club.feature.product.ui.productUiModule
import pl.handsome.club.feature.scanner.ui.scannerUiModule
import pl.handsome.club.ketoscanner.di.appModules


@Suppress("unused")
class KetoSApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@KetoSApplication)
            modules(
                appModules,
                analysisDomainModule,
                databaseModule,
                dataStoreModule,
                historyDataModule,
                historyUiModule,
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

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { get<OkHttpClient>() })) }
            .build()

}
