package com.android.swaddle.adapters.payment_adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemChildPaymentUpdatedBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import org.jetbrains.anko.textColor
import kotlin.math.abs

class PaymentDetailAdapterNew(
    var context: BaseActivity,
    var clickListeners: ClickListener,
    var list: ArrayList<ChildInfo>, var fromReports: Boolean = false
) : RecyclerView.Adapter<PaymentDetailAdapterNew.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemChildPaymentUpdatedBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_child_payment_updated,
            parent,
            false
        )
        return ViewHolder(binding, context, this@PaymentDetailAdapterNew)
    }

    fun setItems(mList: ArrayList<ChildInfo>) {
        list = mList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list, position, context, clickListeners, fromReports)
    }
//    fun setItems(mList: ArrayList<InvoiceDetails>) {
//        list.clear()
//        list.addAll(mList)
//    }
//    fun updateAdapter(list: ArrayList<InvoiceDetails>){
//
//        this.list = list
//        notifyDataSetChanged()
//    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(
        val binding: ItemChildPaymentUpdatedBinding,
        val context: BaseActivity,
        val adapterNew: PaymentDetailAdapterNew
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(
            list: ArrayList<ChildInfo>,
            position: Int,
            context: BaseActivity,
            clickListeners: ClickListener,
            fromReports: Boolean
        ) {

            try {
                itemView.setOnClickListener {
                    clickListeners.onItemClick(adapterPosition)
                }
                binding.ivCardWallet.setOnClickListener {
                    clickListeners.onCardClick(adapterPosition)
                }
                val child = list[position]
                if (child.childLatestInvoice?.size ?: 0 > 0) {
                    val item = child.childLatestInvoice?.first()

                    val dueDate = DateConverter.DateFormatSix(item?.dueDate.toString())
                    binding.tvDueOnDate.text = dueDate

                    if (item?.billType == "payment") {
                        binding.tvLastPayment.text = "Last Payment : $" + item.amount.toString()
                        binding.tvPaymentAmount.text = "$" + item.amount.toString()
                    } else {
                        binding.tvLastPayment.text = "-"
                        binding.tvPaymentAmount.text = "-"
                    }


                    if (item?.totalBalance ?: 0 > 0) {
                        binding.tvBalanceAmount.text = "$${item?.totalBalance}"
                        binding.tvBalanceAmount.textColor = context.resources.getColor(R.color.red)

                        //Usama Edit
                        binding.tvTotalDueAmount.text = "$" + item?.amount.toString()

                    } else {

                        //Usama Edit
                        if (item?.billType == "bill&recursive" || item?.billType == "bill") {
                            binding.tvTotalDueAmount.text = "$" + item.amount.toString()
                            binding.tvPaymentAmount.text = "-"
                        } else {
                            binding.tvPaymentAmount.text = "$" + item?.amount.toString()
                            binding.tvTotalDueAmount.text = "-"
                        }


                        binding.tvBalanceAmount.text = "$${abs(item?.totalBalance ?: 0)}"
                        binding.tvBalanceAmount.textColor =
                            context.resources.getColor(R.color.green)
                    }

                    binding.tvBillDescription.text = item?.description

                } else {
                    binding.tvDueOnDate.text = "-"
                    binding.tvLastPayment.text = "-"
                    binding.tvBalanceAmount.text = "-"
                    binding.tvPaymentAmount.text = "-"
                    binding.tvTotalDueAmount.text = "-"
                    binding.tvBillDescription.text = "-"
                }

                if (context.userManager.user?.type == "parent" || context.userManager.user?.type == "authorized_adult") {
                    if (!fromReports)
                        binding.ivCardWallet.viewVisible()
                    else {
                        binding.ivCardWallet.viewGone()
                    }
//                    binding.ivEdit.viewGone()
                } else {
                    binding.ivCardWallet.viewGone()
                    if (child.childLatestInvoice?.size ?: 0 > 0) {
                        if (child.childLatestInvoice?.first()?.billType == "bill" || child.childLatestInvoice?.first()?.billType == "credit" || child.childLatestInvoice?.first()?.billType == "bill&recursive") {
//                            binding.ivEdit.viewVisible()
                        } else {
//                            binding.ivEdit.viewGone()
                        }
                    } else {
//                        binding.ivEdit.viewGone()
                    }
                }

                binding.tvProfileName.text = child.firstName + " " + child.lastName
                Glide.with(context).load(Constants.IMG_BASE_PATH + child.profilePicture)
                    .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfileImg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface ClickListener {
        fun onItemClick(position: Int)
        fun onCardClick(position: Int)
    }
}