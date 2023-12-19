package com.android.swaddle.adapters.parents_bottom_sheet_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemChildsBottomSheetBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.LoginData
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.signin_signout.SignInSignOutBottomSheet
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide

class ChildesBottomSheetAdapter(
    private val context: Context,
    private var list: ArrayList<ChildInfo>,
    var clickListener: ItemClickListener
) : RecyclerView.Adapter<ChildesBottomSheetAdapter.ViewHolder>() {
    var listener: ItemClickListener? = null

    val userManager: LoginData
        get() {
            return UserData.user(context)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemChildsBottomSheetBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_childs_bottom_sheet,
            parent,
            false
        )
        return ViewHolder(binding, context, this@ChildesBottomSheetAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener, list)
    }

    fun setItems(lis: ArrayList<ChildInfo>) {
        list = lis
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(
        val binding: ItemChildsBottomSheetBinding,
        val context: Context,
        val adapter: ChildesBottomSheetAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: ChildInfo,
            position: Int,
            clickListener: ItemClickListener,
            list: ArrayList<ChildInfo>
        ) {

            if (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") {
                if (SignInSignOutBottomSheet.currentTab == "Pending") {
                    binding.tvAddNote.viewVisible()

                    binding.tvAddNote.setOnClickListener {
                        clickListener.onAddNoteClicked(adapterPosition, item)
                    }
                } else
                    binding.tvAddNote.viewGone()
            } else binding.tvAddNote.viewGone()

            if (SignInSignOutBottomSheet.currentTab == "Pending") {
                if (item.absent_notes != null) {
                    binding.tvAddNote.viewGone()
                    binding.tvParentNote.viewVisible()
                    binding.tvParentNote.text = item.absent_notes.notes

                    if (item.absent_notes.is_absent == 1)
                        binding.tvIsAbsent.viewVisible()
                    else
                        binding.tvIsAbsent.viewGone()

                    if (userManager.user?.type != Constants.provider)
                        if (item.absent_notes.is_absent == 1)
                            binding.iv.viewGone()
                        else
                            binding.iv.viewVisible()
                    else
                        binding.iv.viewVisible()

                } else
                    binding.tvParentNote.viewGone()
            } else {
                binding.tvIsAbsent.viewGone()
                binding.tvParentNote.viewGone()
                binding.iv.viewVisible()
            }


            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.imgProfile)

            binding.tvName.text = item.firstName + " " + item.lastName


            this.itemView.setOnClickListener {
                if (userManager.user?.type != Constants.provider) {
                    if (SignInSignOutBottomSheet.currentTab == "Pending")
                        if (item.absent_notes != null)
                            if (item.absent_notes.is_absent == 0)
                                clickListener.onItemChecked(adapterPosition, item)
                } else
                    clickListener.onItemChecked(adapterPosition, item)
            }

            if (item.isSelected == true) {
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
        fun onItemChecked(position: Int, item: ChildInfo)
        fun onAddNoteClicked(position: Int, item: ChildInfo)
    }
}