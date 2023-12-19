package com.android.swaddle.activities.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityContactSupportBinding
import com.android.swaddle.databinding.ActivitySetingBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.GetSummaryReportResponse
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactSupportActivity : BaseActivity() {

    private lateinit var binding: ActivityContactSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.tvSubmit.setOnClickListener {
            if (validation()
            ) {
                callAPItoSubmit()
            }
        }
    }

    private fun callAPItoSubmit(
    ) {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .contactToSupport(
                "Bearer " + userManager.accessToken ?: "", userManager.user?.email ?: "",
                "${binding.etName.text}", "${binding.etDesc.text}"
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                hideProgressBar()
                if (response.body()?.status == true) {
                    showSuccessToast(this@ContactSupportActivity, response.body()?.message ?: "")
                    finish()
                } else {
                    showErrorToast(this@ContactSupportActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                showErrorToast(this@ContactSupportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@ContactSupportActivity, "No internet connection!")
            return false
        }

        val email = binding.etName.text.toString()
        val password = binding.etDesc.text.toString()

        if (email.isBlank()) {
            showErrorToast(this@ContactSupportActivity, "Please enter your Name or Subject!")
            return false
        }

        if (password.isBlank()) {
            showErrorToast(this@ContactSupportActivity, "Please explain your Subject!")
            return false
        }
        return true
    }

    private fun showProgressBar() {
        binding.tvSubmit.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSubmit.viewVisible()
        binding.progressbar.viewGone()
    }
}