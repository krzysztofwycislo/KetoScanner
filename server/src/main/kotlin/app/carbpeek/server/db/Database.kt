package app.carbpeek.server.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

internal class Database(
    private val dataSource: DataSource,
) {
    suspend fun <T> transaction(block: DSLContext.() -> T): T =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                DSL
                    .using(connection, SQLDialect.POSTGRES)
                    .transactionResult { configuration -> configuration.dsl().block() }
            }
        }
}
