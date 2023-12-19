package com.android.swaddle.fragments.providers

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.activities.providers.AddActivity
import com.android.swaddle.activities.providers.ProviderChildHelperActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.BottomSheetStaffBinding
import com.android.swaddle.databinding.FragmentDashboardBinding
import com.android.swaddle.fragments.bottomSheetFragment.ManageStaffBottomSheet
import com.android.swaddle.fragments.dialog.StaffDismissalPopup
import com.android.swaddle.helper.*
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.models.User
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.signin_signout.SignInSignOutBottomSheet
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.ProgressDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardFragment : BaseFragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val pd = ProgressDialog.newInstance()

    val userManager: LoginData
        get() {
            return UserData.user(requireContext())
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListener()
        initVar()
    }

    private fun clickListener() {
        binding.cvAttandance.setOnClickListener {
            if (mActivity.isNetworkConnected()) {
                startActivity(Intent(context, ProviderChildHelperActivity::class.java))
            } else {
                CustomToasts.failureToast(mActivity, "No internet connection!")
            }
        }
        binding.ivCross.setOnClickListener {
            binding.rlWarnings.viewGone()
        }
        binding.cvdailyreport.setOnClickListener {
            if (mActivity.isNetworkConnected()) {
                startActivity(Intent(context, DailyReportActivity::class.java))
            } else {
                CustomToasts.failureToast(mActivity, "No internet connection!")
            }
        }

        binding.cvActivity.setOnClickListener {
            startActivity(Intent(context, AddActivity::class.java))
        }

        binding.cvSignInSignOut.setOnClickListener {
            showSignInSignOutBottomSheet()
        }

        if (userManager.user?.type == Constants.staff) {
            binding.tvMarkMyAttendance.viewVisible()
            binding.tvMarkMyAttendance.setOnClickListener {
                staffLogin()
            }
        } else {
            binding.tvMarkMyAttendance.viewGone()
        }

    }

    private fun showSignInSignOutBottomSheet() {
        val sheet = SignInSignOutBottomSheet(mActivity)
        sheet.show(activity?.supportFragmentManager!!, "SignInSignOutBottomSheet")
    }

    private fun initVar() {
        if (mActivity.userManager.user?.isProviderPremium == 1) {
            when (mActivity.userManager.user?.providerSubscription?.status) {
                "trial" -> {
                    binding.rlWarnings.viewVisible()
                    binding.tvWarning.text = "Day care center is on trial period of 7 days"
                }
                "expire_soon" -> {//14Days
                    binding.rlWarnings.viewVisible()
                    binding.tvWarning.text =
                        "Payments pending! We will limit the functionality after 14 day."
                }
                else -> {
                    binding.rlWarnings.viewGone()
                }
            }
        } else {
            binding.rlWarnings.viewVisible()
            binding.tvWarning.text = "Day care services are limited due to pending payments"
        }
    }

    private fun staffLogin() {
        showBottomSheetDialog(userManager.user!!)
    }

    private fun showBottomSheetDialog(user: User) {
        val mBinding: BottomSheetStaffBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.bottom_sheet_staff,
            null,
            false
        )
        var otherText = ""
        var text = ""
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(mBinding.root)

        if (user.attendance?.size ?: 0 > 0)
            when (user.attendance?.first()?.status) {
                "absent" -> {
                    mBinding.rbAbsent.isChecked = true
                    text = mBinding.rbAbsent.text.toString()
                    mBinding.rgOthers.viewGone()
                }
                "signIn" -> {
                    mBinding.rbSignIn.isChecked = true
                    text = mBinding.rbSignIn.text.toString()
                    mBinding.rgOthers.viewGone()
                }
                "signOut" -> {
                    mBinding.rbSignOut.isChecked = true
                    text = mBinding.rbSignOut.text.toString()
                    mBinding.rgOthers.viewGone()
                }

                "sick" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbSick.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "vacation" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbVacation.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "appointment" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbDocAppointment.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "late" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbLate.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
            }

        mBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == mBinding.rbOther.id) {
                mBinding.rgOthers.viewVisible()
            } else {
                mBinding.rgOthers.viewGone()
            }

            when (checkedId) {
                mBinding.rbSignOut.id -> {
                    text = "Sign out"
                }
                mBinding.rbSignIn.id -> {
                    text = "Sign in"
                }
                mBinding.rbAbsent.id -> {
                    text = "Absent"
                }
                mBinding.rbOther.id -> {
                    text = "Other"
                }
            }
        }

        mBinding.rgOthers.setOnCheckedChangeListener { group, checkedId ->
            mBinding.rbOther.isChecked = true
            when (checkedId) {
                mBinding.rbSick.id -> {
                    otherText = "sick"
                    text = otherText
                }
                mBinding.rbVacation.id -> {
                    otherText = "vacation"
                    text = otherText
                }
                mBinding.rbDocAppointment.id -> {
                    otherText = "appointment"
                    text = otherText
                }
                mBinding.rbLate.id -> {
                    otherText = "late"
                    text = otherText
                }
            }
        }

        mBinding.ivCross.setOnClickListener {
            dialog.dismiss()
        }
        mBinding.tvUpdate.setOnClickListener {
            if (validateAttendance(text))
                callAPItoUpdateAttendance(
                    mBinding.tvUpdate,
                    mBinding.progressbar, dialog, text, (user.id ?: 0).toString()
                )
        }
        dialog.show()
    }

    private fun validateAttendance(text: String): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(requireContext(), "No internet connection!")
            return false
        }

        if (text.isEmpty()) {
            showErrorToast(requireContext(), "Please select attendance!")
            return false
        }

        return true
    }

    private fun callAPItoUpdateAttendance(
        tv: TextView,
        progressbar: ProgressBar,
        alertDialog: BottomSheetDialog,
        status: String,
        staff_id: String
    ) {
        showProgressBar(tv, progressbar)
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .staffAttendance(
                "Bearer " + userManager.accessToken ?: "",
                staff_id,
                status
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(requireContext(), response.body()?.message ?: "")
                    hideProgressBar(tv, progressbar)
                    alertDialog.dismiss()
                } else {
                    try {
                        hideProgressBar(tv, progressbar)
                        showErrorToast(requireContext(), response.body()?.message ?: "")

//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        showErrorToast(this@StaffActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        Log.d("Staff Attendance", e.printStackTrace().toString())
//                        Toast.makeText(this@StaffActivity, e.message, Toast.LENGTH_LONG)
//                            .show()
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(requireContext(), "Can't Connect to Server!")
                hideProgressBar(tv, progressbar)
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun showProgressBar(tvSignin: TextView, progressbar: ProgressBar) {
        tvSignin.viewGone()
        progressbar.viewVisible()
    }

    private fun hideProgressBar(tvSignin: TextView, progressbar: ProgressBar) {
        tvSignin.viewVisible()
        progressbar.viewGone()
    }

    fun hideLoading() {
        activity?.runOnUiThread {
            try {
                if (pd.isAdded) pd.dismiss()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun isNetworkConnected(): Boolean {
        val cm: ConnectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }


}