package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.common.outcomeOf
import app.carbpeek.server.db.Database
import app.carbpeek.server.db.generated.products.tables.records.ProductRecord
import app.carbpeek.server.db.generated.products.tables.references.PRODUCT
import app.carbpeek.server.db.generated.products.tables.references.PRODUCT_MISSING
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

private val RETRY_AFTER = 30.seconds

internal class JooqProductRepository(
    private val database: Database,
) : ProductRepository {
    private val logger = LoggerFactory.getLogger(JooqProductRepository::class.java)

    override suspend fun find(barcode: String): Outcome<StoredLookup?> =
        transaction {
            findProduct(barcode) ?: findMissing(barcode)
        }

    override suspend fun saveFound(
        product: Product,
        fetchedAt: Instant,
    ): Outcome<Unit> =
        transaction {
            val record = product.toRecord(fetchedAt)
            insertInto(PRODUCT)
                .set(record)
                .onConflict(PRODUCT.BARCODE)
                .doUpdate()
                .set(record)
                .execute()
            deleteFrom(PRODUCT_MISSING)
                .where(PRODUCT_MISSING.BARCODE.eq(product.barcode))
                .execute()
        }

    override suspend fun saveMissing(
        barcode: String,
        checkedAt: Instant,
    ): Outcome<Unit> =
        transaction {
            insertInto(PRODUCT_MISSING)
                .set(PRODUCT_MISSING.BARCODE, barcode)
                .set(PRODUCT_MISSING.CHECKED_AT, checkedAt.toOffsetDateTime())
                .onConflict(PRODUCT_MISSING.BARCODE)
                .doUpdate()
                .set(PRODUCT_MISSING.CHECKED_AT, checkedAt.toOffsetDateTime())
                .execute()
            deleteFrom(PRODUCT)
                .where(PRODUCT.BARCODE.eq(barcode))
                .execute()
        }

    private suspend fun <T> transaction(block: DSLContext.() -> T): Outcome<T> =
        outcomeOf(
            mapError = { e ->
                logger.warn(
                    "Product query failed: {}, SQL state {}",
                    e::class.java.name,
                    e.sqlStateOrNull(),
                )
                ServerError.Unavailable(retryAfter = RETRY_AFTER)
            },
        ) {
            database.transaction(block)
        }
}

private fun Exception.sqlStateOrNull(): String? =
    when (this) {
        is DataAccessException -> sqlState()
        is SQLException -> sqlState
        else -> null
    }

private fun DSLContext.findProduct(barcode: String): StoredLookup? =
    fetchOne(PRODUCT, PRODUCT.BARCODE.eq(barcode))?.let { record ->
        StoredLookup.Found(
            product = record.toProduct(),
            fetchedAt = checkNotNull(record.fetchedAt).toInstant().toKotlinInstant(),
        )
    }

private fun DSLContext.findMissing(barcode: String): StoredLookup? =
    fetchOne(PRODUCT_MISSING, PRODUCT_MISSING.BARCODE.eq(barcode))?.let { record ->
        StoredLookup.Missing(checkedAt = checkNotNull(record.checkedAt).toInstant().toKotlinInstant())
    }

private fun Product.toRecord(fetchedAt: Instant): ProductRecord =
    ProductRecord(
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        servingQuantity = servingQuantity,
        category = category.name,
        labelLanguage = labelLanguage,
        ingredientsText = ingredientsText,
        ingredients = ingredients?.toJsonb(),
        nutriScore = nutriScore?.name,
        novaGroup = novaGroup?.toShort(),
        energyKcalPer_100g = nutrientsPer100g.energyKcal,
        energyKcalPerServing = nutrientsPerServing.energyKcal,
        fatPer_100g = nutrientsPer100g.fat,
        fatPerServing = nutrientsPerServing.fat,
        saturatedFatPer_100g = nutrientsPer100g.saturatedFat,
        saturatedFatPerServing = nutrientsPerServing.saturatedFat,
        carbohydratesPer_100g = nutrientsPer100g.carbohydrates,
        carbohydratesPerServing = nutrientsPerServing.carbohydrates,
        sugarsPer_100g = nutrientsPer100g.sugars,
        sugarsPerServing = nutrientsPerServing.sugars,
        fibrePer_100g = nutrientsPer100g.fibre,
        fibrePerServing = nutrientsPerServing.fibre,
        proteinsPer_100g = nutrientsPer100g.proteins,
        proteinsPerServing = nutrientsPerServing.proteins,
        saltPer_100g = nutrientsPer100g.salt,
        saltPerServing = nutrientsPerServing.salt,
        fetchedAt = fetchedAt.toOffsetDateTime(),
    )

private fun ProductRecord.toProduct(): Product =
    Product(
        barcode = checkNotNull(barcode),
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        servingQuantity = servingQuantity,
        category = ProductCategory.entries.firstOrNull { it.name == category } ?: ProductCategory.OTHER,
        labelLanguage = labelLanguage,
        ingredientsText = ingredientsText,
        ingredients = ingredients?.toIngredients(),
        nutriScore = NutriScore.entries.firstOrNull { it.name == nutriScore },
        novaGroup = novaGroup?.toInt(),
        nutrientsPer100g = Nutrients(
            energyKcal = energyKcalPer_100g,
            fat = fatPer_100g,
            saturatedFat = saturatedFatPer_100g,
            carbohydrates = carbohydratesPer_100g,
            sugars = sugarsPer_100g,
            fibre = fibrePer_100g,
            proteins = proteinsPer_100g,
            salt = saltPer_100g,
        ),
        nutrientsPerServing = Nutrients(
            energyKcal = energyKcalPerServing,
            fat = fatPerServing,
            saturatedFat = saturatedFatPerServing,
            carbohydrates = carbohydratesPerServing,
            sugars = sugarsPerServing,
            fibre = fibrePerServing,
            proteins = proteinsPerServing,
            salt = saltPerServing,
        ),
    )

private fun Instant.toOffsetDateTime(): OffsetDateTime = toJavaInstant().atOffset(ZoneOffset.UTC)

@Serializable
private data class StoredIngredient(
    val id: String,
    val name: String,
    val labelName: String,
    val declaredPercent: Double? = null,
    val subIngredients: List<StoredIngredient> = emptyList(),
)

private val storageJson = Json { ignoreUnknownKeys = true }

private fun List<Ingredient>.toJsonb(): JSONB =
    JSONB.valueOf(
        storageJson.encodeToString(
            serializer = ListSerializer(StoredIngredient.serializer()),
            value = map(Ingredient::toStored),
        ),
    )

private fun JSONB.toIngredients(): List<Ingredient> =
    storageJson
        .decodeFromString(
            deserializer = ListSerializer(StoredIngredient.serializer()),
            string = data(),
        ).map(StoredIngredient::toIngredient)

private fun Ingredient.toStored(): StoredIngredient =
    StoredIngredient(
        id = id,
        name = name,
        labelName = labelName,
        declaredPercent = declaredPercent,
        subIngredients = subIngredients.map(Ingredient::toStored),
    )

private fun StoredIngredient.toIngredient(): Ingredient =
    Ingredient(
        id = id,
        name = name,
        labelName = labelName,
        declaredPercent = declaredPercent,
        subIngredients = subIngredients.map(StoredIngredient::toIngredient),
    )
