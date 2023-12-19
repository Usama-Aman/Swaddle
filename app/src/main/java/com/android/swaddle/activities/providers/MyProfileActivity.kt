package com.android.swaddle.activities.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.adapters.certification_adapters.CertificationsAdapterForProfileInfo
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityMyProfileBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.User
import com.android.swaddle.models.get_certifications.CertificationsData
import com.android.swaddle.models.get_certifications.GetCertifications
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private var adapter: CertificationsAdapterForProfileInfo? = null

    var fileUri: Uri? = null
    var filePath = ""
    var user: User? = null
    private var certificationsList: ArrayList<CertificationsData> = ArrayList()

    private var canNotEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = intent.getSerializableExtra("user") as User
        initVar()
        listener()
/*
        setUpdatingData()
        Log.e("UpdateData", "${setUpdatingData()}")
*/

        canNotEdit = intent?.getBooleanExtra("canNotEdit", false) ?: false
//        if (userManager.user?.id == user?.id || userManager.user?.type == Constants.provider) {
        if (!canNotEdit) {
            binding.imgEdit.viewVisible()
        } else {
            binding.imgEdit.viewGone()
        }
        setCertificationRecView()
        callToGetCertifications()
    }

    private fun setUpdatingData() {
        Glide.with(this@MyProfileActivity).load(Constants.IMG_BASE_PATH + user?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder).into(binding.ivProfile)

        if (userManager.user?.type != "parent" || userManager.user?.type != "authorized_adult") {
            binding.tvEmailText.text = user?.email ?: ""
        }
        binding.tvExperienceText.text = user?.experience ?: ""
        binding.tvAbout.text = user?.aboutMe ?: ""
        binding.tvInspired.text = user?.inspiration ?: ""
        binding.tvName.text = user?.firstName + " " + user?.lastName
        binding.tvRelation.text = user?.position ?: ""
    }

    private fun setCertificationRecView() {

    }

    override fun onResume() {
        super.onResume()
//        user = userManager.user
        initVar()
    }

    private fun callToGetCertifications() {

        val call: Call<GetCertifications> =
            RetrofitClass.getInstance().webRequestsInstance.getProviderCertifications(
                "Bearer " + userManager.accessToken ?: "", "${user?.id}"
            )
        call.enqueue(object : Callback<GetCertifications> {
            override fun onResponse(
                call: Call<GetCertifications>,
                response: Response<GetCertifications>
            ) {
                binding.swipeRefresh.isRefreshing = false
                if (response.body()?.status == true) {
                    certificationsList = response.body()?.data ?: ArrayList()
                } else {

                }
                setRelationsRecyclerView()

//                    val baseModel = response.body()!!
//                    if (baseModel.status) {
//                        certificationsList = baseModel.data as ArrayList<CertificationsData>
//
//                        if (certificationsList.isNullOrEmpty()) {
//
//                            binding.tvDropOffNoData.viewVisible()
//
//                        } else if (certificationsList.isNotEmpty()) {
//
//                            binding.tvDropOffNoData.viewGone()
//                            adapter.updateCertificationsAdapter(certificationsList)
//
//                        }
//                }
            }

            override fun onFailure(call: Call<GetCertifications>, t: Throwable) {
                showErrorToast(this@MyProfileActivity, "Can't Connect to Server.")
            }
        })
    }

    private fun setRelationsRecyclerView() {
        if (adapter == null) {
            adapter = CertificationsAdapterForProfileInfo(this@MyProfileActivity)
            adapter?.certificationsList = this.certificationsList
            binding.revView.adapter = adapter
        } else {
            adapter?.updateCertificationsAdapter(certificationsList)
        }

        if (certificationsList.size > 0) {
            binding.tvDropOffNoData.viewGone()
        } else {
            binding.tvDropOffNoData.viewVisible()
        }
    }

    private fun listener() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            callToGetCertifications()
        }



        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgEdit.setOnClickListener {
            when (user?.type ?: "") {
                "provider" -> {
                    startActivity<ProviderRegistrationActivity>("item" to user)
                }
                "parent" -> {
                    startActivity<ParentSignUpActivity>("item" to user)
                }
                "staff" -> {
                    startActivity<StaffRegistrationActivity>("item" to user)
                }
                "authorized_adult" -> {
                    startActivity<ParentSignUpActivity>("item" to user)
                }
            }
            finish()
        }
    }

    private fun initVar() {

//        when (user?.type) {
//            "parent" -> {
//                binding.tvRelation.text = "Parent"
//            }
//            "provider" -> {
//                binding.tvRelation.text = "Provider"
//            }
//            "staff" -> {
//                binding.tvRelation.text = "Staff"
//            }
//        }
        binding.tvRelation.text = user?.position ?: ""

        binding.tvMemberSince.text =
            "Active user since " + DateConverter.memberSince(user?.createdAt ?: "")
        binding.tvName.text = user?.firstName + " " + user?.lastName

        if (userManager.user?.type != "parent" || userManager.user?.type != "authorized_adult") {
            if (user?.email != null) {
                binding.tvEmailText.text = user?.email
            } else {
                binding.tvEmailText.text = "N/A"
            }
        }
        if (user?.experience != null) {
            binding.tvExperienceText.text = user?.experience
        } else {
            binding.tvExperienceText.text = "N/A"
        }

        if (user?.aboutMe != null) {
            binding.tvAbout.text = user?.aboutMe ?: ""
        } else {
            binding.tvAbout.text = "N/A"
        }

        if (user?.inspiration != null) {
            binding.tvInspired.text = user?.inspiration ?: ""
        } else {
            binding.tvInspired.text = "N/A"
        }
//        binding.tvEmail.text = user?.email ?: ""
        /*  if (user?.phoneNumber == null) {
              binding.view11.viewGone()
              binding.imgPhone.viewGone()
              binding.tvP.viewGone()
              binding.tvPhone.viewGone()
              binding.imgArrowPhone.viewGone()
          } else {
              binding.tvPhone.text = user?.phoneNumber ?: ""
          }

          binding.tvGender.text = user?.gender ?: ""*/

        Glide.with(this@MyProfileActivity)
            .load(Constants.IMG_BASE_PATH + user?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder).into(binding.ivProfile)

        if ((userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") && (user?.type == Constants.staff)) {
//            binding.tvEmailText.text = "********@***.com"

            binding.imgProfile.viewGone()
            binding.tvEmail.viewGone()
            binding.tvEmailText.viewGone()
            binding.imgArrowProfile.viewGone()
            binding.view.viewGone()
        }

    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this);

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
}