package com.android.swaddle.activities.common

import android.content.Intent
import android.os.Bundle
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityForgetPasswordBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ForgetPasswordResponse
import com.android.swaddle.networkManager.RetrofitClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listener()
        focusChanges()
    }

    private fun listener() {
//        binding.btnResetPassword.setOnClickListener {
//            val intent = Intent(this, VerifyOtpActivity::class.java)
//            startActivity(intent)
//        }
//        binding.imgViewBack.setOnClickListener {
//            finish()
//        }

        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.lnrForgetLabel.setOnClickListener {
            val intent = Intent(this, RecoverAccountPhoneActivity::class.java)
            startActivity(intent)
        }

        binding.tvResetPassword.setOnClickListener {
            if (validation()) {
                callAPItoResetPass(
                    binding.etEmail.text.toString().trim()
                )
            }
        }
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
//                    showSuccessToast(this@ForgetPasswordActivity, response.body()?.message ?: "")
                    hideProgressBar()

                    if (response.body()?.status == true) {
                        val intent =
                            Intent(this@ForgetPasswordActivity, VerifyOtpActivity::class.java)
                        intent.putExtra("code", "${response.body()?.data?.code}")
                        intent.putExtra("email",email)
                        startActivity(intent)
                    }
                } else {
                    hideProgressBar()
                    showErrorToast(this@ForgetPasswordActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ForgetPasswordResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ForgetPasswordActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun init() {

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
            showErrorToast(this@ForgetPasswordActivity, "No internet connection!")
            return false
        }

        val email = binding.etEmail.text.toString()
        if (email.isBlank()) {
//            binding.etEmail.error = "Please enter email address"
            binding.etEmail.requestFocus()
            showErrorToast(this@ForgetPasswordActivity, "Please enter email address")
            return false
        }
        if (!email.isValidEmail()) {
//            binding.etEmail.error = "Please enter VALID email address"
            binding.etEmail.requestFocus()
            showErrorToast(this@ForgetPasswordActivity, "Please enter email address")
            return false
        }
        return true
    }

    private fun focusChanges() {
        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusIn(binding.etEmail, R.drawable.ic_pers, this)
            } else {
                focusOut(binding.etEmail, R.drawable.ic_pers, this)
            }
        }
    }
}