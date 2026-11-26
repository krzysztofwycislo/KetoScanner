package app.carbpeek.server.config

import org.koin.core.module.Module
import org.koin.dsl.module

fun configModule(config: AppConfig): Module =
    module {
        single { config }
        single { config.database }
        single { config.meta }
        single { config.product }
        single { config.http }
    }
