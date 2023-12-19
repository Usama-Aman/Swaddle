package com.android.swaddle.activities.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.android.swaddle.R
import com.android.swaddle.activities.common.ChangePasswordActivity
import com.android.swaddle.activities.common.ContactSupportActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivitySetingBinding
import com.android.swaddle.fragments.dialog.CenterAlertDialogFragment
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.models.NotificationSettingsResponse
import com.android.swaddle.models.get_user_detail_models.GetuserDetailBaseModel
import com.android.swaddle.models.update_account_status.UpdateAccountStatus
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.onesignal.OneSignalNotificationManager
import com.android.swaddle.payment_screens.BrowserActivity
import com.android.swaddle.payment_screens.PaymentMethodListActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySetingBinding
    private var alertMessage: String = ""
    var notification = "ON"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        listener()
        getUserDetail()
        getNotificationSettings()
    }

    private fun listener() {
        binding.rlChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        binding.layViewBack.setOnClickListener {
            finish()
        }
        binding.lnrNotification.setOnClickListener {

        }
        binding.switchNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notification = "ON"
            } else {
                notification = "OFF"
            }
            callToAddUpdateNotificationSettings()
        }

        binding.rlConnectToStripe.setOnClickListener {
            startActivity<BrowserActivity>()
        }
        binding.rlHelp.setOnClickListener {
            startActivity<ContactSupportActivity>()
        }

        binding.rlCenterAlerts.setOnClickListener {
            showCenterAlertFragment()
        }
        binding.rlPrivacy.setOnClickListener {
            try {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SERVER_ADDRESS + "term_of_use"))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(this@SettingActivity, "Failed to open URL!")
            }
//            showSuccessToast(this@SettingActivity, "Web page isPending!")
        }

        binding.rlFeaturesAndBenefits.setOnClickListener {
            try {
                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Constants.SERVER_ADDRESS + "features_benefits")
                    )
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(this@SettingActivity, "Failed to open URL!")
            }
//            showSuccessToast(this@SettingActivity, "Web page isPending!")
        }

        binding.rlTermsOfServices.setOnClickListener {
            try {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SERVER_ADDRESS + "term_of_use"))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast(this@SettingActivity, "Failed to open URL!")
            }
//            showSuccessToast(this@SettingActivity, "Web page isPending!")
        }
        binding.rlPayment.setOnClickListener {
            startActivity<PaymentMethodListActivity>()
        }

        if (userManager.user?.type == "provider") {
            binding.rlConnectToStripe.viewVisible()
            binding.rlPayment.viewGone()
            binding.rlCenterAlerts.viewVisible()

            if (userManager.user?.isBankAccountVerified != 1) {
                binding.tvStripeStatus.text = "Not Connected"
                binding.tvStripeStatus.textColor = resources.getColor(R.color.red)
            } else {
                binding.tvStripeStatus.textColor = resources.getColor(R.color.green)
                binding.tvStripeStatus.text = "Connected"
            }
        } else {
            binding.rlConnectToStripe.viewGone()
            binding.rlPayment.viewVisible()
        }
        if (userManager.user?.type == "parent") {
            binding.consDisableAccount.viewGone()
            binding.rlCenterAlerts.viewGone()
        }

        //jChanges
        binding.rlCenterAlerts.viewGone()

        binding.switchDisable.setOnCheckedChangeListener { group, isChecked ->
            binding.switchDisable.isChecked = true
            val alert =
                AlertView(
                    "Disable Account?",
                    "Are you sure you want to disable your account?",
                    AlertStyle.DIALOG
                )
            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                callToDisableAccount()


            })
            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
            })

            alert.show(this@SettingActivity)
            binding.switchDisable.isChecked = false


        }
    }

    private fun showCenterAlertFragment() {


        val fragment = CenterAlertDialogFragment().newInstance(alertMessage)
        fragment.show(supportFragmentManager, CenterAlertDialogFragment().tag)
        fragment.setListener(object : CenterAlertDialogFragment.ClickListener {
            override fun onItemClick(message: String) {
                callToAddUpdateMessage(message)
            }
        })
    }

    private fun callToDisableAccount() {


        showLoading()
        var call: Call<UpdateAccountStatus> =
            RetrofitClass.getInstance().webRequestsInstance.updateAccountStatus(
                "Bearer " + userManager.accessToken ?: "",
                true.toString()
            )
        call.enqueue(object : Callback<UpdateAccountStatus> {
            override fun onResponse(
                call: Call<UpdateAccountStatus>,
                response: Response<UpdateAccountStatus>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    OneSignalNotificationManager.removeUserTags()
                    UserData.setUserLogin(this@SettingActivity, false)
                    finishAffinity()
                    val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<UpdateAccountStatus>, t: Throwable) {
                showErrorToast(this@SettingActivity, "Can't Connect to Server.")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun initVar() {

    }


    override fun onResume() {
        super.onResume()
        if (userManager.user?.type == "provider") {
            binding.rlConnectToStripe.viewVisible()
            binding.rlPayment.viewGone()

            if (userManager.user?.isBankAccountVerified != 1) {
                binding.tvStripeStatus.text = "Not Connected"
                binding.tvStripeStatus.textColor = resources.getColor(R.color.red)
            } else {
                binding.tvStripeStatus.textColor = resources.getColor(R.color.green)
                binding.tvStripeStatus.text = "Connected"
            }
        } else {
            binding.rlConnectToStripe.viewGone()
            binding.rlPayment.viewVisible()

        }
    }


    private fun callToAddUpdateMessage(message: String) {
        showLoading("")
        val call: Call<LoginResponse> =
            RetrofitClass.getInstance().webRequestsInstance.addUpdateMessage(
                "Bearer ${userManager.accessToken}",
                message
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                hideLoading()
                if (response.isSuccessful) {
                    val baseModel = response.body()
                    if (baseModel?.status == true) {
                        showSuccessToast(this@SettingActivity, "${baseModel.message}")
                        alertMessage = message
                    } else {
                        showErrorToast(this@SettingActivity, "${baseModel?.message}")
                    }
                } else {
                    showErrorToast(this@SettingActivity, "${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showErrorToast(this@SettingActivity, "${t.message}")
                hideLoading()
            }
        })
    }


    private fun callToAddUpdateNotificationSettings() {
        binding.progressBar.viewVisible()
        binding.switchNotifications.viewGone()
        val call: Call<LoginResponse> =
            RetrofitClass.getInstance().webRequestsInstance.updateNotificationSetting(
                "Bearer ${userManager.accessToken}",
                notification
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                if (response.isSuccessful) {
                    val baseModel = response.body()
                    if (baseModel?.status == true) {
                        binding.progressBar.viewGone()
                        binding.switchNotifications.viewVisible()
                    } else {
                        showErrorToast(this@SettingActivity, "${baseModel?.message}")
                        binding.progressBar.viewGone()
                        binding.switchNotifications.viewVisible()
                    }
                } else {
                    binding.progressBar.viewGone()
                    binding.switchNotifications.viewVisible()
                    showErrorToast(this@SettingActivity, "${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                binding.progressBar.viewGone()
                binding.switchNotifications.viewVisible()
                showErrorToast(this@SettingActivity, "${t.message}")
                hideLoading()
            }
        })
    }


    private fun getNotificationSettings() {
        binding.progressBar.viewVisible()
        binding.switchNotifications.viewGone()
        val call: Call<NotificationSettingsResponse> =
            RetrofitClass.getInstance().webRequestsInstance.getNotificationSetting(
                "Bearer ${userManager.accessToken}", ""
            )
        call.enqueue(object : Callback<NotificationSettingsResponse> {
            override fun onResponse(
                call: Call<NotificationSettingsResponse>,
                response: Response<NotificationSettingsResponse>
            ) {
                if (response.isSuccessful) {
                    val baseModel = response.body()
                    if (baseModel?.status == true) {
                        binding.progressBar.viewGone()
                        binding.switchNotifications.viewVisible()
                        binding.switchNotifications.isChecked =
                            response.body()?.data?.allowNotification == "ON"
                    } else {
                        showErrorToast(this@SettingActivity, "${baseModel?.message}")

                    }
                } else {
                    showErrorToast(this@SettingActivity, "${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<NotificationSettingsResponse>, t: Throwable) {
                showErrorToast(this@SettingActivity, "${t.message}")
                hideLoading()
            }
        })
    }

    private fun getUserDetail() {
        val call: Call<GetuserDetailBaseModel> =
            RetrofitClass.getInstance().webRequestsInstance.getUserDetail(
                "Bearer ${userManager.accessToken}",
                userManager.user?.id.toString()
            )

        call.enqueue(object : Callback<GetuserDetailBaseModel> {
            override fun onResponse(
                call: Call<GetuserDetailBaseModel>,
                response: Response<GetuserDetailBaseModel>
            ) {
                if (response.isSuccessful) {
                    val baseModel = response.body()
                    if (baseModel?.status == true) {
                        try {
                            if (baseModel.data.message.toString().isNotEmpty()) {
                                alertMessage = baseModel.data.message.toString()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetuserDetailBaseModel>, t: Throwable) {

            }
        })
    }
}