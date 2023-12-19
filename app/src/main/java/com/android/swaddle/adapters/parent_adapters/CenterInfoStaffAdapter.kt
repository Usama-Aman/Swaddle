package com.android.swaddle.adapters.parent_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCenterInfoActivityBinding

class CenterInfoStaffAdapter(private val context: Context) :
    RecyclerView.Adapter<CenterInfoStaffAdapter.CenterInfoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterInfoViewHolder {
        val inflator = LayoutInflater.from(context)
        val binding: ItemCenterInfoActivityBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_center_info_activity,
            parent,
            false
        )
        return CenterInfoViewHolder(
            binding,
            this@CenterInfoStaffAdapter,
            context
        )
    }

    override fun onBindViewHolder(holder: CenterInfoViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return 10
    }


    class CenterInfoViewHolder(
        itemView: ItemCenterInfoActivityBinding,
        centerInfoStaffAdapter: CenterInfoStaffAdapter,
        context: Context
    ) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        private val adapter = centerInfoStaffAdapter
        private val context = context

        fun onBind() {

        }

    }
}