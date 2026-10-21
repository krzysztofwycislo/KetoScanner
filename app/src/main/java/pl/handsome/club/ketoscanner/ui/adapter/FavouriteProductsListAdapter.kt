package pl.handsome.club.ketoscanner.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.ketoscanner.databinding.FavoriteProductsListItemBinding

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
        return FavoriteProductsListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(FavouriteProductsListAdapter::ViewHolder)
    }

    class ViewHolder(binding: FavoriteProductsListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val itemContainer: View = binding.itemContainer
        private val productImage: ImageView = binding.productImage
        private val productBrand: TextView = binding.productBrand
        private val productName: TextView = binding.productName

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
