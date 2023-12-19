package com.android.swaddle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemPromoteChildBinding
import com.android.swaddle.models.ClassroomDetails

class PromoteChildAdapter(
    private val context: Context,
    private var list: ArrayList<ClassroomDetails>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<PromoteChildAdapter.ClassRoomViewHolder>() {
    var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassRoomViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemPromoteChildBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_promote_child,
            parent,
            false
        )
        return ClassRoomViewHolder(binding, this@PromoteChildAdapter, context)
    }

    override fun onBindViewHolder(holder: ClassRoomViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener, list)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(lis: ArrayList<ClassroomDetails>) {
        list = lis
    }

    class ClassRoomViewHolder(
        binding: ItemPromoteChildBinding,
        messageAdapter: PromoteChildAdapter, context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: ClassroomDetails,
            position: Int,
            clickListener: ItemClickListener,
            list: ArrayList<ClassroomDetails>
        ) {
            this.itemView.setOnClickListener {
                clickListener.onItemChecked(adapterPosition, item)
            }
            binding.tvClassName.text = item.name
            if (item.childesCount == 1) {
                binding.tvChildCount.text = "1 Child"
            } else {
                binding.tvChildCount.text = "${item.childesCount} Children"
            }
            if (item.isSelected) {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_check_new))
            } else {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_box_new))
            }

            binding.iv.setOnClickListener {
                clickListener.onItemChecked(position, item)
            }
        }

        private val binding: ItemPromoteChildBinding = binding
        private val mAdapter: PromoteChildAdapter = messageAdapter
        private val context = context
    }


    interface ItemClickListener {
        fun onItemChecked(position: Int, item: ClassroomDetails)
    }
}