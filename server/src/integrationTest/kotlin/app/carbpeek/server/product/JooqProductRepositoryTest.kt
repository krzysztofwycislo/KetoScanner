package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.config.DatabaseConfig
import app.carbpeek.server.db.Database
import app.carbpeek.server.db.PostgresTestDatabase
import app.carbpeek.server.db.createDataSource
import app.carbpeek.server.db.generated.products.tables.references.PRODUCT
import app.carbpeek.server.db.generated.products.tables.references.PRODUCT_MISSING
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.measureTimedValue

private const val BARCODE = "3017620422003"

class JooqProductRepositoryTest {

    private val database = PostgresTestDatabase.database
    private val repository = JooqProductRepository(database)
    private val fetchedAt = Instant.parse("2026-11-24T12:00:00.123456Z")
    private val createdAt = OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC)

    private val noNutrients = Nutrients(
        energyKcal = null,
        fat = null,
        saturatedFat = null,
        carbohydrates = null,
        sugars = null,
        fibre = null,
        proteins = null,
        salt = null,
    )

    private val fullProduct = Product(
        barcode = BARCODE,
        name = "Nutella",
        brand = "Ferrero",
        imageUrl = "https://images.openfoodfacts.org/x.jpg",
        servingQuantity = "15",
        category = ProductCategory.SPREADS,
        labelLanguage = "pl",
        ingredientsText = "cukier, olej palmowy, czekolada mleczna 13% (masło)",
        ingredients = listOf(
            Ingredient(
                id = "en:sugar",
                name = "sugar",
                labelName = "cukier",
                declaredPercent = null,
                subIngredients = emptyList(),
            ),
            Ingredient(
                id = "en:milk-chocolate",
                name = "milk chocolate",
                labelName = "czekolada mleczna",
                declaredPercent = 13.5,
                subIngredients = listOf(
                    Ingredient(
                        id = "en:butter",
                        name = "butter",
                        labelName = "masło",
                        declaredPercent = null,
                        subIngredients = emptyList(),
                    ),
                ),
            ),
        ),
        nutriScore = NutriScore.E,
        novaGroup = 4,
        nutrientsPer100g = Nutrients(
            energyKcal = 539.5,
            fat = 30.9,
            saturatedFat = 10.6,
            carbohydrates = 57.5,
            sugars = 56.3,
            fibre = 3.4,
            proteins = 6.3,
            salt = 0.107,
        ),
        nutrientsPerServing = Nutrients(
            energyKcal = 80.8,
            fat = 4.64,
            saturatedFat = 1.59,
            carbohydrates = 8.62,
            sugars = 8.44,
            fibre = 0.525,
            proteins = 0.945,
            salt = 0.016,
        ),
    )

    private val minimalProduct = Product(
        barcode = BARCODE,
        name = null,
        brand = null,
        imageUrl = null,
        servingQuantity = null,
        category = ProductCategory.OTHER,
        labelLanguage = null,
        ingredientsText = null,
        ingredients = null,
        nutriScore = null,
        novaGroup = null,
        nutrientsPer100g = noNutrients,
        nutrientsPerServing = noNutrients,
    )

    @BeforeTest
    fun setUp() =
        runBlocking {
            PostgresTestDatabase.clean()
        }

    @Test
    fun `when nothing is stored then the lookup is empty`() =
        runBlocking {
            assertEquals(expected = Outcome.Success(null), actual = repository.find(BARCODE))
        }

    @Test
    fun `when a product with every field is saved then it reads back the same`() =
        runBlocking {
            repository.saveFound(product = fullProduct, fetchedAt = fetchedAt)

            assertEquals(expected = found(fullProduct), actual = repository.find(BARCODE))
        }

    @Test
    fun `when a product without optional fields is saved then it reads back the same`() =
        runBlocking {
            repository.saveFound(product = minimalProduct, fetchedAt = fetchedAt)

            assertEquals(expected = found(minimalProduct), actual = repository.find(BARCODE))
        }

    @Test
    fun `when a stored product is saved again then every field is replaced and created_at stays`() =
        runBlocking {
            repository.saveFound(product = fullProduct, fetchedAt = fetchedAt - 8.days)
            database.transaction { update(PRODUCT).set(PRODUCT.CREATED_AT, createdAt).execute() }

            val saved = repository.saveFound(product = minimalProduct, fetchedAt = fetchedAt)

            val storedCreatedAt = database.transaction {
                fetchOne(PRODUCT, PRODUCT.BARCODE.eq(BARCODE))?.createdAt
            }
            assertEquals(expected = Outcome.Success(Unit), actual = saved)
            assertEquals(expected = found(minimalProduct), actual = repository.find(BARCODE))
            assertEquals(expected = createdAt.toInstant(), actual = storedCreatedAt?.toInstant())
        }

    @Test
    fun `when a missing product is saved then it reads back as missing`() =
        runBlocking {
            val saved = repository.saveMissing(barcode = BARCODE, checkedAt = fetchedAt)

            assertEquals(expected = Outcome.Success(Unit), actual = saved)
            assertEquals(
                expected = Outcome.Success(StoredLookup.Missing(checkedAt = fetchedAt)),
                actual = repository.find(BARCODE),
            )
        }

    @Test
    fun `when a missing product is saved again then the time is replaced and created_at stays`() =
        runBlocking {
            repository.saveMissing(barcode = BARCODE, checkedAt = fetchedAt - 2.days)
            database.transaction { update(PRODUCT_MISSING).set(PRODUCT_MISSING.CREATED_AT, createdAt).execute() }

            repository.saveMissing(barcode = BARCODE, checkedAt = fetchedAt)

            val storedCreatedAt = database.transaction {
                fetchOne(PRODUCT_MISSING, PRODUCT_MISSING.BARCODE.eq(BARCODE))?.createdAt
            }
            assertEquals(
                expected = Outcome.Success(StoredLookup.Missing(checkedAt = fetchedAt)),
                actual = repository.find(BARCODE),
            )
            assertEquals(expected = createdAt.toInstant(), actual = storedCreatedAt?.toInstant())
        }

    @Test
    fun `when a missing product is found then the missing row is removed`() =
        runBlocking {
            repository.saveMissing(barcode = BARCODE, checkedAt = fetchedAt)

            repository.saveFound(product = fullProduct, fetchedAt = fetchedAt)

            val missingRows = database.transaction { fetchCount(PRODUCT_MISSING) }
            assertEquals(expected = 0, actual = missingRows)
            assertEquals(expected = found(fullProduct), actual = repository.find(BARCODE))
        }

    @Test
    fun `when a stored product becomes missing then only its own row is removed`() =
        runBlocking {
            val other = minimalProduct.copy(barcode = "04963406")
            repository.saveFound(product = fullProduct, fetchedAt = fetchedAt)
            repository.saveFound(product = other, fetchedAt = fetchedAt)

            repository.saveMissing(barcode = BARCODE, checkedAt = fetchedAt)

            assertEquals(
                expected = Outcome.Success(StoredLookup.Missing(checkedAt = fetchedAt)),
                actual = repository.find(BARCODE),
            )
            assertEquals(expected = found(other), actual = repository.find(other.barcode))
        }

    @Test
    fun `when a stored category or Nutri-Score is not known then the product still reads`() =
        runBlocking {
            repository.saveFound(product = fullProduct, fetchedAt = fetchedAt)
            database.transaction {
                update(PRODUCT)
                    .set(PRODUCT.CATEGORY, "ICE_CREAM")
                    .set(PRODUCT.NUTRI_SCORE, "F")
                    .execute()
            }

            assertEquals(
                expected = found(fullProduct.copy(category = ProductCategory.OTHER, nutriScore = null)),
                actual = repository.find(BARCODE),
            )
        }

    @Test
    fun `when the database is unreachable then every call is unavailable within 3 seconds`() {
        val config = DatabaseConfig(
            url = "jdbc:postgresql://127.0.0.1:1/none",
            user = "none",
            password = "none",
            poolSize = 1,
        )
        createDataSource(config).use { dataSource ->
            val unreachable = JooqProductRepository(Database(dataSource))

            runBlocking {
                listOf<suspend () -> Outcome<*>>(
                    { unreachable.find(BARCODE) },
                    { unreachable.saveFound(product = fullProduct, fetchedAt = fetchedAt) },
                    { unreachable.saveMissing(barcode = BARCODE, checkedAt = fetchedAt) },
                ).forEach { call ->
                    val (outcome, duration) = measureTimedValue { call() }

                    assertIs<ServerError.Unavailable>(assertIs<Outcome.Failure>(outcome).error)
                    assertTrue(actual = duration < 3.seconds, message = "took $duration")
                }
            }
        }
    }

    private fun found(product: Product): Outcome<StoredLookup?> =
        Outcome.Success(StoredLookup.Found(product = product, fetchedAt = fetchedAt))
}
