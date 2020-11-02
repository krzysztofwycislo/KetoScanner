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
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble()
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(productNutriments) {
            parcel.writeDouble(energyPer100g)
            parcel.writeDouble(energyPerServing)

            parcel.writeDouble(fatPer100g)
            parcel.writeDouble(fatPerServing)

            parcel.writeDouble(saturatedFatPer100g)
            parcel.writeDouble(saturatedFatPerServing)

            parcel.writeDouble(carbohydratesPer100g)
            parcel.writeDouble(carbohydratesPerServing)

            parcel.writeDouble(sugarsPer100g)
            parcel.writeDouble(sugarsPerServing)

            parcel.writeDouble(sugarsPer100g)
            parcel.writeDouble(sugarsPerServing)

            parcel.writeDouble(proteinsPer100g)
            parcel.writeDouble(proteinsPerServing)

            parcel.writeDouble(saltPer100g)
            parcel.writeDouble(saltPerServing)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProductNutrientsParcelable> {
        override fun createFromParcel(parcel: Parcel): ProductNutrientsParcelable =
            ProductNutrientsParcelable(parcel)

        override fun newArray(size: Int): Array<ProductNutrientsParcelable?> = arrayOfNulls(size)
    }
}
