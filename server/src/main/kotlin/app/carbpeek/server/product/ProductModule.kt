package app.carbpeek.server.product

import app.carbpeek.server.config.MetaConfig
import app.carbpeek.server.config.ProductConfig
import app.carbpeek.server.product.openfoodfacts.OpenFoodFactsSource
import app.carbpeek.server.product.openfoodfacts.createOpenFoodFactsHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.TimeSource

val productModule =
    module {
        single { CoroutineScope(SupervisorJob() + Dispatchers.Default) } withOptions {
            onClose { it?.cancel() }
        }
        single {
            createOpenFoodFactsHttpClient(
                engine = Java.create(),
                serverVersion = get<MetaConfig>().serverVersion,
            )
        } withOptions {
            onClose { it?.closeWithEngine() }
        }
        single<Clock> { Clock.System }
        single<TimeSource.WithComparableMarks> { TimeSource.Monotonic }
        single { RequestBudget(limit = get<ProductConfig>().offRequestsPerMinute, timeSource = get()) }
        singleOf(::LookupStats)
        single { LookupStatsLogger(get()) }
        singleOf(::JooqProductRepository) { bind<ProductRepository>() }
        singleOf(::OpenFoodFactsSource) { bind<ProductSource>() }
        singleOf(::ProductLookupImpl) { bind<ProductLookup>() }
    }

private fun HttpClient.closeWithEngine() {
    close()
    engine.close()
}
