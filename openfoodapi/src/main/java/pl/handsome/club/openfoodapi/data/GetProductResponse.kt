package pl.handsome.club.openfoodapi.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GetProductResponse(
    @SerialName("status") val status: Int,
    @SerialName("code") val barcode: String,
    @SerialName("product") val apiProduct: ApiProduct? = null
)
