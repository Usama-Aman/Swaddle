package com.android.swaddle.adapters.transaction_history_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemTransactionHistoryBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildLatestInvoice
import com.android.swaddle.models.LoginData
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import kotlin.math.abs

class TransactionHistoryAdapter(
    var context: BaseActivity,
    var clickListeners: ClickListener,
    var list: ArrayList<ChildLatestInvoice>,
    var fromReports: Boolean
) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    val userManager: LoginData
        get() {
            return UserData.user(mContext)
        }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemTransactionHistoryBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_transaction_history,
            parent,
            false
        )
        return ViewHolder(binding, context, this@TransactionHistoryAdapter)
    }

    fun setItems(list: ArrayList<ChildLatestInvoice>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list, position, context, clickListeners, fromReports)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(
        val binding: ItemTransactionHistoryBinding,
        val context: Context,
        val adapter: TransactionHistoryAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(
            list: ArrayList<ChildLatestInvoice>,
            position: Int,
            context: BaseActivity,
            clickListeners: ClickListener,
            fromReports: Boolean
        ) {
            var payment = list[position]

            if (payment.billType == "bill&recursive" || payment.billType == "bill") {
                if (payment.amount != null)
                    binding.tvDueAmount.text = "$${payment.amount}"
                else
                    binding.tvDueAmount.text = "-"

                binding.tvAmount.text = "-"
            } else {
                if (payment.amount != null)
                    binding.tvAmount.text = "$${payment.amount}"
                else
                    binding.tvAmount.text = "-"

                binding.tvDueAmount.text = "-"
            }

//            if (payment.totalBalance ?: 0 > 0) {
//                binding.tvDueAmount.text = "$${payment.totalBalance}"
//            } else {
//                binding.tvDueAmount.text = "0"
//            }


            binding.tvDate.text = DateConverter.convertGenericDate(
                payment.dueDate ?: "",
                "yyyy-MM-dd hh:mm:ss",
                "MMM dd, yyyy"
            )
            binding.ivDropDown.setOnClickListener {
                payment.expanded = !payment.expanded
                binding.expandLayout.toggle()
                if (binding.expandLayout.isExpanded) {
                    binding.ivDropDown.scaleY = -1f
//                    binding.viewMiddle.viewVisible()
                } else {
                    binding.ivDropDown.scaleY = 1f
//                    binding.viewMiddle.viewGone()
                }
            }
            binding.tvTransactionID.text = "${payment.transactionId}"

            if (payment.totalBalance ?: 0 < 0) {
                binding.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.deep_sea))
                if (payment.totalBalance != null)
                    binding.tvBalance.text = "$${abs(payment.totalBalance ?: 0)}"
                else
                    binding.tvBalance.text = "-"

            } else {
                binding.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.red))
                if (payment.totalBalance != null)
                    binding.tvBalance.text = "$${abs(payment.totalBalance ?: 0)}"
                else
                    binding.tvBalance.text = "-"
            }


            if (payment.status == "completed") {
                binding.tvAmount.setTextColor(context.resources.getColor(R.color.deep_sea))
                binding.tvStatusUpdate.setTextColor(context.resources.getColor(R.color.deep_sea))
                binding.tvStatusUpdate.text = "Sent"
                binding.ivDropDown.viewVisible()
                binding.tvTransactionID.text = payment.transactionId ?: ""
                binding.tvPaymentType
            } else {

                //Usama Edit
                binding.tvAmount.setTextColor(context.resources.getColor(R.color.deep_sea))
                binding.tvStatusUpdate.setTextColor(context.resources.getColor(R.color.deep_sea))


//                binding.tvAmount.setTextColor(context.resources.getColor(R.color.red))
//                binding.tvStatusUpdate.setTextColor(context.resources.getColor(R.color.colorYellow))
                binding.ivDropDown.viewGone()


                binding.tvDue.text = "Due"
                if (payment.billType == "bill") {
                    binding.tvStatusUpdate.setTextColor(context.resources.getColor(R.color.colorYellow))
                    binding.tvStatusUpdate.text = "Billed"
                } else {
//                    binding.tvStatusUpdate.text = "Due"
                    //              Status for a payment is also sent changed by awais and naeem
                    binding.tvStatusUpdate.setTextColor(context.resources.getColor(R.color.deep_sea))
                    binding.tvStatusUpdate.text = "Sent"
                }
            }
            binding.ivEdit.setOnClickListener {
                clickListeners.onEditClick(position, payment)
            }
            if (context.userManager.user?.type == "parent" || context.userManager.user?.type == "authorized_adult") {
                binding.ivEdit.viewGone()
            } else {
                if (payment.billType == "bill" || payment.billType == "credit" || payment.billType == "bill&recursive") {
                    if (!fromReports) {
                        binding.ivEdit.viewVisible()
                    } else {
                        binding.ivEdit.viewGone()
                    }
                } else {
                    binding.ivEdit.viewGone()
                }
            }
        }
    }

    interface ClickListener {
        fun onItemClick(position: Int)
        fun onEditClick(position: Int, lastPayment: ChildLatestInvoice)
    }
}