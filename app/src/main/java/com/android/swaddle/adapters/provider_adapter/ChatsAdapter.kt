package com.android.swaddle.adapters.provider_adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemMessageFragBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.Chat
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide

class ChatsAdapter(
    private val context: BaseActivity,
    private var dataList: ArrayList<Chat>,
    var clickListener: ClickListener
) :
    RecyclerView.Adapter<ChatsAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemMessageFragBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_message_frag,
            parent,
            false
        )
        return MessageViewHolder(binding, this@ChatsAdapter, context)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val model = dataList[position]
        holder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(list: ArrayList<Chat>) {
        dataList = list
        notifyDataSetChanged()
    }

    class MessageViewHolder(
        private val binding: ItemMessageFragBinding,
        private val mAdapter: ChatsAdapter, private val context: BaseActivity
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Chat, position: Int) {
            // Glide.with(context).load(model.profileImg).into(binding.imgView)
            if (item.type == "group") {
                Glide.with(context)
                    .load(Constants.IMG_BASE_PATH + item.groupPhoto)
                    .placeholder(R.drawable.ic_group_placeholder)
                    .into(binding.imgView)
                binding.tvName.text = item.title ?: ""
                binding.tvTime.text = DateConverter.convertTime12(item.lastMessage?.createdAt ?: "")
                binding.tvMessage.text = item.lastMessage?.message
                item.mainTitle = item.title ?: ""
            } else {
                var oUser: User? = null
                if (item.otherUserId != context.userManager.user?.id) {
                    item.members?.forEachIndexed { index, member ->
                        if (member.user?.id == item.otherUserId) {
                            oUser = member.user
                            Log.e("id", "${member.id}")
                            return@forEachIndexed
                        }
                    }
                } else {
                    oUser = item.lastMessage?.sender
                }


                item.mainTitle =   oUser?.firstName + " " + oUser?.lastName
                binding.tvName.text =
                    oUser?.firstName + " " + oUser?.lastName
                binding.tvMessage.text = item.lastMessage?.message
                binding.tvTime.text = DateConverter.convertTime12(item.lastMessage?.createdAt ?: "")

                Glide.with(context)
                    .load(Constants.IMG_BASE_PATH + oUser?.profilePicture)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding.imgView)
            }
            itemView.setOnClickListener {
                mAdapter.clickListener.onItemClick(adapterPosition, item)
            }
            binding.tvUnread.text = "${item.unreadCount ?: 0}"
            if ((item.unreadCount ?: 0) > 0) {
                binding.tvUnread.viewVisible()

                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorBlack
                    )
                )
                binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            } else {
                binding.tvUnread.viewGone()
                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorHintGrey
                    )
                )
                binding.tvTime.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorHintGrey
                    )
                )
            }
            if (item.lastMessage?.fileType != null) {
                binding.ivMessage.viewVisible()
                binding.tvMessage.viewGone()
            } else {
                binding.ivMessage.viewGone()
                binding.tvMessage.viewVisible()
            }
        }
    }

    interface ClickListener {
        fun onItemClick(pos: Int, item: Chat)

    }
}