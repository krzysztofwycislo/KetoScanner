package pl.handsome.club.ketoscanner.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
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
import pl.handsome.club.openfoodapi.OpenFoodFactsProductSource
import pl.handsome.club.openfoodapi.ProductNetworkSource
import pl.handsome.club.openfoodapi.api.OPEN_FOOD_FACTS_BASE_URL
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsImageOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit
import kotlin.time.Clock


private const val OPEN_FOOD_FACTS_CONTACT = "https://github.com/krzysztofwycislo/KetoScanner"
const val IMAGE_HTTP_CLIENT = "imageHttpClient"

val appModules = module {
    // coroutines
    single<AppDispatchers> { DefaultAppDispatchers }
    single<CoroutineScope>(named(APPLICATION_SCOPE)) { CoroutineScope(SupervisorJob() + get<AppDispatchers>().default) }

    // time
    single<Clock> { Clock.System }

    // logging
    single<AppLogger> { AndroidAppLogger }
    singleOf(::LoggingErrorReporter) bind ErrorReporter::class

    // events
    single { AppEvents(get(named(APPLICATION_SCOPE))) }

    // network
    single { createOpenFoodFactsMoshi() }
    single {
        createOpenFoodFactsOkHttpClient(
            userAgent = "KetoScanner/${BuildConfig.VERSION_NAME} ($OPEN_FOOD_FACTS_CONTACT)",
            enableBodyLogging = BuildConfig.DEBUG
        )
    }
    single(named(IMAGE_HTTP_CLIENT)) { createOpenFoodFactsImageOkHttpClient(get()) }
    single { createOpenFoodFactsRetrofit(OPEN_FOOD_FACTS_BASE_URL, get(), get()) }
    single { createOpenFoodFactsApi(get()) }

    // repository
    singleOf(::OpenFoodFactsProductSource) bind ProductNetworkSource::class
}
