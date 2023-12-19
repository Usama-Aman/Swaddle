package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemNewGroupChatBinding
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide

class NewGroupChatUsersAdapter(
    private val context: Context,
    var list: ArrayList<User>,
    var clickListener: ClickListener
) :
    RecyclerView.Adapter<NewGroupChatUsersAdapter.NewGroupChatViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewGroupChatViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemNewGroupChatBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_new_group_chat,
            parent,
            false
        )
        return NewGroupChatViewHolder(binding, clickListener, context)
    }

    override fun onBindViewHolder(holder: NewGroupChatViewHolder, position: Int) {
        holder.bind(position, list[position])

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(mList: ArrayList<User>) {
        list = mList
        notifyDataSetChanged()
    }

    inner class NewGroupChatViewHolder(
        val binding: ItemNewGroupChatBinding,
        val mClickListener: ClickListener, val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, item: User) {
            binding.imgView.setOnClickListener { mClickListener.onProfileImageClick(position) }
            binding.mItemGroupChat.setOnClickListener {
                mClickListener.onCheckBoxClick(position)
            }
            binding.ivCheckBox.setOnClickListener {
                mClickListener.onCheckBoxClick(position)
            }
            binding.ivCheckBox.isChecked = item.isUserSelected

            binding.tvName.text = item.firstName + " " + item.firstName
            binding.imgView.let {
                Glide.with(context)
                    .load(Constants.IMG_BASE_PATH + item.profilePicture)
                    .placeholder(R.drawable.ic_user_placeholder).into(it)
            }
        }
    }

    interface ClickListener {
        fun onItemClick(pos: Int)
        fun onCheckBoxClick(pos: Int)
        fun onProfileImageClick(pos: Int)
    }
}