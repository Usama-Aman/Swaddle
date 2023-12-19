package com.android.swaddle.activities.providers


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.android.swaddle.R
import com.android.swaddle.activities.common.ForgetPasswordActivity
import com.android.swaddle.activities.common.SelectorActivity
import com.android.swaddle.activities.parent.AddChildActivity
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.other_reg_screens.CenterRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityLoginBinding

import com.android.swaddle.enums.UserType
import com.android.swaddle.helper.*
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listeners()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun listeners() {
        binding.tvSignin.setOnClickListener {
            if (validation()) {
//                callToSigninAPi()
                callAPItoLogin(
                    binding.etEmail.text.toString().trim(),
                    binding.etPassword.text.toString().trim()
                )
            }
        }

        binding.tvForget.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SelectorActivity::class.java))
        }

        binding.etEmail.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusIn(binding.etEmail, R.drawable.ic_email, this)
            } else {
                focusOut(binding.etEmail, R.drawable.ic_email, this)
            }
        }

        binding.etPassword.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusInTil(binding.tilPassword, R.drawable.ic_lock_new, this)
            } else {
                focusOutTil(binding.tilPassword, R.drawable.ic_lock, this)
            }
        }
    }

    private fun callToSigninAPi() {

        showProgressBar()
        Handler().postDelayed(
            {
                showSuccessToast(this@LoginActivity, "Sign in successfully")
                hideProgressBar()
                if (binding.etEmail.text.toString() == "provider@gmail.com") {
                    UserData.saveUserType(UserType.PROVIDER.type, this@LoginActivity)
                } else if (binding.etEmail.text.toString() == "parent@gmail.com") {
                    UserData.saveUserType(UserType.PARENT.type, this@LoginActivity)
                }
                startActivity(Intent(this, ProviderMainActivity::class.java))
            }, 3000
        )
    }

    private fun init() {
        onSetupViewGroup(findViewById(R.id.content))
    }

    private fun validation(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@LoginActivity, "No internet connection!")
            return false
        }

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        if (email.isBlank()) {
//            binding.etEmail.error = "Please enter email address"
            binding.etEmail.requestFocus()
            showErrorToast(this@LoginActivity, "Please enter email address")
            return false
        }
        if (!email.isValidEmail()) {
//            binding.etEmail.error = "Please enter valid email address"
            binding.etEmail.requestFocus()
            showErrorToast(this@LoginActivity, "Please enter a valid email address")
            return false
        }

        if (password.isEmpty()) {
//            binding.etPassword.error = "Please enter password"
            binding.etPassword.requestFocus()
            showErrorToast(this@LoginActivity, "Please enter password")
            return false
        }
        if (password.length < 6) {
//            binding.etPassword.error = "Minimum Password length should be 6"
            binding.etPassword.requestFocus()
            showErrorToast(this@LoginActivity, "Minimum Password length should be 6")
            return false
        }
        return true
    }

    private fun showProgressBar() {
        binding.tvSignin.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSignin.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun callAPItoLogin(
        email: String, password: String
    ) {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .loginUser(email, password)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    UserData.saveUserData(
                        response.body()?.data ?: LoginData(),
                        this@LoginActivity
                    )
//                    showSuccessToast(this@LoginActivity, response.body()?.message ?: "")
                    hideProgressBar()
                    when (response.body()?.data?.user?.type) {
                        Constants.provider -> {
                            if (response.body()?.data?.user?.position == null) {
                                startActivity<ProviderRegistrationActivity>()
                                finishAffinity()
                            } else if (response.body()?.data?.user?.centerInfo == null) {
                                startActivity<CenterRegistrationActivity>()
                                finishAffinity()
                            } else {
                                startActivity<ProviderMainActivity>()
                                UserData.setUserLogin(this@LoginActivity, true)
                                finishAffinity()
                            }
                            UserData.saveUserType(UserType.PROVIDER.type, this@LoginActivity)
                        }
                        Constants.staff -> {
                            UserData.saveUserType(UserType.STAFF.type, this@LoginActivity)
                            if (response.body()?.data?.user?.gender == null) {
                                startActivity<StaffRegistrationActivity>()
                                finishAffinity()
                            } else {
                                startActivity<ProviderMainActivity>()
                                UserData.setUserLogin(this@LoginActivity, true)
                                finishAffinity()
                            }
                        }
                        Constants.parent -> {
                            UserData.saveUserType(UserType.PARENT.type, this@LoginActivity)
                            if (response.body()?.data?.user?.gender == null) {
                                startActivity<ParentSignUpActivity>()
                                finishAffinity()
                            } else if (response.body()?.data?.user?.childInfo?.size ?: 0 == 0) {
                                startActivity<AddChildActivity>()
                            } else {
                                startActivity<ProviderMainActivity>()
                                UserData.setUserLogin(this@LoginActivity, true)
                                finishAffinity()
                            }
                        }
                        else -> {
                            UserData.saveUserType(
                                UserType.AUTHORIZED_ADULT.type,
                                this@LoginActivity
                            )
                            if (response.body()?.data?.user?.gender == null) {
                                startActivity<ParentSignUpActivity>()
                                finishAffinity()
                            } else {
                                startActivity<ProviderMainActivity>()
                                UserData.setUserLogin(this@LoginActivity, true)
                                finishAffinity()
                            }
                        }
                    }
                } else {
                    hideProgressBar()
                    showErrorToast(this@LoginActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@LoginActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }
}