package com.android.swaddle.activities.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityRecoverAccountPhoneBinding
import com.android.swaddle.fragments.dialog.CountryPickerDialogue
import com.android.swaddle.helper.*
import com.android.swaddle.listeners.CountryData
import com.android.swaddle.models.ForgetPasswordResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.ybs.countrypicker.CountryPicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecoverAccountPhoneActivity : BaseActivity() {
    private lateinit var binding: ActivityRecoverAccountPhoneBinding
    private lateinit var countryPicker: CountryPicker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverAccountPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        clickListener()

    }

    private fun showProgressBar() {
        binding.tvResetPassword.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvResetPassword.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@RecoverAccountPhoneActivity, "No internet connection!")
            return false
        }
        val email = binding.etContactNo.text.toString()
        if (email.isBlank()) {
            showErrorToast(this@RecoverAccountPhoneActivity, "Please enter phone no")
            return false
        }
        return true
    }

    private fun clickListener() {
        binding.tvResetPassword.setOnClickListener {
            if (validation()) {
                callAPItoResetPass(
                    binding.etContactNo.text.toString().trim()
                )
            }
        }

        binding.tvCode.setOnClickListener {
            val bottomSheetDialog = CountryPickerDialogue.newInstance()
            bottomSheetDialog.setListener(object : CountryData {
                override fun getSelectedCountryData(position: Int, countryCode: String) {
                    binding.tvCode.text = countryCode
                }
            })
            bottomSheetDialog.show(
                supportFragmentManager,
                CountryPickerDialogue::class.java.canonicalName
            )
        }
        binding.lnrForgetLabel.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.imgViewBack.setOnClickListener {
            finish()
        }
    }

    private fun initVar() {
        countryPicker = CountryPicker.newInstance("Select Country")
    }

    private fun callAPItoResetPass(
        email: String
    ) {
        showProgressBar()
        val call: Call<ForgetPasswordResponse> = RetrofitClass.getInstance().webRequestsInstance
            .resetPassword(email)
        call.enqueue(object : Callback<ForgetPasswordResponse> {
            override fun onResponse(
                call: Call<ForgetPasswordResponse>,
                response: Response<ForgetPasswordResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(
                        this@RecoverAccountPhoneActivity,
                        response.body()?.message ?: ""
                    )
                    hideProgressBar()

                    if (response.body()?.status == true) {
                        val intent =
                            Intent(this@RecoverAccountPhoneActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("code", "${response.body()?.data?.code}")
                        startActivity(intent)
                    }
                } else {
                    hideProgressBar()
                    showErrorToast(this@RecoverAccountPhoneActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ForgetPasswordResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@RecoverAccountPhoneActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }
}