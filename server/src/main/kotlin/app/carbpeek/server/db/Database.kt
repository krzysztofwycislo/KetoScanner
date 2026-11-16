package app.carbpeek.server.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

class Database(
    dataSource: DataSource,
) {
    private val dsl: DSLContext = DSL.using(dataSource, SQLDialect.POSTGRES)

    suspend fun <T> transaction(block: DSLContext.() -> T): T =
        withContext(Dispatchers.IO) {
            dsl.transactionResult { configuration -> configuration.dsl().block() }
        }
}
