package pl.handsome.club.ketoscanner.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.databinding.HistoryListItemBinding
import pl.handsome.club.ketoscanner.util.getSummaryResultImageIdForDietRate


class AnalysisHistoryListAdapter(
    private val productAnalysisResults: List<ProductAnalysisHistoryEntry>,
    private val onItemClick: (ProductAnalysisHistoryEntry) -> Unit
) : RecyclerView.Adapter<AnalysisHistoryListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return HistoryListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        productAnalysisResults[position].also { holder.bind(it, onItemClick) }
    }

    override fun getItemCount(): Int = productAnalysisResults.size


    class ViewHolder(binding: HistoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val resultStatusIcon: ImageView = binding.resultStatusIcon
        private val productBrandText: TextView = binding.productBrand
        private val productNameText: TextView = binding.productName
        private val itemContainer: View = binding.itemContainer


        fun bind(
            productAnalysisHistoryEntry: ProductAnalysisHistoryEntry,
            onItemClick: (ProductAnalysisHistoryEntry) -> Unit
        ) {
            productAnalysisHistoryEntry.macronutrientAnalysisResult
                ?.carbsRate
                .let(::getSummaryResultImageIdForDietRate)
                .also(resultStatusIcon::setImageResource)

            productNameText.text = productAnalysisHistoryEntry.productName
            productBrandText.text = productAnalysisHistoryEntry.productBrand

            itemContainer.setOnClickListener { onItemClick.invoke(productAnalysisHistoryEntry) }
        }

    }

}
