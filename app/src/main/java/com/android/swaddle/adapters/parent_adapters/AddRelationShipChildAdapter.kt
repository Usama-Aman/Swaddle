package com.android.swaddle.adapters.parent_adapters

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemRecInviteBinding
import com.android.swaddle.helper.Spanny
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import org.json.JSONArray
import java.lang.StringBuilder

class AddRelationShipChildAdapter(
    private val context: Context,
    var selectedChildesList: ArrayList<ChildInfo>, var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<AddRelationShipChildAdapter.AddRelationShipViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddRelationShipViewHolder {
        val inflator = LayoutInflater.from(context)
        val binding: ItemRecInviteBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_rec_invite,
            parent,
            false
        )
        return AddRelationShipViewHolder(
            binding,
            this@AddRelationShipChildAdapter,
            context
        )
    }

    fun setItems(list: ArrayList<ChildInfo>) {
        selectedChildesList = list
    }

    override fun onBindViewHolder(holder: AddRelationShipViewHolder, position: Int) {
        holder.onBind(selectedChildesList[position], position)
    }

    override fun getItemCount(): Int {
        return selectedChildesList.size
    }

    class AddRelationShipViewHolder(
        var binding: ItemRecInviteBinding,
        var addRelationShipChildAdapter: AddRelationShipChildAdapter,
        var context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        private val adapter = addRelationShipChildAdapter

        fun onBind(item: ChildInfo, position: Int) {
            itemView.setOnClickListener {
                adapter.clickListener.onItemClick(adapterPosition)
            }
            binding.ivRemove.setOnClickListener {
                adapter.clickListener.onItemRemovedClick(adapterPosition)
            }

            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)

            binding.textView.text = item.firstName + " " + item.lastName


        }

    }


    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onItemRemovedClick(position: Int)
    }

}