package pl.handsome.club.ketoscanner.di

import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.handsome.club.core.common.APPLICATION_SCOPE
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppLogger
import pl.handsome.club.core.common.DefaultAppDispatchers
import pl.handsome.club.core.common.ErrorReporter
import pl.handsome.club.core.common.LoggingErrorReporter
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.ketoscanner.util.AndroidAppLogger
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.ui.AppViewModel
import pl.handsome.club.openfoodapi.OpenFoodFactsProductSource
import pl.handsome.club.openfoodapi.ProductNetworkSource
import pl.handsome.club.openfoodapi.api.OPEN_FOOD_FACTS_BASE_URL
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsImageOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit
import kotlin.time.Clock


private const val OPEN_FOOD_FACTS_APP_NAME = "KetoScanner"
private const val OPEN_FOOD_FACTS_CONTACT = "https://github.com/krzysztofwycislo/KetoScanner"
private const val IMAGE_HTTP_CLIENT = "imageHttpClient"

val appModules = module {
    // coroutines
    single<AppDispatchers> { DefaultAppDispatchers }
    single<CoroutineScope>(named(APPLICATION_SCOPE)) { CoroutineScope(SupervisorJob() + get<AppDispatchers>().default) }

    // time
    single<Clock> { Clock.System }
    factory<TimeZone> { TimeZone.currentSystemDefault() }

    // logging
    single<AppLogger> { AndroidAppLogger }
    singleOf(::LoggingErrorReporter) bind ErrorReporter::class

    // events
    single { AppEvents(get(named(APPLICATION_SCOPE))) }

    // network
    single { createOpenFoodFactsMoshi() }
    single {
        createOpenFoodFactsOkHttpClient(
            appName = OPEN_FOOD_FACTS_APP_NAME,
            appVersion = BuildConfig.VERSION_NAME,
            contact = OPEN_FOOD_FACTS_CONTACT,
            enableBodyLogging = BuildConfig.DEBUG,
        )
    }
    single(named(IMAGE_HTTP_CLIENT)) { createOpenFoodFactsImageOkHttpClient(get()) }
    single { createOpenFoodFactsRetrofit(OPEN_FOOD_FACTS_BASE_URL, get(), get()) }
    single { createOpenFoodFactsApi(get()) }

    // image loading
    single {
        ImageLoader.Builder(androidContext())
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { get<OkHttpClient>(named(IMAGE_HTTP_CLIENT)) }))
            }
            .build()
    }

    // shell
    viewModelOf(::AppViewModel)

    // repository
    singleOf(::OpenFoodFactsProductSource) bind ProductNetworkSource::class
}
