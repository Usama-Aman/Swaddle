package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCalendarCardBinding
import com.android.swaddle.helper.getDay
import com.android.swaddle.helper.getDayName
import com.android.swaddle.models.CalendarModel
import java.util.*
import kotlin.collections.ArrayList

class DateAdapter(
    private val contex: Context,
    private var dataList: ArrayList<CalendarModel>,
    private val clickListeners: ClickListener
) :
    RecyclerView.Adapter<DateAdapter.DateViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemCalendarCardBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_calendar_card,
            parent,
            false
        )
        return DateViewHolder(binding, this@DateAdapter, contex)
    }


    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dataList[position]
        holder.onBind(date, dataList, clickListeners, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setItems(items: ArrayList<CalendarModel>) {
        dataList = items
    }

    class DateViewHolder(
        bindingg: ItemCalendarCardBinding,
        dateAdapter: DateAdapter,
        contex: Context
    ) : RecyclerView.ViewHolder(bindingg.root) {

        private val binding: ItemCalendarCardBinding = bindingg
        private val adapter = dateAdapter
        private val cc: Context = contex

        fun onBind(
            date: CalendarModel,
            dataList: ArrayList<CalendarModel>,
            clickListeners: ClickListener,
            position: Int
        ) {
            binding.tvDay.text = getDayName(date.date!!)
            binding.tvDate.text = getDay(date.date!!)

            if (date.isSelected) {
                binding.ll.setBackgroundColor(
                    ContextCompat.getColor(
                        cc,
                        R.color.colorYellow
                    )
                )
                binding.tvDate.setTextColor(ContextCompat.getColor(cc, R.color.colorWhite))
                binding.tvDay.setTextColor(ContextCompat.getColor(cc, R.color.colorWhite))
            } else {
                if (date.isEnabled) {
                    binding.ll.setBackgroundColor(
                        ContextCompat.getColor(
                            cc,
                            R.color.colorWhite
                        )
                    )
                    binding.tvDate.setTextColor(ContextCompat.getColor(cc, R.color.colorBlack))
                    binding.tvDay.setTextColor(ContextCompat.getColor(cc, R.color.colorBlack))
                } else {
                    binding.tvDate.setTextColor(ContextCompat.getColor(cc, R.color.colorLightGrey))
                    binding.tvDay.setTextColor(ContextCompat.getColor(cc, R.color.colorLightGrey))
                    binding.ll.setBackgroundColor(
                        ContextCompat.getColor(
                            cc,
                            R.color.grey_100
                        )
                    )
                }
            }
            binding.cardView.setOnClickListener {
                clickListeners.onITemClick(position, dataList.get(position))
            }
        }
    }


    interface ClickListener {
        fun onITemClick(position: Int, item: CalendarModel)
    }

}