package com.android.swaddle.adapters.parents_bottom_sheet_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.adapters.PromoteChildAdapter
import com.android.swaddle.databinding.ItemParentsBottomSheetBinding
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide

class ParentsBottomSheetAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    var clickListener: ItemClickListener
) : RecyclerView.Adapter<ParentsBottomSheetAdapter.ViewHolder>() {
    var listener: ItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemParentsBottomSheetBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_parents_bottom_sheet,
            parent,
            false
        )
        return ViewHolder(binding, context, this@ParentsBottomSheetAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener, list)
    }

    fun setItems(lis: ArrayList<User>) {
        list = lis
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(
        val binding: ItemParentsBottomSheetBinding,
        val context: Context,
        val adapter: ParentsBottomSheetAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: User,
            position: Int,
            clickListener: ItemClickListener,
            list: ArrayList<User>
        ) {

            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.imgProfile)

            binding.tvName.text = item.firstName + " " + item.lastName
            if (item.relation == null) {
                binding.tvRelation.text = "N/A"
            } else {
                binding.tvRelation.text = item.relation
            }

            this.itemView.setOnClickListener {
                clickListener.onItemChecked(adapterPosition, item)
            }
            if (item.isUserSelected) {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_check_new))
            } else {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_box_new))
            }

            binding.iv.setOnClickListener {
                clickListener.onItemChecked(position, item)
            }
        }
    }

    interface ItemClickListener {
        fun onItemChecked(position: Int, item: User)
    }
}