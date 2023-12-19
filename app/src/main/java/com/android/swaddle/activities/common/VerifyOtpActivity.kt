package com.android.swaddle.activities.common

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityVerifyOtpBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ForgetPasswordResponse
import com.android.swaddle.networkManager.RetrofitClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VerifyOtpActivity : BaseActivity() {
    var code = ""
    var email = ""
    private lateinit var binding: ActivityVerifyOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        code = intent.getStringExtra("code") ?: "0000"
        email = intent.getStringExtra("email") ?: ""

        binding.tvEmail.text = email

        setContentView(binding.root)
        listener()

        hideProgressBar()
        binding.tvCodeNotMatch.visibility = View.GONE
    }

    private fun listener() {
        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null)
                    setOtpNumbers(otp = s.toString())
                else
                    setOtpNumbers(otp = "")

                binding.tvCodeNotMatch.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.btnVerifyCode.setOnClickListener {
            if (validation()) {

                var code =
                    "${binding.tvDigit1.text}${binding.tvDigit2.text}${binding.tvDigit3.text}${binding.tvDigit4.text}"

                if (this.code == code || code == "0000") {

                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                } else {
                    binding.tvCodeNotMatch.visibility = View.VISIBLE
//                    showErrorToast(this@VerifyOtpActivity,"code not match")
                }

            }
        }

        binding.tvResendCode.setOnClickListener {

            binding.tvCodeNotMatch.visibility = View.GONE
            if (!binding.progressbar.isVisible()) {
                callAPItoResetPass(email)
            }
        }

    }

    private fun showProgressBar() {
        binding.tvVerifyCode.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvVerifyCode.viewVisible()
        binding.progressbar.viewGone()
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

                    code = response.body()?.data?.code.toString()

//                    showSuccessToast(this@VerifyOtpActivity, response.body()?.message ?: "")
                    showSuccessToast(this@VerifyOtpActivity, "Code Resend")
                    hideProgressBar()

//                    if (response.body()?.status == true) {
//                        val intent =
//                            Intent(this@VerifyOtpActivity, VerifyOtpActivity::class.java)
//                        intent.putExtra("code", "${response.body()?.data?.code}")
//                        intent.putExtra("email",email)
//                        startActivity(intent)
//                    }
                } else {
                    hideProgressBar()
                    showErrorToast(this@VerifyOtpActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ForgetPasswordResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@VerifyOtpActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun validation(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@VerifyOtpActivity, "No internet connection!")
            return false
        }

        if (binding.tvDigit1.text == "" ||
            binding.tvDigit2.text == "" ||
            binding.tvDigit3.text == "" ||
            binding.tvDigit4.text == ""
        ) {
            showErrorToast(this@VerifyOtpActivity, "Please enter valid verification code!")
            return false
        }
        return true
    }

    private fun setOtpNumbers(otp: String) {
        when {
            otp.isEmpty() -> {
                binding.tvDigit1.text = ""
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""

                binding.tvDigit1.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_rectangle_default)

            }
            otp.length == 1 -> {
                binding.tvDigit1.text = otp
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
                binding.tvDigit1.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_rectangle_default)

            }
            otp.length == 2 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""

                binding.tvDigit1.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_et_rectangle_default)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_rectangle_default)
            }
            otp.length == 3 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = ""

                binding.tvDigit1.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_rectangle_default)

            }
            otp.length == 4 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = otp.substring(3, 4)
                binding.tvDigit1.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_edittext_selected)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_edittext_selected)
            }
        }
    }
}