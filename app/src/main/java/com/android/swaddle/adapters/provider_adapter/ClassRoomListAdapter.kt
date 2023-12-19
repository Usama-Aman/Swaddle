package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemClassListBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassroomDetails

class ClassRoomListAdapter(
    private val context: Context,
    private var list: ArrayList<ClassroomDetails>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<ClassRoomListAdapter.ClassRoomViewHolder>() {
    var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassRoomViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemClassListBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_class_list,
            parent,
            false
        )
        return ClassRoomViewHolder(binding, this@ClassRoomListAdapter, context)
    }


    override fun onBindViewHolder(holder: ClassRoomViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(lis: ArrayList<ClassroomDetails>) {
        list = lis
    }

    class ClassRoomViewHolder(
        binding: ItemClassListBinding,
        messageAdapter: ClassRoomListAdapter, context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: ClassroomDetails,
            position: Int,
            clickListener: ItemClickListener
        ) {
            this.itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition, item)
            }
            binding.tvClassName.text = item.name
            binding.tvDesc.text = item.description

            if (item.isDefault == 1) {
                binding.ivMenu.viewGone()
            } else {
                binding.ivMenu.viewVisible()
            }

            if (item.childesCount == 1) {
                binding.tvChildCount.text = "1 Child Enrolled"
            } else {
                binding.tvChildCount.text = "${item.childesCount} Children Enrolled"
            }
            if (item.signInCount != null)
                binding.tvSignInCount.text = "${item.signInCount}"
            if (item.signOutCount != null)
                binding.tvSignOutCount.text = "${item.signOutCount}"
            if (item.absentCount != null)
                binding.tvAbsentCount.text = "${item.absentCount}"
//            if (item.absentCount != null)
//                binding.tvAbsentCount.text = "${item.absentCount}"

            binding.tvClassName.text = item.name
            binding.cvActivities.setOnClickListener {
                clickListener.onAttendanceClick(
                    position,
                    item
                )
            }
            binding.ivMenu.setOnClickListener {
                clickListener.onMoreClick(adapterPosition, item)
            }
            binding.cvMedical.setOnClickListener { clickListener.onMedicalClick(position, item) }
            binding.cvPayment.setOnClickListener { clickListener.onPaymentClick(position, item) }
            binding.cvRelationships.setOnClickListener {
                clickListener.onRelationshipClick(
                    position,
                    item
                )
            }
        }

        private val binding: ItemClassListBinding = binding
        private val mAdapter: ClassRoomListAdapter = messageAdapter
        private val context = context
    }


    interface ItemClickListener {
        fun onItemClick(position: Int, item: ClassroomDetails)
        fun onMedicalClick(position: Int, item: ClassroomDetails)
        fun onAttendanceClick(position: Int, item: ClassroomDetails)
        fun onPaymentClick(position: Int, item: ClassroomDetails)
        fun onRelationshipClick(position: Int, item: ClassroomDetails)
        fun onMoreClick(position: Int, item: ClassroomDetails)
    }
}