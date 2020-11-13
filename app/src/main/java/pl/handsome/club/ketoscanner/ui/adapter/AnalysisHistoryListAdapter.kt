package pl.handsome.club.ketoscanner.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_list_item.view.*
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.getSummaryResultImageIdForDietRate


class AnalysisHistoryListAdapter(
    private val productAnalysisResults: List<ProductAnalysisHistoryEntry>,
    private val onItemClick: (ProductAnalysisHistoryEntry) -> Unit
) : RecyclerView.Adapter<AnalysisHistoryListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        productAnalysisResults[position].also { holder.bind(it, onItemClick) }
    }

    override fun getItemCount(): Int = productAnalysisResults.size


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val resultStatusIcon: ImageView = view.resultStatusIcon
        private val productBrandText: TextView = view.productBrand
        private val productNameText: TextView = view.productName
        private val itemContainer: View = view.itemContainer


        fun bind(
            productAnalysisHistoryEntry: ProductAnalysisHistoryEntry,
            onItemClick: (ProductAnalysisHistoryEntry) -> Unit
        ) {
            productAnalysisHistoryEntry.macronutrientAnalysisResult
                .carbsRate
                .let(::getSummaryResultImageIdForDietRate)
                .also(resultStatusIcon::setImageResource)

            productNameText.text = productAnalysisHistoryEntry.productName
            productBrandText.text = productAnalysisHistoryEntry.productBrand

            itemContainer.setOnClickListener { onItemClick.invoke(productAnalysisHistoryEntry) }
        }

    }

}