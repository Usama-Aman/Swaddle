package com.android.swaddle.adapters.provider_adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ChatRowBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.Chat
import com.android.swaddle.models.Message
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.RoundRectCornerImageView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper


class MessagesAdapter(
    private val context: BaseActivity,
    var list: ArrayList<Message>,
    var clickListener: ClickListener
) :
    RecyclerView.Adapter<MessagesAdapter.ChatViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ChatRowBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.chat_row,
            parent,
            false
        )
        return ChatViewHolder(binding, this@MessagesAdapter, context)
    }

    fun setData(date: ArrayList<Message>) {
        list = date
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.onBind(position, list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ChatViewHolder(
        val binding: ChatRowBinding,
        var messageAdapter: MessagesAdapter, val context: BaseActivity
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(pos: Int, item: Message) {
//            viewBinderHelper.bind(binding.swipeRevealLayout, item.chatId.toString())
//            binding.swipeRevealLayout.setLockDrag(true)
//            binding.swipeRevealLayout.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
//                override fun onClosed(view: SwipeRevealLayout?) {
//
//                }
//
//                override fun onOpened(view: SwipeRevealLayout?) {
//                    Toast.makeText(context, pos.toString(), Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {
//                }
//
//            })

            if (item.fileType == null) {
                binding.tvMessage.text = item.message
                binding.tvTime.text = DateConverter.convertTime12(item.createdAt ?: "")
                binding.lnrImage.viewGone()
                binding.lnrTextMessage.viewVisible()
                binding.tvMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            else {

                if (!item.message.isNullOrEmpty()) {
                    binding.tvMessage.viewVisible()
                    binding.tvMessage.text = item.message
                }else
                    binding.tvMessage.viewGone()


                if (item.fileType.equals("pdf", true)) {
                    binding.lnrImage.viewGone()
                    binding.lnrTextMessage.viewVisible()
                    val index = item.filePath!!.lastIndexOf('/')
                    val fileName =
                        item.filePath!!.substring(index + 1, item.filePath!!.lastIndex + 1)
                    binding.tvMessage.text = fileName
                    binding.tvMessage.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_insert_drive_file_36,
                        0,
                        R.drawable.ic_download,
                        0
                    )
                    binding.tvTime.text = DateConverter.convertTime12(item.createdAt ?: "")
                    binding.lnrTextMessage.setOnClickListener {
                        messageAdapter.clickListener.onFileClick(adapterPosition, item)
                    }
                } else {
//                    binding.lnrImage.viewVisible()
//                    binding.lnrTextMessage.viewGone()

                    if (!item.message.isNullOrEmpty()) {
                        binding.tvMessage.viewVisible()
                        binding.tvMessage.text = item.message
                    }else
                        binding.tvMessage.viewGone()


                    binding.lnrImage.viewVisible()


                    Glide.with(context).load(Constants.IMG_BASE_PATH + item.filePath)
                        .into(binding.ivImage)
                    binding.tvImageTime.text = DateConverter.convertTime12(item.createdAt ?: "")
                    binding.lnrImage.setOnClickListener {
                        messageAdapter.clickListener.onMediaClicked(pos, item)

                    }
                }


            }

            if (item.senderId == context.userManager.user?.id) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.RIGHT
                }
                binding.lnrParent.layoutParams = params

                binding.ivImage.setCornerRadius(
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(0).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat()
                )
                binding.rlImage.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_my_chat)

                binding.tvMessage.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_my_chat)
                binding.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                binding.icProfile.viewGone()
                // mBinding.tvTime.setTextColor(ContextCompat.getColor(contex, R.color.colorWhite))
                binding.tvMessage.layoutParams = params
                binding.tvTime.layoutParams = params
            }
            else {
                Glide.with(context).load(Constants.IMG_BASE_PATH + item.filePath)
                    .into(binding.ivImage)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.LEFT
                }
                binding.rlImage.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_other_chat)
                binding.ivImage.setCornerRadius(
                    RoundRectCornerImageView.convertDpToPixel(0).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat(),
                    RoundRectCornerImageView.convertDpToPixel(10).toFloat()
                )
                binding.lnrParent.layoutParams = params
                binding.tvMessage.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_other_chat)
                binding.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                binding.icProfile.viewVisible()
                //       mBinding.tvTime.setTextColor(ContextCompat.getColor(contex, R.color.colorBlack))

                Glide.with(context)
                    .load(Constants.IMG_BASE_PATH + item.sender?.profilePicture)
                    .into(binding.icProfile)

            }
        }
    }

    interface ClickListener {
        fun onMediaClicked(position: Int, item: Message?)
        fun onFileClick(pos: Int, item: Message) {}
    }


}