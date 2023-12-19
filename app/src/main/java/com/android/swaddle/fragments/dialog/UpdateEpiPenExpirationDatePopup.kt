package com.android.swaddle.fragments.dialog

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.FragmentUpdateEpiPenExpirationPopupBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.DateConverter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class UpdateEpiPenExpirationDatePopup : DialogFragment() {
    private lateinit var binding: FragmentUpdateEpiPenExpirationPopupBinding

    var clickListeners: ClickListener? = null
    private var picker: DatePickerDialog? = null
    var baseActivity: BaseActivity? = null
    private var mCalendar: Calendar = Calendar.getInstance()
    var selectedDate = ""

    companion object {
        var childId: Int? = 0

        fun getInstance(childId: Int?): UpdateEpiPenExpirationDatePopup {
            var epiPenAcknowledgement = UpdateEpiPenExpirationDatePopup()
            epiPenAcknowledgement.isCancelable = false
            this.childId = childId
            return epiPenAcknowledgement
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        window!!.setLayout((size.x * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window!!.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            FragmentUpdateEpiPenExpirationPopupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    private fun datePickerDialog() {
        val day: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val year: Int = mCalendar.get(Calendar.YEAR)
        picker = DatePickerDialog(
            baseActivity!!, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->

                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                binding.tvDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)
                selectedDate = DateConverter.formatePickerDateNEw(mCalendar.time)


            }, year, month, day
        )

        picker?.datePicker?.minDate = Calendar.getInstance().timeInMillis
        picker?.show()
    }

    private fun callAPItoUpdateDate() {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .updateEpiPenExpirationDate(
                "Bearer " + baseActivity?.userManager?.accessToken ?: "",
                "$childId", selectedDate
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideProgressBar()
                if (response.body()?.status == true) {
                    clickListeners?.onUpdated(this@UpdateEpiPenExpirationDatePopup)
                    showSuccessToast(baseActivity!!, response.body()?.message ?: "")
                } else {
                    showErrorToast(baseActivity!!, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(baseActivity!!, "Can't Connect to Server!")
                hideProgressBar()
                t.printStackTrace()
            }
        })
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
        binding.lnrDate.setOnClickListener {
            datePickerDialog()
        }
        binding.tvUpdate.setOnClickListener {
            if (validate())
                callAPItoUpdateDate()
        }
    }

    private fun showProgressBar() {
        binding.tvUpdate.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvUpdate.viewVisible()
        binding.progressbar.viewGone()
    }

    interface ClickListener {
        fun onUpdated(dialogFragment: DialogFragment?)
        fun onCancelClicked()
    }


    private fun validate(): Boolean {

        if (baseActivity?.isNetworkConnected() != true) {
            showErrorToast(baseActivity!!, "No internet connection!")
            return false
        }

        if (selectedDate == "") {

            showErrorToast(baseActivity!!, "Please select new expiration date.")
            return false
        }
        return true
    }
}