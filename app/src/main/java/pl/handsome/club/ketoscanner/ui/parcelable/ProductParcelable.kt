package pl.handsome.club.ketoscanner.ui.parcelable

import android.os.Parcel
import android.os.Parcelable
import pl.handsome.club.domain.product.Product


data class ProductParcelable(
    val product: Product,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        Product(
            parcel.readString()!!,
            parcel.readString()!!
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(product) {
            parcel.writeString(name)
            parcel.writeString(barcode)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProductParcelable> {
        override fun createFromParcel(parcel: Parcel): ProductParcelable = ProductParcelable(parcel)
        override fun newArray(size: Int): Array<ProductParcelable?> = arrayOfNulls(size)
    }

}