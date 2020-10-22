package pl.handsome.club.openfoodapi

import pl.handsome.club.openfoodapi.data.GetProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("/api/v0/product/{barcode}")
    suspend fun getProduct(@Path("barcode") barcode: String) : GetProductResponse

}
