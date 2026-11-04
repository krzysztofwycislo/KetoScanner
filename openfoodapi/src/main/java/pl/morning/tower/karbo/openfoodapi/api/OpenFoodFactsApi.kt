package pl.morning.tower.karbo.openfoodapi.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments

private const val PRODUCT_FIELDS = "product_name,brands,image_url,serving_quantity,nutriments,categories_tags"
private const val PRODUCT_LANGUAGE = "pl"

class OpenFoodFactsApi(private val client: HttpClient) {

    suspend fun getProduct(barcode: String): HttpResponse =
        client.get(OPEN_FOOD_FACTS_BASE_URL) {
            url {
                appendPathSegments("api", "v2", "product", barcode)
                parameters.append("fields", PRODUCT_FIELDS)
                parameters.append("lc", PRODUCT_LANGUAGE)
            }
        }

}
