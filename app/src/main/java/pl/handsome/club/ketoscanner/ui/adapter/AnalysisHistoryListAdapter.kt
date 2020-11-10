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


class AnalysisHistoryListAdapter(
    private val productAnalysisResults: List<ProductAnalysisHistoryEntry>
) : RecyclerView.Adapter<AnalysisHistoryListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        productAnalysisResults[position].also(holder::bind)
    }

    override fun getItemCount(): Int = productAnalysisResults.size


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val resultStatusIcon: ImageView = view.resultStatusIcon
        private val productBrandText: TextView = view.productBrand
        private val productNameText: TextView = view.productName


        fun bind(productAnalysisHistoryEntry: ProductAnalysisHistoryEntry) {
            productNameText.text = productAnalysisHistoryEntry.productName
            productBrandText.text = productAnalysisHistoryEntry.productBrand
        }

    }

}