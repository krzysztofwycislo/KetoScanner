package app.carbpeek.server.db

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class JdbcDatabaseHealthCheckIntegrationTest {

    @Test
    fun `when the database is running then it reports up`() =
        runBlocking {
            val healthCheck = JdbcDatabaseHealthCheck(PostgresTestDatabase.dataSource)

            assertTrue(healthCheck.isUp())
        }
}
