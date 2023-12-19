package com.android.swaddle.adapters.payment_adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemChildPaymentsBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.payment_screens.AddBillActivity
import org.jetbrains.anko.startActivity


class PaymentDetailAdapter(
    private val context: Context,
    var list: ArrayList<ChildInfo>,
    var clickListeners: ClickListener,
    var fragmentManager: FragmentManager
) :
    RecyclerView.Adapter<PaymentDetailAdapter.PaymentDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentDetailViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemChildPaymentsBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_child_payments,
            parent,
            false
        )
        return PaymentDetailViewHolder(
            binding,
            this@PaymentDetailAdapter,
            context, fragmentManager
        )
    }

    fun updateAdapter(list: ArrayList<ChildInfo>) {
//        this.list.clear()
//        this.list.addAll(list)
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PaymentDetailViewHolder, position: Int) {
        holder.onBind(list, position, context, clickListeners)
//        holder.listener()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(mList: ArrayList<ChildInfo>) {
        list.clear()
        list.addAll(mList)
    }

    class PaymentDetailViewHolder(
        private val binding: ItemChildPaymentsBinding,
        private val adapter: PaymentDetailAdapter,
        private val context: Context,
        private val fragmentManager: FragmentManager
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(
            list: ArrayList<ChildInfo>,
            position: Int,
            context: Context,
            clickListeners: ClickListener
        ) {

            try {

                var child: ChildInfo = list[position]
                var item = child.childLatestInvoice

                binding.ivEdit.visibility = if (item?.first()?.status == "pending") {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                binding.ivDropDown.visibility = if (item?.first()?.status == "completed") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                binding.ivEdit.setOnClickListener {
                    (context as Activity).startActivity<AddBillActivity>("item" to item)

                }

                binding.ivDropDown.setOnClickListener {
                    item?.first()?.expanded = !item?.first()?.expanded!!
                    binding.expandLayout.toggle()
                    if (binding.expandLayout.isExpanded) {
                        binding.ivDropDown.scaleY = -1f
                        binding.viewMiddle.viewVisible()
                    } else {
                        binding.ivDropDown.scaleY = 1f
                        binding.viewMiddle.viewGone()
                    }
                }

                binding.tvHead.text = item?.first()?.description

                /* if (item.payments != null) {
 //                    binding.tvDate.text = DateConverter.DueDateTimeStamp(item.dueDate ?: "")
 //                    binding.tvDuePrice.text = "$${item.amount}"
 //                    binding.tvPaymentPrice.text = item.payments!![0].getAmount().toString()
 //                    binding.tvBalancePrice.text = "$${item.payments!![0].getAmount()}"
 //                    binding.tvType.text = "${item.payments!![0].getPaymentMethod()}"
 //                    binding.tvSentDate.text = DateConverter.DueDateTimeStamp(item.payments!![0].getCreatedAt()?: "")
 //
 //                    binding.tvToAccount.text = "${item.payments!![0].getToAccount()?:""}"
 //                    binding.tvMemo.text = "${item.payments!![0].getMemo()}"

                 } else {

                 }*/
                if (item?.first()?.filePath != null) {


                    val dataList = (item.first().filePath ?: "").split(",") as ArrayList<*>

                    Log.i("jfilePath", dataList.size.toString() ?: "")


                    val adapter = FilesAdapter(context, dataList as ArrayList<String>, true)
                    adapter.setListener(object : FilesAdapter.CLickListener {
                        override fun onItemClick(pos: Int) {

                        }

                        override fun onRemoveClick(pos: Int) {

                        }

                        override fun onDownloadClick(dPos: Int) {
                            clickListeners.onDownloadClicked(position, dataList[dPos])
                        }
                    })
                    binding.rvFiles.adapter = adapter
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        fun openBottomSheet(){
//            var sheet = ClassroomOptionsBottomSheet()
//            sheet.show(fragmentManager, "VideoViewsBottomSheet")
//
//        }

//            fun listener() {
//                binding.ivDropDown.setOnClickListener {
//                    binding.expandLayout.toggle()
//                    if (binding.expandLayout.isExpanded) {
//                        binding.ivDropDown.scaleY = -1f
//                        binding.viewMiddle.viewVisible()
//                    } else {
//                        binding.ivDropDown.scaleY = 1f
//                        binding.viewMiddle.viewGone()
//                    }
//                }
//            }
    }


    interface ClickListener {
        fun onITemClick(position: Int, item: ChildInfo)
        fun onEditClicked(position: Int, item: ChildInfo)
        fun onDownloadClicked(position: Int, path: String)
    }
}