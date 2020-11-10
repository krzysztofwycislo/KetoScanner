package pl.handsome.club.ketoscanner.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.history_list_item.view.*
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.R


class AnalysisHistoryListAdapter(
    private val context: Context,
    private val productAnalysisResults: List<ProductAnalysisHistoryEntry>
) : BaseAdapter() {

    override fun getCount(): Int = productAnalysisResults.size

    override fun getItem(position: Int): Any = productAnalysisResults[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val resultView: View

        if (convertView == null) {
            resultView = LayoutInflater.from(context).inflate(R.layout.history_list_item, parent, false)
            viewHolder = ViewHolder(resultView)
            resultView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            resultView = convertView
        }

        val productAnalysisResult = productAnalysisResults[position]
        viewHolder.bind(productAnalysisResult)

        return resultView
    }

    private class ViewHolder(view: View) {

        private val resultStatusIcon: ImageView = view.resultStatusIcon
        private val productBrandText: TextView = view.productBrand
        private val productNameText: TextView = view.productName


        fun bind(productAnalysisHistoryEntry: ProductAnalysisHistoryEntry) {
            productNameText.text = productAnalysisHistoryEntry.productName
            productBrandText.text = productAnalysisHistoryEntry.productBrand
        }

    }

}