package com.android.swaddle.activities.common

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.adapters.transaction_history_adapter.TransactionHistoryAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityTransactionHistoryBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ChildLatestInvoice
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.GetInvoicesResponse
import com.android.swaddle.payment_screens.AddBillActivity
import com.android.swaddle.payment_screens.AddCreditActivity
import com.android.swaddle.utils.Constants
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.jetbrains.anko.startActivity
import org.json.JSONObject

class TransactionHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private var adapter: TransactionHistoryAdapter? = null
    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    var fromReports = false
    var timePeriod = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            selectedChild = intent.getSerializableExtra("selectedChild") as ChildInfo?
            selectedClassRoom =
                intent.getSerializableExtra("selectedClassRoom") as ClassroomDetails?
            fromReports = intent.getBooleanExtra("selectedClassRoom", false)
            timePeriod = intent.getStringExtra("timePeriod") ?: "week"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listener()
        (selectedChild?.firstName + " " + selectedChild?.lastName + "'s Transaction History").also {
            binding.tvHead.text = it
        }

        callAPItoGetLatestInvoicesList()
    }

    private fun setTransactionHistoryRec(childLatestInvoice: ArrayList<ChildLatestInvoice>) {

        if (adapter == null) {
            adapter =
                TransactionHistoryAdapter(
                    this@TransactionHistoryActivity,
                    object : TransactionHistoryAdapter.ClickListener {
                        override fun onItemClick(position: Int) {

                        }

                        override fun onEditClick(position: Int, payment: ChildLatestInvoice) {
                            if (payment.billType == "bill" || payment.billType == "bill&recursive") {
                                startActivity<AddBillActivity>(
                                    "selectedChild" to selectedChild,
                                    "selectedClass" to selectedClassRoom,
                                    "item" to payment,
                                    "update" to true
                                )
                            } else if (payment.billType == "credit") {
                                startActivity<AddCreditActivity>(
                                    "selectedChild" to selectedChild,
                                    "selectedClass" to selectedClassRoom,
                                    "item" to payment,
                                    "update" to true
                                )
                            }
                        }
                    },
                    childLatestInvoice, fromReports
                )
            binding.rec.layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            binding.rec.adapter = adapter
        } else {
            adapter?.setItems(childLatestInvoice)
        }
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun callAPItoGetLatestInvoicesList() {
        showLoading()
        var path = Constants.SERVER_ADDRESS_NEW + "get_invoices"
        Log.e("path", path)
        var api =
            AndroidNetworking.get(path)
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
        api.addQueryParameter("child_id", (selectedChild?.id ?: 0).toString())
//        if (userManager.user?.type != "parent" && userManager.user?.type != "authorized_adult")
        api.addQueryParameter("classroom_id", (selectedClassRoom?.id ?: 0).toString())
        api.addQueryParameter("time_period", timePeriod)

        api.setTag("get_invoices")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    hideLoading()
                    Log.e("get_invoices", response.toString())

                    var data =
                        gson.fromJson(response.toString(), GetInvoicesResponse::class.java)

                    if (data.status == true) {
                        setTransactionHistoryRec(
                            data.data?.childesData?.first()?.childLatestInvoice ?: ArrayList()
                        )
                    } else {
//                        showErrorToast(this@TransactionHistoryActivity, data.message ?: "")
                    }

                    if (data.data?.childesData?.first()?.childLatestInvoice?.isEmpty() == true)
                        binding.tvNoDataFound.viewVisible()
                    else
                        binding.tvNoDataFound.viewGone()

                    // do anything with response
                    hideLoading()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideLoading()
                    error.printStackTrace()
//                    showErrorToast(this@TransactionHistoryActivity, error.message ?: "")
                }
            })
    }
}