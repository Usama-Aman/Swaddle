package com.android.swaddle.activities.parent

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemRecAttandenceHistoryBinding
import com.android.swaddle.models.ChildAttendanceMultiple
import com.android.swaddle.utils.DateConverter

class AttendanceHistoryAdapter(
    private val context: Context,
    var childAttendanceMultiple: ArrayList<ChildAttendanceMultiple>,
    var clickListener: ItemClickListener,
    var arrival: Boolean = true
) :
    RecyclerView.Adapter<AttendanceHistoryAdapter.AddRelationShipViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddRelationShipViewHolder {
        val inflator = LayoutInflater.from(context)
        val binding: ItemRecAttandenceHistoryBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_rec_attandence_history,
            parent,
            false
        )
        return AddRelationShipViewHolder(
            arrival,
            binding,
            context
        )
    }

    fun setItems(list: ArrayList<ChildAttendanceMultiple>) {
        childAttendanceMultiple = list
    }

    override fun onBindViewHolder(holder: AddRelationShipViewHolder, position: Int) {
        holder.onBind(childAttendanceMultiple, position)
    }

    override fun getItemCount(): Int {
        Log.e("Size", "${childAttendanceMultiple?.size ?: 0}")
        return childAttendanceMultiple?.size ?: 0
    }

    class AddRelationShipViewHolder(
        var arrival: Boolean,
        var binding: ItemRecAttandenceHistoryBinding,
        var context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: ArrayList<ChildAttendanceMultiple>?, position: Int) {
            itemView.setOnClickListener {
//                adapter.clickListener.onItemClick(adapterPosition)
            }
            if (arrival) {
                if (item?.get(position)?.arrivalTime != null)
                    binding.tvTime.text = DateConverter.convertTime24Attendance(
                        item[position].arrivalTime ?: ""
                    )
            } else {
                if (item?.get(position)?.departureTime != null)
                    binding.tvTime.text = DateConverter.convertTime24Attendance(
                        item[position].departureTime ?: ""
                    )
            }
        }
    }


    interface ItemClickListener {
//        fun onItemClick(position: Int)
//        fun onItemRemovedClick(position: Int)
    }

}