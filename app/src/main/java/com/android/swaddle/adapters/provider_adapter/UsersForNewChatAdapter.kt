package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemMessageFragBinding
import com.android.swaddle.databinding.ItemUsersForChatBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.Chat
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide

class UsersForNewChatAdapter(private val context: Context, private var dataList: ArrayList<User>) :
    RecyclerView.Adapter<UsersForNewChatAdapter.MessageViewHolder>() {
    private lateinit var clickListener: ClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemUsersForChatBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_users_for_chat,
            parent,
            false
        )
        return MessageViewHolder(binding, this@UsersForNewChatAdapter, context)
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val model = dataList[position]
        holder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(list: ArrayList<User>) {
        dataList = list
        notifyDataSetChanged()
    }

    class MessageViewHolder(
        val binding: ItemUsersForChatBinding,
        val adapter: UsersForNewChatAdapter, val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User, position: Int) {

            binding.imgView.setOnClickListener { adapter.clickListener.onProfileImageClick(position) }

            binding.tvName.text = item.firstName + " " + item.lastName
            binding.imgView.let {
                Glide.with(context)
                    .load(Constants.IMG_BASE_PATH + item.profilePicture)
                    .placeholder(R.drawable.ic_user_placeholder).into(it)
            }

            itemView.setOnClickListener {
                adapter.clickListener.onItemClick(adapterPosition)
            }
        }
    }

    public fun setListener(listener: ClickListener) {
        clickListener = listener
    }


    interface ClickListener {
        fun onItemClick(pos: Int)
        fun onProfileImageClick(pos: Int)
    }
}