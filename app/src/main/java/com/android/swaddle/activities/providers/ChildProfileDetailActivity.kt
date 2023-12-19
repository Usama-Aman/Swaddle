package com.android.swaddle.activities.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.parent.AddChildActivity
import com.android.swaddle.adapters.provider_adapter.RelationAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityChildProfileDetailBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildDetailsResponse
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.User
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildProfileDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityChildProfileDetailBinding
    private var relationAdapter: RelationAdapter? = null
    private var relationsList = ArrayList<User>()
    private var selectedChild: ChildInfo? = null

    var fileUri: Uri? = null
    var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedChild = intent.getSerializableExtra("item") as ChildInfo
        init()

        if (userManager.user?.id == selectedChild?.registrarId) {
            binding.imgEdit.viewVisible()
        } else {
            binding.imgEdit.viewGone()
            binding.imgEdit.isClickable = false
            binding.imgEdit.isFocusable = false
        }

        listener()

        callAPItoGetChildDetails()
    }

    private fun callAPItoGetChildDetails() {
        showLoading()
        val call: Call<ChildDetailsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildDetails(
                "Bearer " + userManager.accessToken ?: "",
                (selectedChild?.id ?: 0).toString(),
            )
        call.enqueue(object : Callback<ChildDetailsResponse> {
            override fun onResponse(
                call: Call<ChildDetailsResponse>,
                response: Response<ChildDetailsResponse>
            ) {
                hideLoading()
                relationsList = response.body()?.childInfo?.relations ?: ArrayList()
                initVar(response.body()?.childInfo)
                setRelationsRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildDetailsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileDetailActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRelationsRecyclerView() {
        if (relationAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recRelatoin.layoutManager = manager
            relationAdapter = RelationAdapter(this, relationsList)
            binding.recRelatoin.adapter = relationAdapter
        } else {
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }

        if (relationsList.size > 0) {
            binding.tvNoRelations.viewGone()
        } else {
            binding.tvNoRelations.viewVisible()
        }
    }

    private fun initVar(item: ChildInfo?) {
        if (item?.foodAllergies != null) {
            var allAlgs = StringBuilder()
            var algs = JSONArray(item?.foodAllergies ?: "")
            for (i in 0 until algs.length()) {
                if (i == algs.length() - 1)
                    allAlgs.append(algs[i])
                else
                    allAlgs.append(algs[i].toString() + ", ")
            }
            binding.tvAllergies.text = allAlgs.toString()
        } else {
            binding.tvAllergies.text = "No Allergies"
        }
        Glide.with(this@ChildProfileDetailActivity)
            .load(Constants.IMG_BASE_PATH + item?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)


        binding.tvName.text = item?.firstName + " " + item?.lastName
        binding.tvDob.text = DateConverter.ConvertDateFormat4(item?.dob ?: "")
        binding.tvEnrolmentDate.text = DateConverter.TimeStampNew(item?.createdAt ?: "")
        binding.tvEpiReceivedDate.text = DateConverter.TimeStampNew(item?.createdAt ?: "")
        if (item?.epiPenExpirationDate == null) {
            binding.llEPIPenExpiration.viewGone()
            binding.viewEPIPen.viewGone()
        } else {
            binding.llEPIPenExpiration.viewVisible()
            binding.viewEPIPen.viewVisible()
        }
        binding.tvEpiReceivedDate.text = item?.epiPenExpirationDate
        if (item?.classroomDetails?.name == null) {
            binding.tvClassRoom.text = "Not Assigned"
        } else {
            binding.tvClassRoom.text = item.classroomDetails?.name
        }

        relationsList.forEachIndexed { index, relation ->
            if (relation.type == "parent") {
                binding.tvCustodyDescription.text = relation.custodyDescription ?: ""
                if (relation.custodyDescription == null) {
                    binding.tvCustodyDesc.viewGone()
                }
            }
        }
//        binding.tvChildAge.text = if ((DateConverter.getAge(
//                DateConverter.getYear(item.dob ?: "").toInt(),
//                DateConverter.getMonth(item.dob ?: "").toInt(),
//                DateConverter.getDayNew(item.dob ?: "").toInt()
//            ) ?: "0").toInt() < 1
//        ) {
//            "Less than ONE year."
//        } else {
//            (DateConverter.getAge(
//                DateConverter.getYear(item.dob ?: "").toInt(),
//                DateConverter.getMonth(item.dob ?: "").toInt(),
//                DateConverter.getDayNew(item.dob ?: "").toInt()
//            ) ?: "0")
//        }

        Glide.with(this@ChildProfileDetailActivity)
            .load(Constants.IMG_BASE_PATH + item?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)

        binding.tvName.text = item?.firstName + " " + item?.lastName
        binding.tvMemberSince.text =
            "Active user since " + DateConverter.memberSince(item?.createdAt ?: "")
        binding.tvCustody.text = item?.custody
        if (item?.custody.isNullOrEmpty()) {
            binding.tvCustody.viewGone()
        }

        try {
            binding.tvEpiPenExpiration.text =
                DateConverter.DateFormateThree(item?.epiPenExpirationDate ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            binding.tvEpiReceivedDate.text =
                DateConverter.DateFormateThree(item?.epiPenReceivedInDayCare ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            binding.tvDob.text = DateConverter.ConvertDateFormat4(item?.dob ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvDob.text = item?.dob ?: ""
        }
    }

    private fun setRecyclerView() {
        if (relationAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recRelatoin.layoutManager = manager
            relationAdapter = RelationAdapter(this, relationsList)
            binding.recRelatoin.adapter = relationAdapter
        } else {
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }
    }

    private fun listener() {
        binding.layViewBack.setOnClickListener {
            finish()
        }
        binding.imgEdit.setOnClickListener {
            startActivity<AddChildActivity>("item" to selectedChild)
        }
        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }
    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this)
    }

    private fun init() {

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
