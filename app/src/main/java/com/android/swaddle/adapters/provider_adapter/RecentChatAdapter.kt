package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemRecentChatBinding
import com.android.swaddle.helper.viewGone

class RecentChatAdapter(private val context : Context) : RecyclerView.Adapter<RecentChatAdapter.RecentViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemRecentChatBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_recent_chat,
            parent,
            false
        )
        return RecentViewHolder(binding, this@RecentChatAdapter, context)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return 10
    }





    class RecentViewHolder(
        binding: ItemRecentChatBinding,
        messageAdapter: RecentChatAdapter, context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        private val binding: ItemRecentChatBinding = binding
        private val mAdapter: RecentChatAdapter = messageAdapter
        private val context = context

        fun bind(pos : Int) {
            if(pos == 9) {
                binding.divider1.viewGone()

            }


        }
    }
}