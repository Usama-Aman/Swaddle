package com.android.swaddle.fragments.payment_fragment

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.FragmentPayBillPopupBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PayBillPopup : DialogFragment() {
    private lateinit var binding: FragmentPayBillPopupBinding
    var baseActivity: BaseActivity? = null
    var clickListeners: ClickListener? = null
    var childInfo: ChildInfo? = null
    var childInvoice: ChildLatestInvoice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childInfo = arguments?.getSerializable("childInfo") as ChildInfo?
        if (childInfo?.childLatestInvoice?.isNotEmpty() == true)
            childInvoice = childInfo?.childLatestInvoice?.first()
    }

    companion object {

        fun getInstance(childInfo: ChildInfo): PayBillPopup {
            val payBillPopup = PayBillPopup()
            payBillPopup.isCancelable = false
            var bundle = Bundle()
            bundle.putSerializable("childInfo", childInfo)
            payBillPopup.arguments = bundle
            return payBillPopup
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPayBillPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.ivCross.setOnClickListener {
            dismiss()
        }
        listener()
    }

    private fun callAPItoPayBill(invoice_id: Int) {
        val call: Call<ChildesResponse> =
            RetrofitClass.getInstance().webRequestsInstance.payBills(
                "Bearer " + baseActivity!!.userManager.accessToken ?: "",
                invoice_id.toString(),
                binding.etAmount.text.trim().toString(), (childInfo?.id ?: 0).toString()
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(baseActivity!!, response.body()?.message ?: "")
                    dismiss()
                    clickListeners?.onBillPayed(this@PayBillPopup)
                } else {
                    showErrorToast(baseActivity!!, response.body()?.message ?: "")
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                showErrorToast(baseActivity!!, "Can't Connect to Server!")
                baseActivity?.hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun listener() {
        binding.tvSave.setOnClickListener {
            if (validate()) {
                showProgressBar()
                callAPItoPayBill(childInvoice?.id ?: 0)
            }
        }
    }

    private fun showProgressBar() {
        binding.tvSave.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSave.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun validate(): Boolean {

        if (!baseActivity!!.isNetworkConnected()) {
            showErrorToast(baseActivity!!, "No internet connection!")
            return false
        }
        if (binding.etAmount.text.toString().isEmpty()) {
            showErrorToast(baseActivity!!, "Please enter amount.")
            return false
        }
//        if (binding.etAmount.text.toString().toInt() > childInvoice?.totalBalance ?: 0) {
//            showErrorToast(baseActivity!!, "Amount should be less than your balance")
//            return false
//        }
        return true
    }

    interface ClickListener {
        fun onBillPayed(dialogFragment: DialogFragment?)
        fun onCancelClicked()
    }
}