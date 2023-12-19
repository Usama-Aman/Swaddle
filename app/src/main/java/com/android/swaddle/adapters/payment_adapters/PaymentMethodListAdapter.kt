package com.android.swaddle.adapters.payment_adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCardDetailsBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.CalendarModel
import com.android.swaddle.models.CardData
import com.bumptech.glide.Glide


class PaymentMethodListAdapter(
    private val context: Context,
    var list: ArrayList<CardData>,
    var clickListener: ClickListener
) :
    RecyclerView.Adapter<PaymentMethodListAdapter.PaymentMethodVIewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodVIewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemCardDetailsBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_card_details,
            parent,
            false
        )
        return PaymentMethodVIewHolder(
            binding,
            this@PaymentMethodListAdapter,
            context
        )
    }

    override fun onBindViewHolder(holder: PaymentMethodVIewHolder, position: Int) {
        holder.onBind(list, context, position, clickListener)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(mList: ArrayList<CardData>) {
        this.list = mList
    }


    class PaymentMethodVIewHolder(
        private val binding: ItemCardDetailsBinding,
        private val adapter: PaymentMethodListAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(
            list: ArrayList<CardData>,
            context: Context,
            position: Int,
            clickListener: ClickListener
        ) {
            var item = list[position]
            when (item.cardBrand) {
                "visa" -> {

                    Glide.with(context).load(R.drawable.ic_visa).into(binding.ivCard)
                }
                "mastercard" -> {
                    Glide.with(context).load(R.drawable.ic_master_card).into(binding.ivCard)
                }
                else -> {
                    Glide.with(context).load(R.drawable.ic_visa).into(binding.ivCard)
                }
            }

            binding.tvCardNumber.text = "**** **** **** ${item.lastFour}"
            binding.ivDelete.setOnClickListener { clickListener.onDeleteClick(position, item) }

            if (item.isDefault == 1) {
                binding.tvPaymentType.text = "Default Payment Method"
                binding.tvMakeDefault.viewGone()
                binding.tvPaymentType.viewVisible()
            } else {
                binding.tvMakeDefault.viewVisible()
                binding.tvPaymentType.viewGone()
                binding.tvPaymentType.text = ""
            }

            binding.tvMakeDefault.setOnClickListener {
                if (item.isDefault == 0)
                    clickListener.onMakeDefaultClick(position, item)
            }

            binding.root.setOnClickListener {
                clickListener.onITemClick(position, item)
            }

        }


    }

    interface ClickListener {
        fun onITemClick(position: Int, item: CardData)
        fun onMakeDefaultClick(position: Int, item: CardData)
        fun onDeleteClick(position: Int, item: CardData)
    }

}
