package app.carbpeek.server.db

import com.zaxxer.hikari.HikariDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import javax.sql.DataSource

val databaseModule =
    module {
        singleOf(::createDataSource) withOptions {
            bind<DataSource>()
            onClose { it?.close() }
        }
        single { Migrations(get()) }
        singleOf(::Database)
        singleOf(::JdbcDatabaseHealthCheck) { bind<DatabaseHealthCheck>() }
    }
