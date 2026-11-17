package app.carbpeek.server.db

import app.carbpeek.server.db.generated.app.tables.references.INSTALL
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GeneratedSchemaTest {

    private val database = PostgresTestDatabase.database

    @BeforeTest
    fun setUp() =
        runBlocking {
            PostgresTestDatabase.clean()
        }

    @Test
    fun `when an install is inserted through the generated table then it reads back`() =
        runBlocking {
            val id = UUID.randomUUID()

            database.transaction {
                insertInto(INSTALL)
                    .set(INSTALL.ID, id)
                    .set(INSTALL.APP_VERSION_CODE, 42)
                    .execute()
            }
            val record = database.transaction { fetchOne(INSTALL, INSTALL.ID.eq(id)) }

            val install = assertNotNull(record)
            assertNotNull(install.createdAt)
            assertNotNull(install.lastSeenAt)
            assertEquals(expected = id, actual = install.id)
            assertEquals(expected = 42, actual = install.appVersionCode)
        }
}
