package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.activities.providers.MyProfileActivity
import com.android.swaddle.databinding.ItemStaffListBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import org.jetbrains.anko.textColor

class StaffListAdapter(
    private val context: com.android.swaddle.baseClasses.BaseActivity,
    var list: ArrayList<User>
) :
    RecyclerView.Adapter<StaffListAdapter.StaffViewHolder>() {

    private lateinit var clickListener: ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemStaffListBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_staff_list,
            parent,
            false
        )
        return StaffViewHolder(
            binding,
            this@StaffListAdapter,
            context
        )
    }

    fun setItems(mList: ArrayList<User>) {
        list.clear()
        list.addAll(mList)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.onBind(list, position, context)
        holder.itemView.setOnClickListener {
            clickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class StaffViewHolder(
        itemView: ItemStaffListBinding,
        staffListAdapter: StaffListAdapter,
        context: Context
    ) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        private val adapter = staffListAdapter

        fun onBind(
            list: ArrayList<User>,
            position: Int,
            context: com.android.swaddle.baseClasses.BaseActivity
        ) {
            var item = list[position]

            binding.tvStatus.setOnClickListener {
//                adapter.clickListener.onMarkAttendanceClick(position)
            }
            binding.llMenu.setOnClickListener {
                adapter.clickListener.onMoreOptionClick(position)
            }
            binding.tvName.text = item.firstName + " " + item.lastName

            binding.tvName.setOnClickListener {

                val intent = Intent(context, MyProfileActivity::class.java)
                intent.putExtra("user", item)
                context.startActivity(intent)
                item.selected = true
                adapter.notifyDataSetChanged()

            }

            binding.tvName.setTextColor(
                context.resources.getColor(
                    if (!item.selected!!) R.color.blue else {
                        R.color.colorMedBlack
                    }
                )
            )

            if (context.userManager.user?.type == "provider") {
                binding.llMenu.viewVisible()
            } else {
                binding.llMenu.viewGone()
            }
            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImg)

            if (item.attendance?.size ?: 0 > 0) {

                binding.tvTime.text = item.attendance?.first()?.updatedAt?.let {
                    DateConverter.getChatTime(
                        it
                    )
                }

                when (item.attendance?.first()?.status) {
                    null -> {
                        binding.tvStatus.text = "Not Marked yet"
                        binding.tvStatus.textColor =
                            context.resources.getColor(R.color.txt_medium_gray)
                        /*   binding.tvStatus.background =
                               context.resources.getDrawable(R.drawable.bg_tv_time_gray)*/
                    }
                    "other" -> {
                        binding.tvStatus.text = item.attendance?.first()?.details
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_gray)
                    }
                    "Sign in" -> {
                        binding.tvStatus.text = "Signed in"
                        binding.tvStatus.textColor = context.resources.getColor(R.color.teal_400)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_sign_in)
                    }
                    "Sign out"-> {
                        binding.tvStatus.text = "Signed out"
                        binding.tvStatus.textColor = context.resources.getColor(R.color.colorYellow)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_edit_text_orange)
                    }
                    "sick" -> {
                        binding.tvStatus.text = "Sick"
                        binding.tvStatus.textColor =
                            context.resources.getColor(R.color.txt_medium_gray)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_gray)
                    }
                    "Absent" -> {
                        binding.tvStatus.text = "Absent"
                        binding.tvStatus.textColor = context.resources.getColor(R.color.red)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_red)
                    }
                    "appointment" -> {
                        binding.tvStatus.text = "Doctor's Appointment"
                        binding.tvStatus.textColor =
                            context.resources.getColor(R.color.txt_medium_gray)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_gray)
                    }
                    "late" -> {
                        binding.tvStatus.text = "Going to be late"
                        binding.tvStatus.textColor =
                            context.resources.getColor(R.color.txt_medium_gray)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_gray)
                    }
                    "vacation" -> {
                        binding.tvStatus.text = "Vacation"
                        binding.tvStatus.textColor =
                            context.resources.getColor(R.color.txt_medium_gray)
                        binding.tvStatus.background =
                            context.resources.getDrawable(R.drawable.bg_tv_time_gray)
                    }
                    else -> {
                        binding.tvStatus.text = item.attendance?.first()?.status
                    }
                }
            } else {
                binding.tvStatus.text = "Not Marked yet"
                binding.tvStatus.textColor =
                    context.resources.getColor(R.color.black)
                /* binding.tvStatus.background =
                     context.resources.getDrawable(R.drawable.bg_tv_time_gray)*/
            }
        }
    }

    public fun setListener(listener: ItemClickListener) {
        clickListener = listener
    }


    interface ItemClickListener {
        fun onItemClick(pos: Int)
        fun onMarkAttendanceClick(pos: Int)
        fun onMoreOptionClick(pos: Int)
    }
}