package app.carbpeek.server.db

import kotlinx.coroutines.runBlocking
import org.jooq.exception.DataAccessException
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private const val QUERY_CANCELED = "57014"

class QueryTimeoutTest {

    @Test
    fun `when a query runs too long then it is cancelled within 3 seconds and the connection can be used again`() =
        runBlocking {
            PostgresTestDatabase.dataSource.connection.use { connection ->
                val start = TimeSource.Monotonic.markNow()

                val error = assertFailsWith<SQLException> {
                    connection.createStatement().use { it.execute("select pg_sleep(10)") }
                }

                val duration = start.elapsedNow()
                assertEquals(expected = QUERY_CANCELED, actual = error.sqlState)
                assertTrue(actual = duration < 3.seconds, message = "took $duration")
                assertTrue(connection.createStatement().use { it.execute("select 1") })
            }
        }

    @Test
    fun `when a query in a transaction runs too long then the next transaction of the pool works`() =
        runBlocking {
            val database = PostgresTestDatabase.database
            val start = TimeSource.Monotonic.markNow()

            val error = assertFailsWith<DataAccessException> {
                database.transaction { execute("select pg_sleep(10)") }
            }

            val duration = start.elapsedNow()
            assertEquals(expected = QUERY_CANCELED, actual = error.sqlState())
            assertTrue(actual = duration < 3.seconds, message = "took $duration")
            repeat(5) {
                assertEquals(expected = 1, actual = database.transaction { fetchValue("select 1") })
            }
        }

    @Test
    fun `when the database stops answering then a query on an open connection fails within 6 seconds`() {
        startPostgresContainer().use { container ->
            container.createTestDataSource().use { dataSource ->
                val database = Database(dataSource)
                val dockerClient = container.dockerClient
                var start = TimeSource.Monotonic.markNow()
                try {
                    runBlocking {
                        assertFailsWith<DataAccessException> {
                            database.transaction {
                                execute("select 1")
                                dockerClient.pauseContainerCmd(container.containerId).exec()
                                start = TimeSource.Monotonic.markNow()
                                execute("select 1")
                            }
                        }
                    }
                    val duration = start.elapsedNow()
                    assertTrue(actual = duration < 6.seconds, message = "took $duration")
                    assertTrue(actual = duration > 4.seconds, message = "took $duration")
                } finally {
                    dockerClient.unpauseContainerCmd(container.containerId).exec()
                }
            }
        }
    }
}
