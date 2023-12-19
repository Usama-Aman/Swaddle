package com.android.swaddle.activities.common

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityInvitePeopleBinding
import com.android.swaddle.enums.UserType
import com.android.swaddle.helper.*
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvitePeopleActivity : BaseActivity() {
    private lateinit var binding: ActivityInvitePeopleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitePeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        listener()

    }

    private fun listener() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnSignin.setOnClickListener {
            if (validate()) {
                callAPItoInvite()
            }
        }


    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@InvitePeopleActivity, "No internet connection!")
            return false
        }

        val email = binding.etEmail.text.toString()

        if (email.isBlank()) {
//            binding.etEmail.error = "Please enter email address"
            showErrorToast(this@InvitePeopleActivity, "Please enter email address!")
            return false
        }
        if (!email.isValidEmail()) {
//            binding.etEmail.error = "Please enter email address"
            showErrorToast(this@InvitePeopleActivity, "Please enter valid email address!")
            return false
        }

        return true
    }

    private fun initVar() {

    }


    private fun callAPItoInvite(
    ) {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .sendInvitation(
                "Bearer " + userManager.accessToken ?: "",
                Constants.parent,
                binding.etEmail.text.toString().trim()
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@InvitePeopleActivity, response.body()?.message ?: "")
                    hideProgressBar()
                    finish()
                } else {

                    hideProgressBar()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@InvitePeopleActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@InvitePeopleActivity, response.body()?.message ?: "")
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@InvitePeopleActivity, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun showProgressBar() {
        binding.tvSignin.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSignin.viewVisible()
        binding.progressbar.viewGone()
    }
}