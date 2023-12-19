package com.android.swaddle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCenterRelationshipsBinding
import com.android.swaddle.models.User
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import org.json.JSONArray

class CenterRelationsAdapter(
    private val mContext: Context,
    var list: ArrayList<User>,
    var itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<CenterRelationsAdapter.RelationShipViewHolder>() {
    private var clickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationShipViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemCenterRelationshipsBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_center_relationships,
            parent,
            false
        )
        return RelationShipViewHolder(binding, this@CenterRelationsAdapter, mContext)
    }

    fun setItems(mList: ArrayList<User>) {
        list = mList
    }

    override fun onBindViewHolder(holder: RelationShipViewHolder, position: Int) {
        holder.bind(list[position], mContext, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class RelationShipViewHolder(
        itemView: ItemCenterRelationshipsBinding,
        var adapter: CenterRelationsAdapter,

        context: Context
    ) : RecyclerView.ViewHolder(itemView.root) {

        var binding: ItemCenterRelationshipsBinding = itemView
        fun bind(item: User, mContext: Context, position: Int) {
            itemView.setOnClickListener {
                adapter.itemClickListener.onItemClick(position)
            }
            Glide.with(mContext.applicationContext)
                .load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImage)
            binding.tvName.text = item.firstName + " " + item.lastName
            binding.tvRelation.text = item.position
            if (item.inspiration != null) {
                binding.tvInspiration.text = item.inspiration
            } else {
                binding.tvInspiration.text = "Not Available"
            }

            if (item.moreCertification != null) {
                try {
                    var obj = JSONArray(item.moreCertification)
                    binding.tvCertifications.text = obj.opt(0).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.tvCertifications.text = "Not Available"
            }
            if (item.experience != null) {
                binding.tvExperience.text = item.experience
            } else {
                binding.tvExperience.text = "Not Available"
            }
        }
    }

    fun setListener(listener: ItemClickListener) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }
}