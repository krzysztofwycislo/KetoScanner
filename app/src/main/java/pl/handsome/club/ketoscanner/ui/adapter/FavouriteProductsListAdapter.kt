package pl.handsome.club.ketoscanner.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.favorite_products_list_item.view.*
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.ketoscanner.R

class FavouriteProductsListAdapter(
    private val onItemClick: (FavouriteProduct) -> Unit
) : RecyclerView.Adapter<FavouriteProductsListAdapter.ViewHolder>() {

    private var favouriteProducts: List<FavouriteProduct> = listOf()

    fun setProducts(newFavouriteProducts: List<FavouriteProduct>) {
        favouriteProducts = newFavouriteProducts
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = favouriteProducts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        favouriteProducts[position].also { holder.bind(it, onItemClick) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_products_list_item, parent, false)
            .let(FavouriteProductsListAdapter::ViewHolder)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val itemContainer: View = view.itemContainer
        private val productImage: ImageView = view.productImage
        private val productBrand: TextView = view.productBrand
        private val productName: TextView = view.productName

        fun bind(favouriteProduct: FavouriteProduct?, onItemClick: (FavouriteProduct) -> Unit) {
            if (favouriteProduct != null) {
                itemContainer.setOnClickListener { onItemClick(favouriteProduct) }
                productBrand.text = favouriteProduct.productBrand
                productName.text = favouriteProduct.productName
                favouriteProduct.imageUrl?.also(::loadImage)
            }
        }

        private fun loadImage(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .into(productImage)
        }

    }

}