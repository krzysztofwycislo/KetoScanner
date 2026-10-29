package pl.handsome.club.ketoscanner.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
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
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit
import kotlin.time.Clock


private const val OPEN_FOOD_FACTS_CONTACT = "https://github.com/krzysztofwycislo/KetoScanner"

val appModules = module {
    listOf(
        // coroutines
        single<AppDispatchers> { DefaultAppDispatchers },
        single<CoroutineScope>(named(APPLICATION_SCOPE)) { CoroutineScope(SupervisorJob() + get<AppDispatchers>().default) },

        // time
        single<Clock> { Clock.System },

        // logging
        single<AppLogger> { AndroidAppLogger },
        single<ErrorReporter> { LoggingErrorReporter(get()) },

        // events
        single { AppEvents(CoroutineScope(SupervisorJob())) },

        // network
        single { createOpenFoodFactsMoshi() },
        single {
            createOpenFoodFactsOkHttpClient(
                userAgent = "KetoScanner/${BuildConfig.VERSION_NAME} ($OPEN_FOOD_FACTS_CONTACT)",
                enableBodyLogging = BuildConfig.DEBUG
            )
        },
        single { createOpenFoodFactsRetrofit(OPEN_FOOD_FACTS_BASE_URL, get(), get()) },
        single { createOpenFoodFactsApi(get()) },

        // repository
        single<ProductNetworkSource> { OpenFoodFactsProductSource(get(), get()) },
    )
}
