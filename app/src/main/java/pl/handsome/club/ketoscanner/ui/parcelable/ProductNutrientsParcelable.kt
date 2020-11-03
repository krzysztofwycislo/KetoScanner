package pl.handsome.club.ketoscanner.ui.parcelable

import android.os.Parcel
import android.os.Parcelable
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


data class ProductNutrientsParcelable(
    val productNutriments: ProductNutriments,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        ProductNutriments(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble()
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(productNutriments) {
            parcel.writeDouble(fatPerServing)
            parcel.writeDouble(carbohydratesPerServing)
            parcel.writeDouble(proteinsPerServing)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProductNutrientsParcelable> {
        override fun createFromParcel(parcel: Parcel): ProductNutrientsParcelable =
            ProductNutrientsParcelable(parcel)

        override fun newArray(size: Int): Array<ProductNutrientsParcelable?> = arrayOfNulls(size)
    }
}
