package app.carbpeek.di

import app.carbpeek.BuildConfig
import app.carbpeek.core.common.APPLICATION_SCOPE
import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppLogger
import app.carbpeek.core.common.DefaultAppDispatchers
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.LoggingErrorReporter
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.openfoodapi.OpenFoodFactsProductSource
import app.carbpeek.openfoodapi.ProductNetworkSource
import app.carbpeek.openfoodapi.api.OpenFoodFactsApi
import app.carbpeek.openfoodapi.api.createOpenFoodFactsHttpClient
import app.carbpeek.openfoodapi.api.createOpenFoodFactsImageHttpClient
import app.carbpeek.ui.AppViewModel
import app.carbpeek.util.AndroidAppLogger
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock


private const val OPEN_FOOD_FACTS_APP_NAME = "CarbPeek"
private const val OPEN_FOOD_FACTS_CONTACT = "support@carbpeek.app"
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
    single<HttpClientEngine> { OkHttp.create() }
    single {
        createOpenFoodFactsHttpClient(
            engine = get(),
            appName = OPEN_FOOD_FACTS_APP_NAME,
            appVersion = BuildConfig.VERSION_NAME,
            contact = OPEN_FOOD_FACTS_CONTACT,
            enableBodyLogging = BuildConfig.DEBUG,
        )
    }
    single(named(IMAGE_HTTP_CLIENT)) {
        createOpenFoodFactsImageHttpClient(
            engine = get(),
            appName = OPEN_FOOD_FACTS_APP_NAME,
            appVersion = BuildConfig.VERSION_NAME,
            contact = OPEN_FOOD_FACTS_CONTACT,
        )
    }
    single { OpenFoodFactsApi(get()) }

    // image loading
    single {
        ImageLoader.Builder(androidContext())
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { get<HttpClient>(named(IMAGE_HTTP_CLIENT)) }))
            }
            .build()
    }

    // shell
    viewModelOf(::AppViewModel)

    // repository
    singleOf(::OpenFoodFactsProductSource) bind ProductNetworkSource::class
}
