

package com.android.swaddle.activities.common

import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.R
import com.android.swaddle.activities.providers.LoginActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityResetPasswordBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ForgetPasswordResponse
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)

        email = intent.getStringExtra("email")?: ""

        setContentView(binding.root)
        init()
        listener()
        focusChanged()
    }

    private fun focusChanged() {
        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusInTil(binding.tilPassword, R.drawable.ic_lock, this)
            } else {
                focusOutTil(binding.tilPassword, R.drawable.ic_lock, this)
            }
        }

        binding.etConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusInTil(binding.tilConfirmPass, R.drawable.ic_lock, this)
            } else {
                focusOutTil(binding.tilConfirmPass, R.drawable.ic_lock, this)
            }
        }
    }

    private fun callAPItoResetPass(
        email: String, password: String
    ) {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .forgotPassword(email,password,password)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@ResetPasswordActivity, response.body()?.message ?: "")
                    hideProgressBar()


//                    val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
//                    startActivity(intent)

                    finishAffinity()

                    val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    hideProgressBar()
                    showErrorToast(this@ResetPasswordActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ResetPasswordActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun listener() {
        binding.btnResetPassword.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.tvResetPassword.setOnClickListener {
            if (validation()) {
                callAPItoResetPass(
                    email,
                    password
                )
            }
        }

        binding.tvSignin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = binding.etPassword.text.toString()
                val resetPassword = binding.etConfirmPassword.text.toString()
                if (resetPassword == password && password.length >= 6) {
                    binding.ivTick.viewVisible()
                } else {
                    binding.ivTick.viewGone()
                }

            }

            override fun afterTextChanged(s: Editable?) {

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

    var password = "";

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@ResetPasswordActivity, "No internet connection!")
            return false
        }
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (password.isBlank()) {
            showErrorToast(this@ResetPasswordActivity, "Please enter Password")
            return false
        }
        if(password.length<6){
            showErrorToast(this@ResetPasswordActivity, "Minimum Password length should be 6")
            return false
        }
        if (password != confirmPassword) {
            showErrorToast(this@ResetPasswordActivity, "Confirm Password Does Not Match")
            return false
        }

        this.password = binding.etPassword.text.toString()

        return true
    }
}