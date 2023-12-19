package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemSelectChildrenBinding
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.utils.Constants

class SelectChildrenAdapter(
    private val context: Context,
    var list: ArrayList<ChildInfo>,
    var clickListener: ClickListener,
   var classRoomDetail: ClassroomDetails?
) :
    RecyclerView.Adapter<SelectChildrenAdapter.ChildViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemSelectChildrenBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_select_children,
            parent,
            false
        )
        return ChildViewHolder(binding, this@SelectChildrenAdapter, context)

    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model, position, clickListener)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ChildViewHolder(
        binding: ItemSelectChildrenBinding,
        adapter: SelectChildrenAdapter, context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private val binding: ItemSelectChildrenBinding = binding
        private val mAdapter: SelectChildrenAdapter = adapter
        private val context = context

        fun bind(model: ChildInfo, position: Int, clickListener: ClickListener) {
            Glide.with(context).load(Constants.IMG_BASE_PATH + model.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder).into(binding.imgView)
            binding.txtName.text = model.firstName + " " + model.lastName
            if (model.isSelected == true) {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_check_new))
            } else {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_box_new))
            }

            binding.iv.setOnClickListener {
                clickListener.onItemChecked(position, model)
            }


            itemView.setOnClickListener {
                clickListener.onItemCheckedChange(position, model)
            }

        }

    }


    public fun setItems(data: ArrayList<ChildInfo>) {
        list = data
        notifyDataSetChanged()

    }

    interface ClickListener {
        fun onItemCheckedChange(position: Int, item: ChildInfo)
        fun onItemChecked(position: Int, item: ChildInfo)
    }

}