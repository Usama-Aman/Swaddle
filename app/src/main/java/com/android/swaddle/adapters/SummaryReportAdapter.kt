package com.android.swaddle.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemSummaryReportBinding
import com.android.swaddle.models.SummaryReport
import com.android.swaddle.utils.DateConverter

class SummaryReportAdapter(private val context: Context, var list: ArrayList<SummaryReport>) :
    RecyclerView.Adapter<SummaryReportAdapter.SummaryReportViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryReportViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemSummaryReportBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_summary_report,
            parent,
            false
        )
        return SummaryReportViewHolder(
            binding,
            this@SummaryReportAdapter,
            context
        )
    }

    fun setItems(listClass: ArrayList<SummaryReport>) {
        list.clear()
        list.addAll(listClass)
    }

    override fun onBindViewHolder(holder: SummaryReportViewHolder, position: Int) {
        holder.onBind(list, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class SummaryReportViewHolder(
        private val binding: ItemSummaryReportBinding,
        private val adapter: SummaryReportAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(list: ArrayList<SummaryReport>, position: Int) {
            var item = list[position]
            binding.tvDay.text = item.attendenceDay

            if (item.arrivalTime != null)
                binding.tvStatingTime.text = item.arrivalTime
//                    DateConverter.convertGenericDate(item.arrivalTime ?: "","hh:mm a","hh:mm a")
            if (item.departureTime != null)
                binding.tvEndingTime.text = item.departureTime
//                    DateConverter.convertGenericDate(item.departureTime ?: "","hh:mm a","hh:mm a")

        }
    }
}