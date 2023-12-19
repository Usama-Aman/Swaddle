package com.android.swaddle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCalendarCardBinding
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.WeeklyChildAttendanceData

class WeeklyChildAttendanceAdapter(
    private val contex: Context,
    private var dataList: MutableList<WeeklyChildAttendanceData>,
    private val clickListeners: ClickListener
) :
    RecyclerView.Adapter<WeeklyChildAttendanceAdapter.DateViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemCalendarCardBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_calendar_card,
            parent,
            false
        )
        return DateViewHolder(binding, this@WeeklyChildAttendanceAdapter, contex)
    }


    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dataList[position]
        holder.onBind(dataList, clickListeners, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setItems(items: ArrayList<WeeklyChildAttendanceData>) {
        dataList = items
    }

    class DateViewHolder(
        bindingg: ItemCalendarCardBinding,
        dateAdapter: WeeklyChildAttendanceAdapter,
        contex: Context
    ) : RecyclerView.ViewHolder(bindingg.root) {

        private val binding: ItemCalendarCardBinding = bindingg
        private val adapter = dateAdapter
        private val cc: Context = contex

        fun onBind(
            dataList: MutableList<WeeklyChildAttendanceData>,
            clickListeners: ClickListener,
            position: Int
        ) {

            binding.tvAttendanceStatus.viewVisible()

            if (dataList[position].is_present == 0) {
                binding.tvAttendanceStatus.text = "Absent"
                binding.tvAttendanceStatus.setTextColor(ContextCompat.getColor(cc, R.color.red))
            } else {
                binding.tvAttendanceStatus.text = "Present"
                binding.tvAttendanceStatus.setTextColor(ContextCompat.getColor(cc, R.color.emerald))
            }

            binding.tvDay.text = dataList[position].week_day
            binding.tvDate.text = dataList[position].day

            if (dataList[position].isSelected) {
                binding.ll.setBackgroundColor(
                    ContextCompat.getColor(
                        cc,
                        R.color.colorYellow
                    )
                )
                binding.tvDate.setTextColor(ContextCompat.getColor(cc, R.color.colorWhite))
                binding.tvDay.setTextColor(ContextCompat.getColor(cc, R.color.colorWhite))
                binding.tvAttendanceStatus.setTextColor(ContextCompat.getColor(cc, R.color.colorWhite))
            } else {
                if (dataList[position].isEnabled) {
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
                clickListeners.onITemClick(position, dataList[position])
            }
        }
    }


    interface ClickListener {
        fun onITemClick(position: Int, item: WeeklyChildAttendanceData)
    }

}