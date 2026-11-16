package app.carbpeek.server.db

import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL.table
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseTest {

    private val database = PostgresTestDatabase.database

    @BeforeTest
    fun setUp() =
        runBlocking {
            PostgresTestDatabase.clean()
        }

    @Test
    fun `when the block succeeds then the transaction commits`() =
        runBlocking {
            database.transaction { insertInstall() }

            assertEquals(expected = 1, actual = installCount())
        }

    @Test
    fun `when the block throws then the transaction rolls back and rethrows`() =
        runBlocking {
            assertFailsWith<IllegalStateException> {
                database.transaction {
                    insertInstall()
                    throw IllegalStateException("rollback")
                }
            }

            assertEquals(expected = 0, actual = installCount())
        }

    private fun DSLContext.insertInstall() {
        execute(
            "insert into app.install (id, app_version_code) values (?, ?)",
            UUID.randomUUID(),
            1,
        )
    }

    private suspend fun installCount(): Int = database.transaction { fetchCount(table("app.install")) }
}
