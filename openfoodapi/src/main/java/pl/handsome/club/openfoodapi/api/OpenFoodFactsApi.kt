package pl.handsome.club.openfoodapi.api

import pl.handsome.club.openfoodapi.data.GetProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

private const val PRODUCT_FIELDS = "product_name,brands,image_url,serving_quantity,nutriments"

interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}?fields=$PRODUCT_FIELDS")
    suspend fun getProduct(@Path("barcode") barcode: String) : Response<GetProductResponse>

}
