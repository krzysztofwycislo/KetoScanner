package app.carbpeek.server

import app.carbpeek.server.config.AppConfig
import app.carbpeek.server.config.HttpConfig
import app.carbpeek.server.config.MetaConfig
import app.carbpeek.server.config.configModule
import app.carbpeek.server.db.DatabaseHealthCheck
import app.carbpeek.server.db.Migrations
import app.carbpeek.server.db.databaseModule
import app.carbpeek.server.http.healthRoutes
import app.carbpeek.server.http.installErrorHandling
import app.carbpeek.server.http.installRateLimiting
import app.carbpeek.server.http.installRequestId
import app.carbpeek.server.http.metaRoutes
import app.carbpeek.server.http.productRoutes
import app.carbpeek.server.product.LookupStatsLogger
import app.carbpeek.server.product.ProductLookup
import app.carbpeek.server.product.productModule
import app.carbpeek.shared.contract.ApiJson
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    val config = AppConfig.fromEnvironment(System.getenv())
    embeddedServer(
        factory = Netty,
        port = config.port,
        host = "0.0.0.0",
    ) {
        serverModule(config)
    }.start(wait = true)
}

fun Application.serverModule(config: AppConfig) {
    module(listOf(configModule(config), databaseModule, productModule))
    get<Migrations>().migrate()
    val statsLogger = get<LookupStatsLogger>()
    get<CoroutineScope>().launch { statsLogger.run() }
}

internal fun Application.module(koinModules: List<Module>) {
    install(Koin) {
        slf4jLogger()
        modules(koinModules)
    }
    installRequestId()
    installErrorHandling()
    install(ContentNegotiation) {
        json(ApiJson)
    }
    installRateLimiting(get<HttpConfig>())
    val healthCheck = get<DatabaseHealthCheck>()
    val meta = get<MetaConfig>()
    val productLookup = get<ProductLookup>()
    routing {
        healthRoutes(healthCheck)
        metaRoutes(meta)
        productRoutes(productLookup)
    }
}
