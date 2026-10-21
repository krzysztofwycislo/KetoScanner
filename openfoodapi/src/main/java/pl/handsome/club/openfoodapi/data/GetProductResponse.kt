package pl.handsome.club.openfoodapi.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class GetProductResponse(
    @Json(name = "status") val status: Int,
    @Json(name = "code") val barcode: String,
    @Json(name = "product") val apiProduct: ApiProduct?
)
