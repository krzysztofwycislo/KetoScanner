package app.carbpeek.openfoodapi

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.DefaultAppDispatchers
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.api.model.Ingredient
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.api.OpenFoodFactsApi
import app.carbpeek.openfoodapi.api.createOpenFoodFactsHttpClient
import app.carbpeek.openfoodapi.api.createOpenFoodFactsImageHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.SocketTimeoutException
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.cancel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds


private const val TEST_USER_AGENT = "CarbPeek/test (contact)"

class OpenFoodFactsProductSourceTest {

    private val requests = mutableListOf<HttpRequestData>()
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respondJson("") }

    private val mockEngine = MockEngine { request ->
        requests += request
        handler(request)
    }

    private val source = OpenFoodFactsProductSource(OpenFoodFactsApi(apiClient()), DefaultAppDispatchers)


    @Test
    fun `found product is parsed into success`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_found.json"))

        val product = successValue(source.getProduct("04963406"))

        assertEquals("Coke Original Taste", product.name)
        assertEquals("04963406", product.barcode)
        assertEquals("Coke", product.brand)
        assertEquals("355", product.servingAmount)
        assertEquals(39.4366, assertNotNull(product.nutriments.energyPer100g), absoluteTolerance = 1e-4)
        assertEquals(10.9859, assertNotNull(product.nutriments.carbohydratesPer100g), absoluteTolerance = 1e-4)
        assertEquals(39.0, product.nutriments.carbohydratesPerServing)
        assertEquals(0.0317, product.nutriments.saltPerServing)
        assertEquals(10.6, product.nutriments.sugarsPer100g)
        assertEquals(37.6, product.nutriments.sugarsPerServing)
        assertEquals(0.0, product.nutriments.fibrePer100g)
        assertEquals(0.0, product.nutriments.fibrePerServing)
        assertEquals(ProductCategory.DRINKS, product.category)
    }

    @Test
    fun `ingredients nutri score and nova group are parsed`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_with_ingredients.json"))

        val product = successValue(source.getProduct("3017620422003"))

        assertEquals(
            listOf(
                ingredient(id = "en:sugar", name = "Sugar"),
                ingredient(id = "en:palm-oil", name = "Palm oil"),
                ingredient(id = "en:hazelnut", name = "Hazelnuts", percent = 13.0),
                ingredient(id = "en:skimmed-milk-powder", name = "Skimmed milk powder", percent = 8.7),
                ingredient(
                    id = "en:milk-chocolate",
                    name = "Milk chocolate",
                    sub = listOf(
                        ingredient(id = "en:sugar", name = "sugar"),
                        ingredient(id = "en:cocoa-butter", name = "cocoa butter"),
                    ),
                ),
                ingredient(id = "en:e322", name = "Lecithins"),
            ),
            product.ingredients,
        )
        assertEquals(NutriScoreGrade.E, product.nutriScore)
        assertEquals(NovaGroup.ULTRA_PROCESSED, product.novaGroup)
    }

    @Test
    fun `quoted nova group is parsed`() = runTest {
        respondWith(
            HttpStatusCode.OK,
            """{"code":"1","product":{"nova_group":"3","nutriscore_grade":"unknown"},"status":1}""",
        )

        val product = successValue(source.getProduct("1"))

        assertEquals(NovaGroup.PROCESSED, product.novaGroup)
        assertNull(product.nutriScore)
    }

    @Test
    fun `product without ingredients and scores has them null`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_found.json"))

        val product = successValue(source.getProduct("04963406"))

        assertNull(product.ingredients)
        assertNull(product.nutriScore)
        assertNull(product.novaGroup)
    }

    @Test
    fun `http 404 is mapped to not found`() = runTest {
        respondWith(HttpStatusCode.NotFound, fixture("product_not_found.json"))

        val result = source.getProduct("5900000000017")

        assertEquals(Outcome.Failure(AppError.NotFound), result)
    }

    @Test
    fun `invalid code response without product is mapped to not found`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("invalid_code.json"))

        val result = source.getProduct("asd")

        assertEquals(Outcome.Failure(AppError.NotFound), result)
    }

    @Test
    fun `product without name nutriments and serving is parsed into success`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_minimal.json"))

        val product = successValue(source.getProduct("3017620422003"))

        assertTrue(product.name.isEmpty())
        assertEquals(ProductNutriments(null, null, null, null, null, null, null, null, null, null), product.nutriments)
        assertNull(product.servingAmount)
        assertEquals(ProductCategory.OTHER, product.category)
    }

    @Test
    fun `server error is mapped to http failure with its code`() = runTest {
        respondWith(HttpStatusCode.InternalServerError, "")

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Http(500)), result)
    }

    @Test
    fun `malformed json is mapped to parsing failure`() = runTest {
        respondWith(HttpStatusCode.OK, "not json")

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Parsing), result)
    }

    @Test
    fun `json with a wrong field type is mapped to parsing failure`() = runTest {
        respondWith(HttpStatusCode.OK, """{"status":"one","code":"04963406"}""")

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Parsing), result)
    }

    @Test
    fun `io failure is mapped to network failure`() = runTest {
        handler = { throw IOException("connection reset") }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Network), result)
    }

    @Test
    fun `request timeout is mapped to timeout failure`() = runTest {
        handler = { request -> throw HttpRequestTimeoutException(request) }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Timeout), result)
    }

    @Test
    fun `connect timeout is mapped to timeout failure`() = runTest {
        handler = { request -> throw ConnectTimeoutException(request) }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Timeout), result)
    }

    @Test
    fun `socket timeout is mapped to timeout failure`() = runTest {
        handler = { request -> throw SocketTimeoutException(request) }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Timeout), result)
    }

    @Test
    fun `answer whose body stalls is mapped to timeout failure`() = runTest {
        handler = { request ->
            respond(
                content = ByteChannel().apply { cancel(SocketTimeoutException(request)) },
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Timeout), result)
    }

    @Test
    fun `request that never answers is mapped to timeout failure`() = runTest {
        handler = { awaitCancellation() }
        val impatientClient = createOpenFoodFactsHttpClient(
            engine = mockEngine,
            appName = "CarbPeek",
            appVersion = "test",
            contact = "contact",
            enableBodyLogging = false,
            requestTimeout = 100.milliseconds,
        )
        val impatientSource = OpenFoodFactsProductSource(
            openFoodFactsApi = OpenFoodFactsApi(impatientClient),
            dispatchers = DefaultAppDispatchers,
        )

        val result = impatientSource.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Timeout), result)
    }

    @Test
    fun `unknown host is mapped to network failure`() = runTest {
        handler = { throw UnknownHostException("no address") }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Network), result)
    }

    @Test
    fun `refused connection is mapped to network failure`() = runTest {
        handler = { throw ConnectException("refused") }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Network), result)
    }

    @Test
    fun `answer whose body is cut is mapped to network failure`() = runTest {
        handler = {
            respond(
                content = ByteChannel().apply { cancel(IOException("connection reset")) },
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val result = source.getProduct("04963406")

        assertEquals(Outcome.Failure(AppError.Network), result)
    }

    @Test
    fun `request uses world host v2 path fields filter and user agent`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_found.json"))

        source.getProduct("04963406")

        val request = requests.single()
        assertEquals(URLProtocol.HTTPS, request.url.protocol)
        assertEquals("world.openfoodfacts.org", request.url.host)
        assertEquals("/api/v2/product/04963406", request.url.encodedPath)
        assertEquals(
            "product_name,brands,image_url,serving_quantity,nutriments,categories_tags,ingredients," +
                "ingredients_text,nutriscore_grade,nova_group,lang",
            request.url.parameters["fields"]
        )
        assertEquals("pl", request.url.parameters["lc"])
        assertEquals(TEST_USER_AGENT, request.headers[HttpHeaders.UserAgent])
    }

    @Test
    fun `api client has default timeouts`() = runTest {
        respondWith(HttpStatusCode.OK, fixture("product_found.json"))

        source.getProduct("04963406")

        val timeouts = assertNotNull(requests.single().getCapabilityOrNull(HttpTimeoutCapability))
        assertEquals(10_000L, timeouts.connectTimeoutMillis)
        assertEquals(20_000L, timeouts.socketTimeoutMillis)
        assertEquals(25_000L, timeouts.requestTimeoutMillis)
    }

    @Test
    fun `image client keeps user agent without request timeout`() = runTest {
        val imageRequests = mutableListOf<HttpRequestData>()
        val imageEngine = MockEngine { request ->
            imageRequests += request
            respond(ByteArray(0), HttpStatusCode.OK)
        }
        val imageClient = createOpenFoodFactsImageHttpClient(imageEngine, "CarbPeek", "test", "contact")

        imageClient.get("https://images.openfoodfacts.org/image.jpg")

        val request = imageRequests.single()
        val timeouts = assertNotNull(request.getCapabilityOrNull(HttpTimeoutCapability))
        assertEquals(10_000L, timeouts.connectTimeoutMillis)
        assertEquals(30_000L, timeouts.socketTimeoutMillis)
        assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, timeouts.requestTimeoutMillis)
        assertEquals(TEST_USER_AGENT, request.headers[HttpHeaders.UserAgent])
    }

    @Test
    fun `body logging is installed only when enabled`() {
        assertNotNull(apiClient(enableBodyLogging = true).pluginOrNull(Logging))
        assertNull(apiClient().pluginOrNull(Logging))
    }


    private fun apiClient(enableBodyLogging: Boolean = false): HttpClient =
        createOpenFoodFactsHttpClient(
            engine = mockEngine,
            appName = "CarbPeek",
            appVersion = "test",
            contact = "contact",
            enableBodyLogging = enableBodyLogging,
        )

    private fun respondWith(status: HttpStatusCode, body: String) {
        handler = { respondJson(body, status) }
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun successValue(outcome: Outcome<Product>): Product =
        assertIs<Outcome.Success<Product>>(outcome).value

    private fun ingredient(
        id: String,
        name: String,
        percent: Double? = null,
        sub: List<Ingredient> = emptyList(),
    ): Ingredient =
        Ingredient(
            id = id,
            name = name,
            labelName = name,
            declaredPercent = percent,
            subIngredients = sub,
        )

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource(name)!!.readText()

}
