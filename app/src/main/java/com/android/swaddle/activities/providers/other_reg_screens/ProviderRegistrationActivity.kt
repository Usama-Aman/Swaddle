package com.android.swaddle.activities.providers.other_reg_screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.activities.providers.LoginActivity
import com.android.swaddle.adapters.certification_adapters.CertificationsAdapter
import com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter.EthnicityList
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityProviderRegistrationBinding
import com.android.swaddle.fragments.bottomSheetFragment.EthnicityBottomSheet
import com.android.swaddle.fragments.fragment_certifications.AddCertificationsPopup
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.UpdateProfileResponse
import com.android.swaddle.models.User
import com.android.swaddle.models.delete_certifications.DeleteCertifications
import com.android.swaddle.models.get_certifications.CertificationsData
import com.android.swaddle.models.get_certifications.GetCertifications
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.isValidEmail
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class ProviderRegistrationActivity : BaseActivity(),
    CertificationsAdapter.CertificationsAdapterListener {
    var myCalendar = Calendar.getInstance()
    private lateinit var binding: ActivityProviderRegistrationBinding
    private lateinit var adapter: CertificationsAdapter
    private var certificationsList: ArrayList<CertificationsData> = ArrayList()
    var fileUri: Uri? = null
    var filePath = ""
    var gender = ""
    var user: User? = null
    var removalIndex = -1
    var ethnicityList = ArrayList<EthnicityList>()
    var selectedEthnicity: EthnicityList? = null
    var selectedEthnicityOtherText = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("item") as User?

        if (user != null) {
            binding.imgViewBack.viewVisible()
            binding.lnrSigninLabel.viewGone()
            binding.tvRegHeading.text = "Edit Profile"
            binding.tvNext.text = "Save"
        } else {
            binding.imgViewBack.viewGone()
            binding.lnrSigninLabel.viewGone()
        }
        setUpData()
        setCertificationRecView()
        initVar()
        listener()
        callToGetCertifications()

        if (user != null) {
            setupUpdatingData()
        }
    }

    private fun callToGetCertifications() {
        var token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiODE1ZGZmOGQ1MzRmNTY4MmE5NTExMjUxMmM3YmQ3M2UwYTdhZTdjODFjMWMwMDZiNmJlNjY1ZWMyODBmZDQ1ZmE5NDU1YzQ4MWQzYzJhMGEiLCJpYXQiOjE2MTM1NjY5NjAsIm5iZiI6MTYxMzU2Njk2MCwiZXhwIjoxNjQ1MTAyOTYwLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.EG3ZF44xDUPdtNfRK-BoVDbjEI-b2j6hLOH5WrQxoToX5FJH2nP5F1YLPcVtX1uHzu8BtfCXcSoTy-oUduGalS4_mO46DHBiUhKLeOMZwl3OMs9q-Uws-bVGgAnnMeGy7ENWjnl0B54rwOtdALvNEPBUJD7xQoX2OK3qPZKe4TTAedqfc2PssHwA1GMzddBZruy-stbVORxJ_h2gtiVBiZaDg7Fo4vt84AsGa9EJpYqw0dxR5qb1QR0ZR-BoXVDFDS9N2PPi9y5MIMYuLu8wA3PbjYIqOkMpw6a5FTuL7rI-eRsdgnbTA0gzs27uIAcN6SAHuxrVquQSJKsWux15WQI7PMvFMyCdjZQknYXYMSzg5xwkiE9MtVr_lt7FKxvfaKQQRzJ2deit39wXi_i9HxQuLR26Lwac09l6sOVaqfe2sh1zXLSeUExWOjrdgNf3lx_VGlvSTcSnCxWuvpX08egdVt-84uFJ6EiEHRJeKnF9IOBf7Z1ijF1tvkECNsETFwhSu6wf69t5vyHGK5eWRSXhdy7K2-ctd46KP6roELwLybowEXFqzXn3KBydKI3b_C7KW3g92T545SXYHAbi2tuav5keYL6Zej8-10Y-WZLzOk0yajlZJQZp0AFT6NME0OcuyONRaoumo3iWTznfDejhybecdwATZPAw2z5CfKI"

        val call: Call<GetCertifications> =
            RetrofitClass.getInstance().webRequestsInstance.getProviderCertifications(
                "Bearer " + userManager.accessToken ?: "", "${user?.id}"
            )

        call.enqueue(object : Callback<GetCertifications> {
            override fun onResponse(
                call: Call<GetCertifications>,
                response: Response<GetCertifications>
            ) {
                if (response.isSuccessful) {
                    val baseModel = response.body()!!
                    if (baseModel.status) {
                        certificationsList = baseModel.data as ArrayList<CertificationsData>

                        if (certificationsList.isNullOrEmpty()) {

                            binding.tvDropOffNoData.viewVisible()

                        } else if (certificationsList.isNotEmpty()) {

                            binding.tvDropOffNoData.viewGone()
                            adapter.updateCertificationsAdapter(certificationsList)

                        }
                    }
                } else {
                    val s = JSONObject(response.errorBody()?.string())
                    showErrorToast(this@ProviderRegistrationActivity, s.getString("message") ?: "")
                }
            }

            override fun onFailure(call: Call<GetCertifications>, t: Throwable) {
                showErrorToast(this@ProviderRegistrationActivity, "Can't Connect to Server.")
            }

        })
    }

    override fun onDeleteClick(position: Int, item: CertificationsData) {
        showLoading()
        var token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiODE1ZGZmOGQ1MzRmNTY4MmE5NTExMjUxMmM3YmQ3M2UwYTdhZTdjODFjMWMwMDZiNmJlNjY1ZWMyODBmZDQ1ZmE5NDU1YzQ4MWQzYzJhMGEiLCJpYXQiOjE2MTM1NjY5NjAsIm5iZiI6MTYxMzU2Njk2MCwiZXhwIjoxNjQ1MTAyOTYwLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.EG3ZF44xDUPdtNfRK-BoVDbjEI-b2j6hLOH5WrQxoToX5FJH2nP5F1YLPcVtX1uHzu8BtfCXcSoTy-oUduGalS4_mO46DHBiUhKLeOMZwl3OMs9q-Uws-bVGgAnnMeGy7ENWjnl0B54rwOtdALvNEPBUJD7xQoX2OK3qPZKe4TTAedqfc2PssHwA1GMzddBZruy-stbVORxJ_h2gtiVBiZaDg7Fo4vt84AsGa9EJpYqw0dxR5qb1QR0ZR-BoXVDFDS9N2PPi9y5MIMYuLu8wA3PbjYIqOkMpw6a5FTuL7rI-eRsdgnbTA0gzs27uIAcN6SAHuxrVquQSJKsWux15WQI7PMvFMyCdjZQknYXYMSzg5xwkiE9MtVr_lt7FKxvfaKQQRzJ2deit39wXi_i9HxQuLR26Lwac09l6sOVaqfe2sh1zXLSeUExWOjrdgNf3lx_VGlvSTcSnCxWuvpX08egdVt-84uFJ6EiEHRJeKnF9IOBf7Z1ijF1tvkECNsETFwhSu6wf69t5vyHGK5eWRSXhdy7K2-ctd46KP6roELwLybowEXFqzXn3KBydKI3b_C7KW3g92T545SXYHAbi2tuav5keYL6Zej8-10Y-WZLzOk0yajlZJQZp0AFT6NME0OcuyONRaoumo3iWTznfDejhybecdwATZPAw2z5CfKI"

        removalIndex = position
        val call: Call<DeleteCertifications> =
            RetrofitClass.getInstance().webRequestsInstance.deleteCertifications(
                "Bearer " + userManager.accessToken ?: "",
                removalIndex.toString()
            )

        call.enqueue(object : Callback<DeleteCertifications> {
            override fun onResponse(
                call: Call<DeleteCertifications>,
                response: Response<DeleteCertifications>
            ) {
                if (response.isSuccessful) {
                    val baseModel = response.body()!!
                    certificationsList.removeAt(removalIndex)
                    adapter.notifyItemRemoved(removalIndex)
                    adapter.updateCertificationsAdapter(certificationsList)
                } else {
                    val s = JSONObject(response.errorBody()?.string())
                    showErrorToast(this@ProviderRegistrationActivity, s.getString("message") ?: "")
                }
                hideLoading()
            }

            override fun onFailure(call: Call<DeleteCertifications>, t: Throwable) {
                showErrorToast(this@ProviderRegistrationActivity, "Can't Connect to Server.")
                hideLoading()
            }
        })
    }

    private fun setCertificationRecView() {
        adapter = CertificationsAdapter(
            this@ProviderRegistrationActivity,
            this@ProviderRegistrationActivity
        )
        binding.recCertifications.adapter = adapter
    }

    private fun setUpData() {
//        if (userManager.email != null) {
        binding.etFname.setText(userManager.user?.firstName ?: "")
        binding.etLastName.setText(userManager.user?.lastName ?: "")
        binding.etEmailAddress.setText(userManager.user?.email ?: "")
        binding.etEmailAddress.isEnabled = false
        binding.etEmailAddress.isClickable = false

//        }
    }

    private fun setupUpdatingData() {
        binding.tvEthnicityText.text = user?.ethnicity
        selectedEthnicity = ethnicityList.find { it.text == user?.ethnicity }

        if (user?.ethnicity_others != null) {
            binding.tvEthnicityText.text = user?.ethnicity
            selectedEthnicity = ethnicityList.find { it.text == "Other" }
            binding.tvEthnicityOther.viewVisible()
            binding.cvEthnicityOther.viewVisible()
            binding.tvEthnicityTextOther.text = user?.ethnicity_others ?: ""
            selectedEthnicityOtherText = user?.ethnicity_others ?: ""
        }

        binding.etFname.setText(user?.firstName ?: "")
        binding.etLastName.setText(user?.lastName ?: "")
        binding.etEmailAddress.setText(user?.email ?: "")
        binding.etEmailAddress.isEnabled = true
        binding.etEmailAddress.isClickable = true
        binding.etDesc.setText(user?.inspiration ?: "")
        binding.etFirstPos.setText(user?.position ?: "")
        binding.etExp.setText(user?.experience ?: "")
//        binding.etCert.setText(user?.certification ?: "")
        binding.etAbout.setText(user?.aboutMe ?: "")

        Glide.with(this@ProviderRegistrationActivity)
            .load(Constants.IMG_BASE_PATH + user?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder).into(binding.ivProfile)

        when (user?.gender ?: "") {
            "Female" -> {
                binding.rbFemal.isChecked = true
                gender = "Female"
            }
            "Unspecified" -> {
                binding.rbOther.isChecked = true
                gender = "Unspecified"
            }
            "Male" -> {
                binding.rbMale.isChecked = true
                gender = "Male"
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onResume() {
        super.onResume()
        callToGetCertifications()
    }

    override fun onBackPressed() {
        if (user != null) {
            super.onBackPressed()
            return
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.myLooper()!!).postDelayed(
            Runnable { doubleBackToExitPressedOnce = false },
            2000
        )

    }

    private fun listener() {
        binding.imgViewBack.setOnClickListener {
            finish()
        }

        binding.lnrAddCertification.setOnClickListener {
            var dialog = AddCertificationsPopup.getInstance()
            dialog.baseActivity = this@ProviderRegistrationActivity
            dialog.clickListeners = object : AddCertificationsPopup.ClickListener {
                override fun onCertificationAdded(dialogFragment: DialogFragment?) {
                    callToGetCertifications()
                }

                override fun onCancelClicked() {

                }
            }
            dialog.show(supportFragmentManager, "AddCertificationsPopup")
        }

//        binding.llCompletionDate.setOnClickListener {
//            var dpd = DatePickerDialog(
//                this@ProviderRegistrationActivity,  R.style.MySpinnerDatePickerStyle,
//                { _, year, month, dayOfMonth ->
//                    myCalendar.set(Calendar.YEAR, year)
//                    myCalendar.set(Calendar.MONTH, month)
//                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                    compDate = "$year-${month + 1}-$dayOfMonth"
//                    binding.tvCompletionDate.text = "$dayOfMonth / ${month + 1} / $year"
//                }, myCalendar
//                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                myCalendar.get(Calendar.DAY_OF_MONTH)
//            )
//
//            var cal = Calendar.getInstance()
////            cal.add(Calendar.YEAR,-12)
//            dpd.datePicker.maxDate = cal.timeInMillis
//
//            dpd.show()
//        }

        binding.lnrSigninLabel.setOnClickListener {
            val intent = Intent(this@ProviderRegistrationActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }

        binding.tvEthnicityText.setOnClickListener {
            ethnicityList.forEachIndexed { index, item ->
                if (item.text == binding.tvEthnicityText.text) {
                    ethnicityList[index].isChecked = true
                } else ethnicityList[index].isChecked =
                    item.text == "Other" && selectedEthnicityOtherText != ""
            }
            var sheet = EthnicityBottomSheet(
                this@ProviderRegistrationActivity,
                ethnicityList,
                selectedEthnicityOtherText
            )
            sheet.setListener(object : EthnicityBottomSheet.ClickListener {
                override fun onChooseClicked(selectedEthn: EthnicityList?, otherText: String) {
                    selectedEthnicity = selectedEthn
                    binding.tvEthnicityText.text = selectedEthn?.text
                    if (selectedEthn?.text == "Other") {
                        selectedEthnicityOtherText = otherText
                        binding.tvEthnicityOther.viewVisible()
                        binding.cvEthnicityOther.viewVisible()
                        binding.tvEthnicityTextOther.text = otherText
                    } else {
                        selectedEthnicityOtherText = ""
                        binding.tvEthnicityOther.viewGone()
                        binding.cvEthnicityOther.viewGone()
                        binding.tvEthnicityTextOther.text = ""
                    }
                }

                override fun onCancelClicked() {
                }
            })
            sheet.show(supportFragmentManager, "EthnicityBottomSheet")
        }

        binding.tvNext.setOnClickListener {
            if (validate()) {
                showProgressBar()
                callAPiToUpdate()
            }
        }

        binding.radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbFemal.id) {
                gender = "Female"
            }

            if (checkedId == binding.rbOther.id) {
                gender = "Unspecified"
            }

            if (checkedId == binding.rbMale.id) {
                gender = "Male"
            }
        }
    }

    private fun showProgressBar() {
        binding.tvNext.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvNext.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this)
    }

    private fun initVar() {
        ethnicityList.add(EthnicityList("Caucasian"))
        ethnicityList.add(EthnicityList("African American"))
        ethnicityList.add(EthnicityList("Native American"))
        ethnicityList.add(EthnicityList("Hispanic/Latino"))
        ethnicityList.add(EthnicityList("Asian"))
        ethnicityList.add(EthnicityList("South Asian"))
        ethnicityList.add(EthnicityList("Other"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                filePath = resultUri.path.toString()
                fileUri = resultUri
                //  fileUri = Uri.fromFile(File(image.path))
                Log.e("SELECTED_URI", fileUri.toString() + "")
                Glide.with(this).load(filePath)
                    .into(binding.ivProfile)
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //   val error = result.error
        }
    }

//    private fun callAPiToUpdate() {
//        val image = File(filePath)
//        val file = MultipartBody.Part.createFormData(
//            "profile_picture",
//            image.name,
//            RequestBody.create("image/*".toMediaTypeOrNull(), image)
//        )
//    }

    private fun callAPiToUpdate() {
//        val fileURI = Uri.parse(filePath)
//        val file = File(fileURI.path ?: "")
        var api = AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "update_user_profile")

        if (filePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path ?: "")
            api.addMultipartFile("profile_picture", file)
        }

        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
            .addMultipartParameter("_method", "PUT")
            .addMultipartParameter("first_name", binding.etFname.text.toString().trim())
            .addMultipartParameter("last_name", binding.etLastName.text.toString().trim())
            .addMultipartParameter("position", binding.etFirstPos.text.toString().trim())
            .addMultipartParameter("experience", binding.etExp.text.toString().trim())
//            .addMultipartParameter("certification", binding.etCert.text.toString().trim())
            .addMultipartParameter("inspiration", binding.etDesc.text.toString().trim())
            .addMultipartParameter("gender", gender)
            .addMultipartParameter("about_me", binding.etAbout.text.toString().trim())
            .addMultipartParameter("ethnicity", selectedEthnicity?.text)
            .addMultipartParameter("ethnicity_others", selectedEthnicityOtherText)
            .setTag("update_user_profile")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("Response", response.toString())

                    var data = gson.fromJson(response.toString(), UpdateProfileResponse::class.java)
                    if (data.status == true) {
                        var loginData = userManager
                        loginData.user = data.user
                        UserData.saveUserData(loginData, this@ProviderRegistrationActivity)
                        showSuccessToast(this@ProviderRegistrationActivity, data.message ?: "")

                        Handler(Looper.getMainLooper()).postDelayed({
                            runOnUiThread {
                                if (user != null) {
                                    finish()
                                } else {
                                    finishAffinity()
                                    startActivity<CenterRegistrationActivity>()
                                }
                                hideProgressBar()
                            }
                        }, 300)


                        // do anything with response

                    } else {
                        showErrorToast(this@ProviderRegistrationActivity, data.message ?: "")
                    }
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    var data =
                        gson.fromJson(error.errorBody.toString(), UpdateProfileResponse::class.java)
                    showErrorToast(this@ProviderRegistrationActivity, data.message ?: "")
                }
            })
    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@ProviderRegistrationActivity, "No internet connection!")
            return false
        }

        if (binding.etFname.text.toString().trim().isEmpty()) {
//            binding.etFname.error = "Please enter First Name!"
            binding.etFname.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter First Name!")
            return false
        }
        if (binding.etLastName.text.toString().trim().isEmpty()) {
//            binding.etLastName.error = "Please enter Last Name!"
            binding.etLastName.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter Last Name!")
            return false
        }
        if (binding.etEmailAddress.text.toString().trim().isEmpty()) {
//            binding.etEmailAddress.error = "Please enter your Email!"
            binding.etEmailAddress.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter your Email!")
            return false
        }

        if (selectedEthnicity == null) {
            showErrorToast(this@ProviderRegistrationActivity, "Please select Ethnicity!")
            return false
        }

        if (!binding.etEmailAddress.text.toString().trim().isValidEmail()) {
//            binding.etEmailAddress.error = "Please enter Valid Email!"
            binding.etEmailAddress.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter valid Email!")
            return false
        }

        if (gender.trim().isEmpty()) {
            binding.radioGroupGender.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please select your Gender!")
            return false
        }
//        if (binding.etDesc.text.toString().trim().isEmpty()) {
//            showErrorToast(this@ProviderRegistrationActivity, "Please enter your Description!")
//            return false
//        }
        if (binding.etFirstPos.text.toString().trim().isEmpty()) {
//            binding.etFirstPos.error = "Please enter your Position!"
            binding.etFirstPos.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter your Position!")
            return false
        }
        if (binding.etExp.text.toString().trim().isEmpty()) {
            binding.etExp.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter your Experience!")
            return false
        }
        if (certificationsList.size < 1) {
            showErrorToast(
                this@ProviderRegistrationActivity,
                "Please enter at least one Certification!"
            )
            return false
        }

        if (filePath.isEmpty() && user == null) {
            showErrorToast(this@ProviderRegistrationActivity, "Please select Display Picture!")
            return false
        }

        if (binding.etAbout.text.toString().trim().isEmpty()) {
            binding.etAbout.requestFocus()
            showErrorToast(this@ProviderRegistrationActivity, "Please enter About Provider!")
            return false
        }
        return true


    }
}