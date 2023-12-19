package com.android.swaddle.activities.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivitySignupBinding
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

class SignupActivity : BaseActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var userType: UserType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userType = intent.getSerializableExtra("type") as UserType
        init()
        listener()
        focusChangeListener()
    }

    private fun listener() {
        binding.lnrSigninLabel.setOnClickListener {

            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }



        binding.tvSignUp.setOnClickListener {
            if (validation()) {
                callAPItoRegister()
            }
        }

        binding.tvTerms.setOnClickListener {
            try {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SERVER_ADDRESS + "term_of_use"))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(this@SignupActivity, "Failed to open URL!")
            }
//            showSuccessToast(this@SettingActivity, "Web page isPending!")
        }
    }

    private fun init() {
        if (userType == UserType.PROVIDER) {
            binding.tilCode.viewGone()
            binding.tvCode.viewGone()
        } else {
            binding.tilCode.viewVisible()
            binding.tvCode.viewVisible()
        }
    }

    private fun callAPItoRegister() {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .registerUser(
                binding.etName.text.toString().trim(),
                binding.etLastName.text.toString().trim(),
                binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString().trim(),
                binding.etPassword.text.toString().trim(),
                binding.etCode.text.toString().trim(), userType.toString().toLowerCase()
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    UserData.saveUserData(
                        response.body()?.data ?: LoginData(),
                        this@SignupActivity
                    )
                    showSuccessToast(this@SignupActivity, response.body()?.message ?: "")
                    hideProgressBar()

                    when (userType) {
                        UserType.PROVIDER -> {
                            if (response.body()?.data?.user?.phoneNumber == null) {
                                //startActivity<ProviderRegistrationActivity>()
                                startActivity<PopupActivity>()
                            } else {
                                startActivity<ProviderMainActivity>()
                            }
                            UserData.saveUserType(UserType.PROVIDER.type, this@SignupActivity)
                        }
                        UserType.STAFF -> {
                            UserData.saveUserType(UserType.STAFF.type, this@SignupActivity)
                            if (response.body()?.data?.user?.phoneNumber == null) {
                                startActivity<StaffRegistrationActivity>()
                            } else {
                                startActivity<ProviderMainActivity>()
                            }
                        }
                        UserType.PARENT -> {
                            UserData.saveUserType(UserType.PARENT.type, this@SignupActivity)
                            if (response.body()?.data?.user?.phoneNumber == null) {
                                startActivity<ParentSignUpActivity>()
                            } else {
                                startActivity<ProviderMainActivity>()
                            }
                        }
                        else -> {
                            UserData.saveUserType(
                                UserType.AUTHORIZED_ADULT.type,
                                this@SignupActivity
                            )
                            if (response.body()?.data?.user?.phoneNumber == null) {
                                startActivity<ParentSignUpActivity>()
                            } else {
                                startActivity<ProviderMainActivity>()
                            }
                        }
                    }

                    finishAffinity()

//                    if (UserData.getUserType(this) == UserType.PROVIDER.type) {
//                        val intent = Intent(this, ProviderMainActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                        val intent = Intent(this, ParentSignupActivity::class.java)
//                        startActivity(intent)
//                    }
                } else {
                    hideProgressBar()
                    showErrorToast(this@SignupActivity, response.body()?.message ?: "")
                }

                binding.tvSignUp.viewVisible()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                binding.tvSignUp.viewVisible()
                showErrorToast(this@SignupActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun validation(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@SignupActivity, "No internet connection!")
            return false
        }

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val repeatPassword = binding.etRepeatPassword.text.toString()
        val userName = binding.etName.text.toString()
        val lastName = binding.etLastName.text.toString()

        if (userName.isEmpty()) {
//            binding.etName.error = "Please enter First Name!"
            binding.etName.requestFocus()
            showErrorToast(this@SignupActivity, "Please enter First Name!")
            return false
        }
        if (lastName.isEmpty()) {
//            binding.etLastName.error = "Please enter Last Name!"
            binding.etLastName.requestFocus()
            showErrorToast(this@SignupActivity, "Please enter Last Name!")
            return false
        }
        if (email.isBlank()) {
//            binding.etEmail.error = "Please enter Email Address!"
            binding.etEmail.requestFocus()
            showErrorToast(this@SignupActivity, "Please enter Email Address!")
            return false
        }
        if (!email.isValidEmail()) {
//            binding.etEmail.error = "Please enter Valid Email Address!"
            binding.etEmail.requestFocus()
            showErrorToast(this@SignupActivity, "Please enter valid Email Address!")
            return false
        }
        if (userType != UserType.PROVIDER) {
            if (binding.etCode.text.toString().trim().isBlank()) {
//                binding.etCode.error = "Please enter Invitation Code!"
                binding.etCode.requestFocus()
                showErrorToast(this@SignupActivity, "Please enter Invitation Code!")
                return false
            }
        }

        if (password.isEmpty()) {
//            binding.etPassword.error = "Please enter Password"
            binding.etPassword.requestFocus()
            showErrorToast(this@SignupActivity, "Please enter Password!")
            return false
        }
        if (password.length < 6) {
//            binding.etPassword.error = "Minimum Password length should be 8!"
            binding.etPassword.requestFocus()
            showErrorToast(this@SignupActivity, "Minimum Password length should be 6!")
            return false
        }

        if (repeatPassword.isEmpty()) {
//            binding.etRepeatPassword.error = "Please enter Repeat Password"
            binding.etRepeatPassword.requestFocus()
            showErrorToast(this@SignupActivity, "Please Repeat your Password!")
            return false
        }

//        if(repeatPassword.length<6){
//            binding.etRepeatPassword.error = "Minimum Repeat Password length should be 8!"
//            binding.etRepeatPassword.requestFocus()
//            showErrorToast(this@SignupActivity, "Minimum Repeat Password length should be 6!")
//            return false
//        }

        if (repeatPassword != password) {
//            binding.etRepeatPassword.error = "Passwords Do Not Match"
            binding.etRepeatPassword.requestFocus()
            showErrorToast(this@SignupActivity, "Passwords Do Not Match!")
            return false
        }

        if (!binding.checkbox.isChecked) {
            showErrorToast(this@SignupActivity, "You need to agree with our Terms & conditions!")
            return false
        }
        return true
    }

    private fun focusChangeListener() {
        binding.etName.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusIn(binding.etName, R.drawable.ic_person_new, this@SignupActivity)
            } else {
                focusOut(binding.etName, R.drawable.ic_person_new, this@SignupActivity)
            }
        }

        binding.etEmail.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusIn(binding.etEmail, R.drawable.ic_email, this@SignupActivity)
            } else {
                focusOut(binding.etEmail, R.drawable.ic_email, this@SignupActivity)
            }
        }

        binding.etLastName.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusIn(binding.etLastName, R.drawable.ic_person_new, this@SignupActivity)
            } else {
                focusOut(binding.etLastName, R.drawable.ic_person_new, this@SignupActivity)
            }
        }

        binding.etPassword.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusInTil(binding.tilPassword, R.drawable.ic_lock_new, this@SignupActivity)
            } else {
                focusOutTil(binding.tilPassword, R.drawable.ic_lock, this@SignupActivity)
            }
        }

        binding.etRepeatPassword.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                focusInTil(binding.tilRepeatPassword, R.drawable.ic_lock_new, this@SignupActivity)
            } else {
                focusOutTil(binding.tilRepeatPassword, R.drawable.ic_lock, this@SignupActivity)
            }
        }
    }

    private fun showProgressBar() {
        binding.tvSignUp.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSignUp.viewVisible()
        binding.progressbar.viewGone()
    }
}