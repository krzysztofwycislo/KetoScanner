package pl.handsome.club.openfoodapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.DefaultAppDispatchers
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsImageHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


private const val TEST_USER_AGENT = "KetoScanner/test (contact)"

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
    fun `request timeout is mapped to network failure`() = runTest {
        handler = { request -> throw HttpRequestTimeoutException(request) }

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
            "product_name,brands,image_url,serving_quantity,nutriments,categories_tags",
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
        val imageClient = createOpenFoodFactsImageHttpClient(imageEngine, "KetoScanner", "test", "contact")

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
            appName = "KetoScanner",
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

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource(name)!!.readText()

}
