package com.android.swaddle.fragments.fragment_certifications

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.PopupCertificationsBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.CardData
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.models.PaymentCardsResponse
import com.android.swaddle.networkManager.RetrofitClass
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddCertificationsPopup() : DialogFragment() {
    private lateinit var binding: PopupCertificationsBinding
    var myCalendar = Calendar.getInstance()
    var compDate = ""
    var baseActivity: BaseActivity? = null
    var clickListeners: ClickListener? = null

    companion object {

        fun getInstance(): AddCertificationsPopup {
            val addCertification = AddCertificationsPopup()
            addCertification.isCancelable = false

            return addCertification
        }
    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.getWindowManager().getDefaultDisplay()
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window!!.setLayout((size.x * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window!!.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    private fun callAPItoAddCertification() {
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .addCertification(
                "Bearer " + baseActivity!!.userManager.accessToken ?: "",
                binding.etCert.text.trim().toString(), compDate
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        showSuccessToast(baseActivity!!, response.body()?.message ?: "")
                        dismiss()
                        clickListeners?.onCertificationAdded(this@AddCertificationsPopup)
                    } else {
                        showErrorToast(baseActivity!!, response.body()?.message ?: "")
                    }
                } else {
                    val s = JSONObject(response.errorBody()?.string())
                    showErrorToast(baseActivity!!, s.getString("message") ?: "")
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                showErrorToast(baseActivity!!, "Can't Connect to Server!")
                baseActivity?.hideLoading()
                t.printStackTrace()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = PopupCertificationsBinding.inflate(layoutInflater, container, false)
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

    private fun listener() {
        binding.llCompletionDate.setOnClickListener {
            var dpd = DatePickerDialog(
                requireContext(), R.style.MySpinnerDatePickerStyle,
                { _, year, month, dayOfMonth ->
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, month)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    var m = ""
                    var y = ""
                    var d = ""
                    m = if (month < 9) {
                        "0${month + 1}"
                    } else {
                        "${month + 1}"
                    }

                    y = if (year < 9) {
                        "0${year}"
                    } else {
                        "$year"
                    }

                    d = if (dayOfMonth < 9) {
                        "0${dayOfMonth}"
                    } else {
                        "$dayOfMonth"
                    }
//                    compDate = if (month < 9)
//                        "$year-0${month + 1}-$dayOfMonth"
//                    else
//                        "$year-${month + 1}-$dayOfMonth"
                    compDate = "$y-$m-$d"
                    binding.tvCompletionDate.text = "${month + 1} / $dayOfMonth / $year"
                }, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            var cal = Calendar.getInstance()
//            cal.add(Calendar.YEAR,-12)
//            dpd.datePicker.maxDate = cal.timeInMillis
            dpd.show()
        }

        binding.tvSave.setOnClickListener {
            if (validate()) {
                showProgressBar()
                callAPItoAddCertification()
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

    interface ClickListener {
        fun onCertificationAdded(dialogFragment: DialogFragment?)
        fun onCancelClicked()
    }

    private fun validate(): Boolean {
        if (!baseActivity!!.isNetworkConnected()) {
            showErrorToast(baseActivity!!, "No internet connection!")
            return false
        }
        if (binding.etCert.text.toString().toString().isEmpty()) {
            showErrorToast(baseActivity!!, "Please enter certification name.")
            return false
        }
        if (compDate == "") {
            showErrorToast(baseActivity!!, "Please select completion date.")
            return false
        }
        return true
    }
}