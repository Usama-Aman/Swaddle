package com.android.swaddle.adapters.payment_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemChildPaymentsBinding
import com.android.swaddle.databinding.ItemParentPaymentExpandableRecyclerviewBinding

class PaymentDescriptionAdapter(
    var context: Context
) : RecyclerView.Adapter<PaymentDescriptionAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemParentPaymentExpandableRecyclerviewBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_parent_payment_expandable_recyclerview,
            parent,
            false
        )
        return ViewHolder(binding, this@PaymentDescriptionAdapter, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return 5
    }

    class ViewHolder(
        val binding: ItemParentPaymentExpandableRecyclerviewBinding,
        val adapter: PaymentDescriptionAdapter,
        val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {

        }
    }
}