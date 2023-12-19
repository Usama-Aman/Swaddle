package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.MyProfileActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemChildRelationShipBinding
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import org.jetbrains.anko.startActivity

class RelationAdapter(private val mContext: BaseActivity, var list: ArrayList<User>) :
    RecyclerView.Adapter<RelationAdapter.RelationShipViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationShipViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemChildRelationShipBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_child_relation_ship,
            parent,
            false
        )
        return RelationShipViewHolder(binding, this@RelationAdapter, mContext)
    }

    fun setItems(mList: ArrayList<User>) {
        list = mList
    }

    override fun onBindViewHolder(holder: RelationShipViewHolder, position: Int) {
        holder.bind(list[position], mContext)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class RelationShipViewHolder(
        itemView: ItemChildRelationShipBinding,
        relationAdapter: RelationAdapter,
        context: Context
    ) : RecyclerView.ViewHolder(itemView.root) {

        var binding: ItemChildRelationShipBinding = itemView
        fun bind(item: User, mContext: BaseActivity) {
            itemView.setOnClickListener {
                mContext.startActivity<MyProfileActivity>(
                    "user" to item,
                    "canNotEdit" to true
                )
            }

            Glide.with(mContext.applicationContext)
                .load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImage)

            binding.tvName.text = item.firstName + " " + item.lastName
            binding.tvRelation.text = item.relation
            binding.tvEmail.text = item.email
            if (item.isActive == "1") {
                binding.tvAppStatus.text = "Active"
            } else {
                binding.tvAppStatus.text = "Inactive"
            }

            if (item.phoneNumber != null)
                binding.tvPhone.text = item.phoneNumber
            else
                binding.tvPhone.text = "Not Available"
            if (item.homeAddress != null)
                binding.tvHomeAddress.text = item.homeAddress
            else
                binding.tvHomeAddress.text = "Not Available"
            binding.tvCallsYou.text = item.childCallsYou ?: "N/A"
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }
}