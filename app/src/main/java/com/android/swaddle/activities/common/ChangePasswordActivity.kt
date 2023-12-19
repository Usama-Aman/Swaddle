package com.android.swaddle.activities.common

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.swaddle.adapters.UpdatePasswordModel
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityChangePassworBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityChangePassworBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePassworBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVar()
        listener()
    }

    private fun listener() {
        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.tvSignin.setOnClickListener {
            if (validation()) {
                resetPassword()
            }
        }
    }

    private fun initVar() {

    }

    private fun resetPassword() {
        showProgressBar(binding.tvSignin, binding.progressbar)

        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .updatePassword(
                "Bearer " + userManager.accessToken ?: "",
                binding.etOldPassword.text.toString().trim(),
                userManager.user?.email ?: "",
                binding.etNewPassword.text.toString().trim(),
                binding.etRepeatPassword.text.toString().trim()
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@ChangePasswordActivity, response.body()?.message ?: "")
                    hideProgressBar(binding.tvSignin, binding.progressbar)
                    finish()
                } else {
                    hideProgressBar(binding.tvSignin, binding.progressbar)
                    showErrorToast(this@ChangePasswordActivity, "Old Password Not correct.")
//                    try {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        showErrorToast(this@ChangePasswordActivity, jObjError.getString("message"))
//                    } catch (e: Exception) {
//                        Toast.makeText(this@ChangePasswordActivity, e.message, Toast.LENGTH_LONG)
//                            .show()
//                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ChangePasswordActivity, "Can't Connect to Server.")
                hideProgressBar(binding.tvSignin, binding.progressbar)
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

    private fun validation(): Boolean {

        if (binding.etOldPassword.text.toString().trim().isEmpty()) {
            showErrorToast(this@ChangePasswordActivity, "Please enter your Old Password!")
            return false
        }

        if (binding.etNewPassword.text.toString().trim().isEmpty()) {
            showErrorToast(this@ChangePasswordActivity, "Please enter New Password!")
            return false
        }

        if (binding.etNewPassword.text.toString().trim().length < 6) {
            showErrorToast(this@ChangePasswordActivity, "Minimum Password length should be 6!")
            return false
        }
        if (binding.etRepeatPassword.text.toString().trim().isEmpty()) {
            showErrorToast(this@ChangePasswordActivity, "Please repeat your new password!")
            return false
        }
        if (binding.etNewPassword.text.toString().trim() != binding.etRepeatPassword.text.toString()
                .trim()
        ) {
            showErrorToast(this@ChangePasswordActivity, "Password does not match!")
            return false
        }

        return true
    }
}