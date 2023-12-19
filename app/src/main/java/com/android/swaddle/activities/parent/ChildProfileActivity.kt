package com.android.swaddle.activities.parent

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.adapters.AllergiesAdapter
import com.android.swaddle.adapters.provider_adapter.DailyReportAdapter
import com.android.swaddle.adapters.provider_adapter.RelationAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityChildProfileBinding
import com.android.swaddle.fragments.dialog.UpdateEpiPenExpirationDatePopup
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChildProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityChildProfileBinding
    var item: ChildInfo? = null

    private var relationsList = ArrayList<User>()
    private var dailyReportAdapter: DailyReportAdapter? = null
    private var relationAdapter: RelationAdapter? = null
    private var reportDocumentsList = ArrayList<DailyReport>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent?.getSerializableExtra("item") as ChildInfo

        if (userManager.user?.type == "staff") {
            binding.imgEdit.viewGone()
        } else {
            binding.imgEdit.viewVisible()
        }

        initVar()
        listener()

        callAPItoGetChildDetails()
    }

    private fun setAllergiesAdapter(rvTags: RecyclerView, data: ArrayList<String>) {
        var layoutManager = FlexboxLayoutManager(this@ChildProfileActivity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        rvTags.layoutManager = layoutManager
        var adapter =
            AllergiesAdapter(
                this@ChildProfileActivity,
                data
            )
        rvTags.adapter = adapter

        if (data.size > 0) {
            binding.tvNoAllergies.viewGone()
            binding.rvTags.viewVisible()
        } else {
            binding.tvNoAllergies.viewVisible()
            binding.rvTags.viewGone()
        }
    }

    private fun callAPItoGetChildDetails() {
        showLoading()
        val call: Call<ChildDetailsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildDetails(
                "Bearer " + userManager.accessToken ?: "",
                (item?.id ?: 0).toString(),
            )
        call.enqueue(object : Callback<ChildDetailsResponse> {
            override fun onResponse(
                call: Call<ChildDetailsResponse>,
                response: Response<ChildDetailsResponse>
            ) {
                hideLoading()
                relationsList = response.body()?.childInfo?.relations ?: ArrayList()
                setRelationsRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildDetailsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileActivity, "Can't Connect to Server!")
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


    private fun listener() {
        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.layEdit.setOnClickListener {
            var intent = Intent(this@ChildProfileActivity, AddChildActivity::class.java)
            intent.putExtra("childitem", item)
            intent.putExtra(Constants.child_type, Constants.update_child)
            startActivity(intent)
        }

        binding.rlEpiPenExpired.setOnClickListener {
            if (DateConverter.getEpiPenExpiredOrNot(item?.epiPenExpirationDate ?: "")) {
                var dialog = UpdateEpiPenExpirationDatePopup.getInstance(item?.id)
                dialog.baseActivity = this@ChildProfileActivity
                dialog.clickListeners = object : UpdateEpiPenExpirationDatePopup.ClickListener {

                    override fun onUpdated(dialogFragment: DialogFragment?) {
                        dialog.dismiss()
                        finish()
                    }

                    override fun onCancelClicked() {
                    }

                }
                dialog.show(supportFragmentManager, "EpiPenAcknowledgementPopup")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initVar() {
        try {
            var allAlgs = ArrayList<String>()
            if (item?.foodAllergies != null || item?.environmentalAllergies != null) {

                if (item?.foodAllergies != null) {

                    var algs = JSONArray(item?.foodAllergies ?: "")
                    for (i in 0 until algs.length()) {
                        allAlgs.add(algs[i].toString())
                    }
                }
                if (item?.environmentalAllergies != null) {
                    var environmentalAllergies = JSONArray(item?.environmentalAllergies ?: "")
                    for (i in 0 until environmentalAllergies.length()) {
                        allAlgs.add(environmentalAllergies[i].toString())
                    }
                }

                setAllergiesAdapter(binding.rvTags, allAlgs)
            } else {
                setAllergiesAdapter(binding.rvTags, allAlgs)
                binding.rvTags.viewGone()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Glide.with(this@ChildProfileActivity).load(Constants.IMG_BASE_PATH + item?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)

        binding.tvName.text = item?.firstName + " " + item?.lastName
        binding.tvDob.text = DateConverter.DateFormatMonthDayYear(item?.dob ?: "")
        binding.tvEnrolmentDate.text = DateConverter.TimeStampMonthDayYear(item?.createdAt ?: "")
        binding.tvEpiPen.text = item?.doesChildRequireEpiPin ?: ""

        if (item?.epiPenExpirationDate == null) {
            binding.llEPIPenExpiration.viewGone()
            binding.viewEPIPen.viewGone()
        } else {
            binding.llEPIPenExpiration.viewVisible()
            binding.viewEPIPen.viewVisible()
        }

        if (item?.classroomDetails?.name == null) {
            binding.tvClassRoom.text = "Not Assigned"
        } else {
            binding.tvClassRoom.text = item?.classroomDetails?.name
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

        Glide.with(this@ChildProfileActivity).load(Constants.IMG_BASE_PATH + item?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)

        binding.tvName.text = item?.firstName + " " + item?.lastName

        val pan = Spanny(
            "Grade: ",
            ForegroundColorSpan(resources?.getColor(R.color.colorBlack) ?: 0)
        ).append(
            "N/A",
            ForegroundColorSpan(
                resources?.getColor(R.color.colorYellow) ?: 0
            )
        )
        binding.tvCustody.text = item?.custody ?: ""
        if (item?.custody.isNullOrEmpty()) {
            binding.tvCustody.viewGone()
        }
        binding.tvNeedsConsiderationsDetails.text = item?.specialNeedConsiderations ?: ""
        if (item?.specialNeedConsiderations.isNullOrEmpty()) {
            binding.tvNeedsConsiderations.viewGone()
        }
        binding.tvCustodyOperationDetail.text = item?.custodyDescription ?: ""
        if (item?.custodyDescription.isNullOrEmpty()) {
            binding.idcustodyHead.viewGone()
        }
        binding.tvIEPDetail.text = item?.comment ?: ""
        if (item?.comment.isNullOrEmpty()) {
            binding.tvIEPHeading.viewGone()
        }
        binding.tvIEPDiagnosedDetail.text = item?.iepDiagnosedBy ?: ""
        if (item?.iepDiagnosedBy.isNullOrEmpty()) {
            binding.tvIEPDiagnosedHeading.viewGone()
        }

        binding.tvEpiPenExpiration.text =
            DateConverter.convertEPPenDateMonthDayYear(item?.epiPenExpirationDate ?: "")

        if (DateConverter.getEpiPenExpiredOrNot(item?.epiPenExpirationDate ?: "")) {
            binding.ivExpired.viewVisible()
            binding.tvExpired.viewVisible()
        } else {
            binding.ivExpired.viewGone()
            binding.tvExpired.viewGone()
        }
        binding.tvEpiPen.text = item?.doesChildRequireEpiPin ?: ""
//            DateConverter.DateFormateThree(item?.epiPenReceivedInDayCare ?: "")
        try {
            binding.tvDob.text = DateConverter.DateFormatMonthDayYear(item?.dob ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvDob.text = item?.dob ?: ""
        }

        Log.i("jtag", item?.tuesday.toString())

        try {
            if (item?.monday == "[null,null]") {
                binding.tvMonOpening.text = "--:--"
                binding.tvMonClosing.text = "--:--"
            } else {
                var mon = JSONArray(item?.monday)
                binding.tvMonOpening.text = mon[0].toString()
                binding.tvMonClosing.text = mon[1].toString()
            }
            if (item?.tuesday == "[null,null]") {
                binding.tvTueOpening.text = "--:--"
                binding.tvTueClosing.text = "--:--"
            } else {
                var tue = JSONArray(item?.tuesday)
                binding.tvTueOpening.text = tue[0].toString()//.getString("1")
                binding.tvTueClosing.text = tue[1].toString()//.getString("2")
            }
            if (item?.wednesday == "[null,null]") {
                binding.tvWedOpening.text = "--:--"
                binding.tvWedClosing.text = "--:--"
            } else {
                var wed = JSONArray(item?.wednesday)
                binding.tvWedOpening.text = wed[0].toString()
                binding.tvWedClosing.text = wed[1].toString()
            }
            if (item?.thursday == "[null,null]") {
                binding.tvThuOpening.text = "--:--"
                binding.tvThuClosing.text = "--:--"
            } else {
                var thu = JSONArray(item?.thursday)
                binding.tvThuOpening.text = thu[0].toString()
                binding.tvThuClosing.text = thu[1].toString()
            }
            if (item?.friday == "[null,null]") {
                binding.tvFriOpening.text = "--:--"
                binding.tvFriClosing.text = "--:--"
            } else {
                var fri = JSONArray(item?.friday)
                binding.tvFriOpening.text = fri[0].toString()
                binding.tvFriClosing.text = fri[1].toString()
            }

        } catch (e: java.lang.Exception) {

        }
    }
}