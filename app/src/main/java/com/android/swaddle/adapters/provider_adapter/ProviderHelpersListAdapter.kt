package com.android.swaddle.adapters.provider_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemProviderHelperClassBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide

class ProviderHelpersListAdapter(
    private val context: BaseActivity,
    private var list: ArrayList<ChildInfo>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<ProviderHelpersListAdapter.ClassRoomViewHolder>() {
    var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassRoomViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemProviderHelperClassBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_provider_helper_class,
            parent,
            false
        )
        return ClassRoomViewHolder(binding, this@ProviderHelpersListAdapter, context)
    }


    override fun onBindViewHolder(holder: ClassRoomViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(lis: ArrayList<ChildInfo>) {
        list = lis
    }

    class ClassRoomViewHolder(
        var binding: ItemProviderHelperClassBinding,
        messageAdapter: ProviderHelpersListAdapter, var context: BaseActivity
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: ChildInfo,
            position: Int,
            clickListener: ItemClickListener
        ) {
            itemView.setOnClickListener { clickListener.onItemClicked(position, item) }

            (item.firstName + " " + item.lastName).also { binding.tvName.text = it }
            Glide.with(context)
                .load(Constants.IMG_BASE_PATH + item.profilePicture ?: "")
                .placeholder(
                    R.drawable.ic_user_placeholder_new
                ).into(binding.ivImg)

            if (item.attendance?.childAttendanceMultiple?.isNotEmpty() == true && item.attendance?.childAttendanceMultiple?.last()?.arrivalTime != null) {
                binding.llSignIn.viewVisible()
                binding.llSignOut.viewVisible()
                binding.tvSignInStatus.setTextColor(context.resources.getColor(R.color.teal_400))
                binding.tvSignInStatus.background =
                    context.resources.getDrawable(R.drawable.bg_tv_sign_in)

                DateConverter.convertTime24Attendance(
                    item.attendance?.childAttendanceMultiple?.last()?.arrivalTime ?: ""
                ).also { binding.tvSignInStatus.text = "$it" }
            } else {
                binding.llSignIn.viewVisible()
                binding.llSignOut.viewGone()
                binding.tvSignInStatus.text = "Absent"
                binding.tvSignInStatus.setTextColor(context.resources.getColor(R.color.red))
                binding.tvSignInStatus.background =
                    context.resources.getDrawable(R.drawable.bg_tv_time_red)
            }
            if (item.attendance?.childAttendanceMultiple?.isNotEmpty() == true && item.attendance?.childAttendanceMultiple?.last()?.departureTime != null) {
                DateConverter.convertTime24Attendance(
                    item.attendance?.childAttendanceMultiple?.last()?.departureTime ?: ""
                ).also {
                    binding.tvSignOutStatus.text = "$it"
                    binding.tvSignOutStatus.setTextColor(context.resources.getColor(R.color.teal_400))
                    binding.tvSignOutStatus.background =
                        context.resources.getDrawable(R.drawable.bg_tv_sign_in)
                }
            }

        }


    }


    interface ItemClickListener {
        //        fun onRelationshipClick(position: Int, item: ChildInfo)
        fun onItemClicked(position: Int, item: ChildInfo)
    }
}