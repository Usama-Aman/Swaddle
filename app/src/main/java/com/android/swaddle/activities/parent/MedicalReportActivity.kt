package com.android.swaddle.activities.parent


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.activities.providers.messages.NewMessageActivity
import com.android.swaddle.adapters.AllergiesAdapter
import com.android.swaddle.adapters.provider_adapter.MedicalReportAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityMedicalReportBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.codekidlabs.storagechooser.StorageChooser
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import android.content.ActivityNotFoundException
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider


class MedicalReportActivity : BaseActivity() {
    private lateinit var binding: ActivityMedicalReportBinding

    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1

    var downloadedPosition = 0
    var deletedPosition = -1
    var selectedTabPosition = 0

    private val tvMonthList = ArrayList<TextView>()
    private val viewMonthList = ArrayList<View>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var childesList = ArrayList<ChildInfo>()
    val reportDocumentsList = ArrayList<DailyReport>()
    val filteredDocumentsList = ArrayList<DailyReport>()

    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null

    var documentPath = ArrayList<String>()

    private var childesAdapter: SelectChildSpinnerAdapter? = null
    private var dailyReportAdapter: MedicalReportAdapter? = null

    private var builder = StorageChooser.Builder()
    private var chooser: StorageChooser? = null

    var formats = ArrayList<String>()
    var selectedPos = 0
    var classroom_id: Int = -1
    var child_id: Int = -1
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        mContext = this@MedicalReportActivity
        selectedPos = intent.getIntExtra("position", 0)
        initi()
        if (userManager.user?.type == Constants.provider || userManager.user?.type == Constants.staff) {
            callAPItoGetClassRooms()
        } else {
            callAPItoGetChildes()
        }
        listeners()
    }
    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
        child_id = intent.getIntExtra("child_id",-1)
    }
    private fun setAllergiesAdapter(rvTags: RecyclerView, data: ArrayList<String>) {
        var layoutManager = FlexboxLayoutManager(this@MedicalReportActivity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        rvTags.layoutManager = layoutManager
        var adapter =
            AllergiesAdapter(
                this@MedicalReportActivity,
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

    @SuppressLint("NewApi", "SetTextI18n")
    fun setUpSelectedChildInfo(item: ChildInfo) {
        try {
            var allAlgs = ArrayList<String>()
            if (item.foodAllergies != null || item.environmentalAllergies != null) {

                if (item.foodAllergies != null) {

                    var algs = JSONArray(item?.foodAllergies ?: "")
                    for (i in 0 until algs.length()) {
                        allAlgs.add(algs[i].toString())
                    }
                }
                if (item.environmentalAllergies != null) {
                    var environmentalAllergies = JSONArray(item?.environmentalAllergies ?: "")
                    for (i in 0 until environmentalAllergies.length()) {
                        allAlgs.add(environmentalAllergies[i].toString())
                    }
                }
                setAllergiesAdapter(binding.rvTags, allAlgs)
            } else {
                binding.tvNoAllergies.viewVisible()
                setAllergiesAdapter(binding.rvTags, allAlgs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            Glide.with(applicationContext).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivChildImg)
        }catch (ex: Exception) {

        }

        binding.tvChildName.text = item.firstName + " " + item.lastName
        binding.tvChildDob.text = DateConverter.jDateFormatFour(item.dob ?: "")
        binding.tvChildEnrolmentDate.text = DateConverter.jTimeStampNew(item.createdAt ?: "")
        binding.tvChildEpiPen.text = item.doesChildRequireEpiPin
        if (item.epiPenExpirationDate == null) {
            binding.llEPIPenExpiration.viewGone()
            binding.viewEPIPen.viewGone()
        } else {
            binding.llEPIPenExpiration.viewVisible()
            binding.viewEPIPen.viewVisible()
        }
        binding.tvChildEpiPenExpiryDate.text =
            DateConverter.jconvertEPPenDate(item.epiPenExpirationDate ?: "")

        Log.e("ChildInfo", "${selectedChild?.classroomDetails?.name}")
        if (selectedChild?.classroomDetails?.name == null) {
            binding.tvChildClassRoom.text = "No Classroom Associated."
        } else {
            binding.tvChildClassRoom.text = selectedChild?.classroomDetails?.name
        }
        /* binding.tvChildAge.text = if ((DateConverter.getAge(
                 DateConverter.getYear(item.dob ?: "").toInt(),
                 DateConverter.getMonth(item.dob ?: "").toInt(),
                 DateConverter.getDayNew(item.dob ?: "").toInt()
             ) ?: "0").toInt() < 1
         ) {
             "Less than ONE year."
         } else {
             (DateConverter.getAge(
                 DateConverter.getYear(item.dob ?: "").toInt(),
                 DateConverter.getMonth(item.dob ?: "").toInt(),
                 DateConverter.getDayNew(item.dob ?: "").toInt()
             ) ?: "0")
         }*/

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val month2 = if ((month + 1) < 10) {
            "0${month + 1}"
        } else {
            "${month + 1}"
        }
        val day2 = if ((day) < 10) {
            "0${day}"
        } else {
            "${day}"
        }
        val year2 = if ((year) < 10) {
            "${year}"
        } else {
            "${year}"
        }


        val myDate = "${year}-${month2}-${day2}"
        val monthsBetween: Long = ChronoUnit.MONTHS.between(
            LocalDate.parse(item.dob).withDayOfMonth(1),
            LocalDate.parse(myDate).withDayOfMonth(1)
        )
        val yearsBetween: Long = ChronoUnit.YEARS.between(
            LocalDate.parse(item.dob).withDayOfMonth(1),
            LocalDate.now(ZoneId.systemDefault())
//            LocalDate.parse(year2).withDayOfMonth(1)
        )

        when {
            yearsBetween < 1 -> {
                binding.tvChildAge.setText("${monthsBetween % 12}m")
            }
            monthsBetween % 12 < 1 -> {
                binding.tvChildAge.setText("${yearsBetween}y")
            }
            else -> {
                binding.tvChildAge.setText("${yearsBetween}y, ${monthsBetween % 12}m")
            }
        }

        if (monthsBetween <= 4) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_months" })
            selectedTabPosition = 0
            changeMonthTabsColor(binding.tv2Month, binding.view2Month)
        } else if (monthsBetween <= 6) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_months" })
            selectedTabPosition = 1
            changeMonthTabsColor(binding.tv4Month, binding.view4Month)
        } else if (monthsBetween <= 9) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "six_months" })
            selectedTabPosition = 2
            changeMonthTabsColor(binding.tv6Month, binding.view6Month)
            dailyReportAdapter?.notifyDataSetChanged()
        } else if (monthsBetween <= 12) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "nine_months" })
            selectedTabPosition = 3
            changeMonthTabsColor(binding.tv9Month, binding.view9Month)
        } else if (monthsBetween <= 15) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "twelve_months" })
            selectedTabPosition = 4
            changeMonthTabsColor(binding.tv12Month, binding.view12Month)
        } else if (monthsBetween <= 18) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "fifteen_months" })
            selectedTabPosition = 5
            changeMonthTabsColor(binding.tv15Month, binding.view15Month)
        } else if (monthsBetween <= 24) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "eighteen_months" })
            selectedTabPosition = 6
            changeMonthTabsColor(binding.tv18Month, binding.view18Month)
        } else if (monthsBetween <= 36) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_years" })
            selectedTabPosition = 7
            changeMonthTabsColor(binding.tv2Years, binding.view2Years)
        } else if (monthsBetween <= 48) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "three_years" })
            selectedTabPosition = 8
            changeMonthTabsColor(binding.tv3Years, binding.view3Years)
        } else if (monthsBetween <= 60) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_years" })
            selectedTabPosition = 9
            changeMonthTabsColor(binding.tv4Years, binding.view4Years)
        } else if (monthsBetween > 60) {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "five_years" })
            selectedTabPosition = 10
            changeMonthTabsColor(binding.tv5Years, binding.view5Years)
        }

        dailyReportAdapter?.notifyDataSetChanged()

//        binding.tvChildAge.text = item.custody
    }

//    private fun callAPItoGetChildDetails() {
//        showLoading()
//        val call: Call<ChildDetailsResponse> = RetrofitClass.getInstance().webRequestsInstance
//            .getChildDetails(
//                "Bearer " + userManager.accessToken ?: "",
//                (selectedChild?.id ?: 0).toString(),
//            )
//        call.enqueue(object : Callback<ChildDetailsResponse> {
//            override fun onResponse(
//                call: Call<ChildDetailsResponse>,
//                response: Response<ChildDetailsResponse>
//            ) {
//                hideLoading()
//                relationsList = response.body()?.childInfo?.relations ?: ArrayList()
//                setReportRecyclerView(response.body()?.childInfo?.relations)
//
//            }
//
//            override fun onFailure(
//                call: Call<ChildDetailsResponse>,
//                t: Throwable
//            ) {
//                showErrorToast(this@MedicalReportActivity, "Can't Connect to Server!")
//                hideLoading()
//                t.printStackTrace()
//            }
//        })
//    }

    private fun callAPItoGetClassRooms(
    ) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getClassRooms("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()

            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@MedicalReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetMedicalInformation(
    ) {
        showLoading()
        val call: Call<MedicalReportsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getMedicalInformation(
                "Bearer " + userManager.accessToken ?: "",
                (selectedChild?.id ?: 0).toString()
            )
        call.enqueue(object : Callback<MedicalReportsResponse> {
            override fun onResponse(
                call: Call<MedicalReportsResponse>,
                response: Response<MedicalReportsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                reportDocumentsList.clear()
                reportDocumentsList.addAll(response.body()?.data!!)
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(response.body()?.data!!)

                setReportRecyclerView()
            }

            override fun onFailure(
                call: Call<MedicalReportsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@MedicalReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@MedicalReportActivity, classRoomsList)
            binding.spClassRoom.adapter = classRoomsAdapter
        } else {
            classRoomsAdapter?.setItems(classRoomsList)
            classRoomsAdapter?.notifyDataSetChanged()
        }
        binding.spClassRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedClassRoom = classRoomsList[position]
                callAPItoGetChildesOfClassRoom()
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                filteredDocumentsList.clear()
                reportDocumentsList.clear()
                //   showSuccessToast(this@MedicalReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
            binding.tvNoChildData.viewGone()
            binding.llChildDetails.viewVisible()
            //callAPItoGetChildesOfClassRoom()
            binding.tvNoReportsFound.viewGone()
            if(selectedClassRoom?.id == -1)
                selectedClassRoom = classRoomsList.first()
            if(classroom_id != -1) {
                classRoomsList.forEachIndexed { index, item ->
                    if (item.id == classroom_id) {
                        binding.spClassRoom.setSelection(index)
                        selectedClassRoom = item
                    }
                }
                classroom_id = -1
            }
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvNoChildData.viewVisible()
            binding.llChildDetails.viewGone()
            binding.tvNoReportsFound.viewVisible()
        }

        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun callAPItoGetChildesOfClassRoom() {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesListOfRoom(
                "Bearer " + userManager.accessToken ?: "",
                (selectedClassRoom?.id ?: 0).toString(), "0"
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@MedicalReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetChildes() {
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {

                hideLoading()
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@MedicalReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        childesAdapter = null
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@MedicalReportActivity, childesList)
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(childesList)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (childesList.size > 0) {
                    selectedChild = childesList[position]
                    setUpSelectedChildInfo(selectedChild!!)
                    filteredDocumentsList.clear()
                    reportDocumentsList.clear()
                    callAPItoGetMedicalInformation()

                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            binding.tvNoChildData.viewGone()
            binding.llChildDetails.viewVisible()
            binding.tvNoReportsFound.viewGone()
            if(selectedChild?.id == -1)
                selectedChild = childesList.first()
            if(child_id != -1) {
                childesList.forEachIndexed { index, item ->
                    if (item.id == child_id) {
                        binding.spChild.setSelection(index)
                        selectedChild = item
                    }
                }
                child_id = -1
            }
        } else {
            binding.tvNoChildData.viewVisible()
            binding.llChildDetails.viewGone()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            binding.tvNoReportsFound.viewVisible()
        }
//        if (childesList.size > 0) {
//            callAPItoGetMedicalInformation()
//            setUpSelectedChildInfo(selectedChild!!)
//        }
    }

//    private fun setUpSelectedChildInfo(item: ChildInfo) {
//
//        Glide.with(this@MedicalReportActivity).load(Constants.IMG_BASE_PATH + item.profilePicture)
//            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivChildImg)
//
//        binding.tvChildName.text = item.firstName + " " + item.lastName
//        binding.tvChildDob.text = DateConverter.DateFormatFour(item.dob ?: "")
//        binding.tvChildEnrolmentDate.text = DateConverter.TimeStampNew(item.createdAt ?: "")
//        binding.tvChildEpiPen.text = item.doesChildRequireEpiPin
//        if (item.epiPenExpirationDate == null) {
//            binding.llEPIPenExpiration.viewGone()
//            binding.viewEPIPen.viewGone()
//        } else {
//            binding.llEPIPenExpiration.viewVisible()
//            binding.viewEPIPen.viewVisible()
//        }
//        binding.tvChildEpiPenExpiryDate.text = item.epiPenExpirationDate
//        if (item.classroomDetails?.name == null) {
//            binding.tvChildClassRoom.text = "No Classroom Associated yet!"
//            binding.tvClassRoom.text = "No Classroom Associated yet!"
//        } else {
//            binding.tvChildClassRoom.text = item.classroomDetails?.name
//            binding.tvClassRoom.text = item.classroomDetails?.name
//        }
//
//
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
//    }


    private fun listeners() {
        binding.llChildInfo.setOnClickListener {
            startActivity<ChildProfileActivity>("item" to selectedChild)
        }

        binding.tv2Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_months" })
            selectedTabPosition = 0

            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()

            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv2Month, binding.view2Month)

        }

        binding.tv4Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_months" })
            selectedTabPosition = 1
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv4Month, binding.view4Month)
        }

        binding.tv6Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "six_months" })
            selectedTabPosition = 2
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv6Month, binding.view6Month)
        }

        binding.tv9Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "nine_months" })
            selectedTabPosition = 3
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv9Month, binding.view9Month)
        }

        binding.tv12Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "twelve_months" })
            selectedTabPosition = 4
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv12Month, binding.view12Month)
        }

        binding.tv15Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "fifteen_months" })
            selectedTabPosition = 5
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv15Month, binding.view15Month)
        }

        binding.tv18Month.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "eighteen_months" })
            selectedTabPosition = 6
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv18Month, binding.view18Month)
        }

        binding.tv2Years.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_years" })
            selectedTabPosition = 7
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv2Years, binding.view2Years)
        }

        binding.tv3Years.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "three_years" })
            selectedTabPosition = 8
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv3Years, binding.view3Years)
        }

        binding.tv4Years.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_years" })
            selectedTabPosition = 9
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv4Years, binding.view4Years)
        }

        binding.tv5Years.setOnClickListener {
            filteredDocumentsList.clear()
            filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "five_years" })
            selectedTabPosition = 10
            if (filteredDocumentsList.isEmpty())
                binding.tvNoReportsFound.viewVisible()
            else
                binding.tvNoReportsFound.viewGone()
            dailyReportAdapter?.notifyDataSetChanged()
            changeMonthTabsColor(binding.tv5Years, binding.view5Years)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvUpload.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
//                        chooser?.show()

                        chooserDialog()


                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun chooserDialog() {
        val alert =
            AlertView(
                "Please choose an option",
                "",
                AlertStyle.DIALOG
            )
        alert.addAction(AlertAction("File", AlertActionStyle.DEFAULT) {
            chooser?.show()
        })
        alert.addAction(AlertAction("Image", AlertActionStyle.DEFAULT) {
            val options: Options = Options.init()
                .setRequestCode(NewMessageActivity.ImageSelectionRequestCode) //Request code for activity results
                .setCount(1) //Number of images to restrict selection count
                .setFrontfacing(false) //Front Facing camera on start
                .setExcludeVideos(true) //Option to exclude videos
                .setVideoDurationLimitinSeconds(30) //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientation
                .setPath("/.collegiate/images") //Custom Path For media Storage

            Pix.start(this@MedicalReportActivity, options)
        })
        alert.show(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == NewMessageActivity.ImageSelectionRequestCode) {
            val mPath = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
            documentPath = ArrayList()
            if (mPath != null) {
                documentPath = mPath
            }
            callAPItoUploadDocuments(selectedTabPosition)
        }
    }


    private fun initi() {
        //filling text view list(month)
        tvMonthList.add(binding.tv2Month)
        tvMonthList.add(binding.tv4Month)
        tvMonthList.add(binding.tv6Month)
        tvMonthList.add(binding.tv9Month)
        tvMonthList.add(binding.tv12Month)
        tvMonthList.add(binding.tv15Month)
        tvMonthList.add(binding.tv18Month)
        tvMonthList.add(binding.tv2Years)
        tvMonthList.add(binding.tv3Years)
        tvMonthList.add(binding.tv4Years)
        tvMonthList.add(binding.tv5Years)

        formats.add("txt")
        formats.add("jpg")
        formats.add("jpeg")
        formats.add("xlx")
        formats.add("xlxs")
        formats.add("doc")
        formats.add("docx")
        formats.add("png")
        formats.add("pdf")

        builder.customFilter(formats);

        /*filling view list*/
        viewMonthList.add(binding.view2Month)
        viewMonthList.add(binding.view4Month)
        viewMonthList.add(binding.view6Month)
        viewMonthList.add(binding.view9Month)
        viewMonthList.add(binding.view12Month)
        viewMonthList.add(binding.view15Month)
        viewMonthList.add(binding.view18Month)
        viewMonthList.add(binding.view2Years)
        viewMonthList.add(binding.view3Years)
        viewMonthList.add(binding.view4Years)
        viewMonthList.add(binding.view5Years)

        if (userManager.user?.type == Constants.parent) {
            binding.tvClassRoom.viewVisible()
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
//            binding.btnUpload.viewVisible()
        } else {
            binding.tvClassRoom.viewGone()
            binding.tvClass.viewVisible()
            binding.cvSpinnerClass.viewVisible()
//            binding.btnUpload.viewInVisible()
        }


        builder.withActivity(this@MedicalReportActivity)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .customFilter(formats)
            .build()

        chooser = builder.build()
        chooser?.setOnMultipleSelectListener { paths ->
            Log.e("SELECTED_PATH", paths.size.toString())
            documentPath = ArrayList()
            documentPath = paths
            callAPItoUploadDocuments(selectedTabPosition)
        }
        chooser?.setOnSelectListener { path ->
            Log.e("SELECTED_PATH", path)
            documentPath = ArrayList()
            documentPath.add(path)
            callAPItoUploadDocuments(selectedTabPosition)
        }
    }

    private fun changeMonthTabsColor(tv: TextView, v: View) {
        for (txtView in tvMonthList) {
            txtView.typeface = getNormalFont()
            txtView.setTextColor(ContextCompat.getColor(this, R.color.color_grey_300))
        }

        for (vv in viewMonthList) {
            vv.setBackgroundResource(R.color.colorSilver)
        }

//        tv.typeface = getBoldFont()
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
        v.setBackgroundResource(R.color.colorYellow)

        binding.medMainScrollView.scrollTo(
            0,
            binding.medMainScrollView.height - (binding.recDailyReport.height / 2)
        )
    }

    private fun getNormalFont(): Typeface? {
        return ResourcesCompat.getFont(this, R.font.font_regular)
    }

    private fun getBoldFont(): Typeface? {
        return ResourcesCompat.getFont(this, R.font.font_semi_bold)
    }

    private fun setReportRecyclerView() {
        if (dailyReportAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recDailyReport.layoutManager = manager

            dailyReportAdapter =
                MedicalReportAdapter(
                    this@MedicalReportActivity,
                    filteredDocumentsList,
                    object :
                        MedicalReportAdapter.ItemClickListener {
                        override fun onItemClick(position: Int) {

                        }

                        override fun onDownloadClick(id: Int) {

                            outer@ for (i in reportDocumentsList.indices)
                                if (reportDocumentsList[i].id == id) {
                                    downloadedPosition = i
                                    break@outer
                                }


                            permissionAccess(downloadedPosition)
                        }

                        override fun onOpenDraftClick(position: Int) {
                            permissionAccess(position)
                            downloadedPosition = position
                        }

                        override fun onViewDocClick(id: Int) {
                            outer@ for (i in reportDocumentsList.indices)
                                if (reportDocumentsList[i].id == id) {
                                    downloadedPosition = i
                                    break@outer
                                }
//<<<<<<< HEAD
//
//                            openFileInBrowser(
//                                downloadedPosition,
//                                Constants.IMG_BASE_PATH + reportDocumentsList[downloadedPosition].filePath
//                                    ?: ""
//                            )
//=======
                            openFileInBrowser(
                                downloadedPosition,
                                Constants.IMG_BASE_PATH + reportDocumentsList[downloadedPosition].filePath
                                    ?: ""
                            )
//>>>>>>> origin/hakeemDev
//                            permissionAccess(downloadedPosition)
                        }

                        override fun onDeleteClick(position: Int) {
                            deletedPosition = position

                            val alert =
                                AlertView(
                                    "Delete?",
                                    "Are you sure you want to Delete Medical Report?",
                                    AlertStyle.DIALOG
                                )
                            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                                //=call api to remove from Classroom...
                                callAPItoDeleteReport(
                                    filteredDocumentsList[position].filePath ?: "",
                                    position, filteredDocumentsList[position].id ?: 0
                                )
                            })
                            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                            })
                            alert.show(this@MedicalReportActivity)

                        }
                    })
            binding.recDailyReport.adapter = dailyReportAdapter
        } else {
            dailyReportAdapter?.setItems(reportDocumentsList)
            dailyReportAdapter?.notifyDataSetChanged()
        }

        if (reportDocumentsList.size > 0) {
            binding.tvNoReportsFound.viewGone()
        } else {
            binding.tvNoReportsFound.viewVisible()
        }

        when (selectedTabPosition) {
            0 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_months" })
                selectedTabPosition = 0
                changeMonthTabsColor(binding.tv2Month, binding.view2Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            1 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_months" })
                selectedTabPosition = 1
                changeMonthTabsColor(binding.tv4Month, binding.view4Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            2 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "six_months" })
                selectedTabPosition = 2
                changeMonthTabsColor(binding.tv6Month, binding.view6Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            3 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "nine_months" })
                selectedTabPosition = 3
                changeMonthTabsColor(binding.tv9Month, binding.view9Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            4 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "twelve_months" })
                selectedTabPosition = 4
                changeMonthTabsColor(binding.tv12Month, binding.view12Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            5 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "fifteen_months" })
                selectedTabPosition = 5
                changeMonthTabsColor(binding.tv15Month, binding.view15Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            6 -> {
                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "eighteen_months" })
                selectedTabPosition = 6
                changeMonthTabsColor(binding.tv18Month, binding.view18Month)
                dailyReportAdapter?.notifyDataSetChanged()
            }
            7 -> {

                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_years" })
                selectedTabPosition = 7
                changeMonthTabsColor(binding.tv2Years, binding.view2Years)
                dailyReportAdapter?.notifyDataSetChanged()

            }
            8 -> {

                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "three_years" })
                selectedTabPosition = 8
                changeMonthTabsColor(binding.tv3Years, binding.view3Years)
                dailyReportAdapter?.notifyDataSetChanged()

            }
            9 -> {

                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "four_years" })
                selectedTabPosition = 9
                changeMonthTabsColor(binding.tv4Years, binding.view4Years)
                dailyReportAdapter?.notifyDataSetChanged()


            }
            10 -> {


                filteredDocumentsList.clear()
                filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "five_years" })
                selectedTabPosition = 10
                changeMonthTabsColor(binding.tv5Years, binding.view5Years)
                dailyReportAdapter?.notifyDataSetChanged()


            }
        }


        /* if (){
             filteredDocumentsList.clear()
             filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "nine_months" })
             selectedTabPosition = 3
             changeMonthTabsColor(binding.tv9Month, binding.view9Month)
             dailyReportAdapter?.notifyDataSetChanged()
         }else if (binding.tvChildAge.text.toString().toInt() == 1){
             filteredDocumentsList.clear()
             filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "nine_months" })
             selectedTabPosition = 3
             changeMonthTabsColor(binding.tv9Month, binding.view9Month)
         }*/


        /*  filteredDocumentsList.clear()
          filteredDocumentsList.addAll(reportDocumentsList.filter { it.type == "two_months" })
          changeMonthTabsColor(binding.tv2Month, binding.view2Month)
          dailyReportAdapter?.notifyDataSetChanged()*/
    }

    private fun callAPItoDeleteReport(media: String, pos: Int, medicalInfoId: Int) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteMedicalReportMedia(
                "Bearer " + userManager.accessToken ?: "",
                "$medicalInfoId"
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(
                        this@MedicalReportActivity,
                        response.body()?.message ?: ""
                    )
                    callAPItoGetMedicalInformation()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@MedicalReportActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@MedicalReportActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@MedicalReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun permissionAccess(position: Int) {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1)
            requestPermission(p1)
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2)
            requestPermission(p2)
        } else {

            Log.e(
                "filePath",
                Constants.IMG_BASE_PATH + reportDocumentsList[position].filePath ?: ""
            )
            val fileName = (reportDocumentsList[position].filePath ?: "").split("/").last()
            CustomToasts.successToast(this@MedicalReportActivity, "Downloading $fileName")

            val request = DownloadManager.Request(
                Uri.parse(
                    Constants.IMG_BASE_PATH + (reportDocumentsList[position].filePath ?: "")
                )
            )
            request.setTitle("Swaddle-$fileName")
                .setDescription("Downloading is in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
                .setDestinationInExternalFilesDir(
                    applicationContext,
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
//                .setDestinationInExternalPublicDir(
//                Environment.DIRECTORY_DOWNLOADS,
//                "/.Swaddle/$fileName"
//                )

                .setMimeType(
                    getMimeType(
                        Uri.parse(
                            Constants.IMG_BASE_PATH + (reportDocumentsList[position].filePath ?: "")
                        ).toString()
                    )
                )

//                .setMimeType("*/*")
//                .setMimeType(".pdf")
//                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }


    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this@MedicalReportActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@MedicalReportActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MedicalReportActivity,
                arrayOf(permission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            //Do the stuff that requires permission...
            Log.e("TAG", "Not say request")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                Log.e("TAG", "val " + grantResults[0])
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionAccess(downloadedPosition)
                } else {
                    Toast.makeText(
                        this@MedicalReportActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun callAPItoUploadDocuments(pos: Int = 0) {
        showProgressBar()
        var paramsList = ArrayList<String>()
        paramsList.add("two_months")
        paramsList.add("four_months")
        paramsList.add("six_months")
        paramsList.add("nine_months")
        paramsList.add("twelve_months")
        paramsList.add("fifteen_months")
        paramsList.add("eighteen_months")
        paramsList.add("two_years")
        paramsList.add("three_years")
        paramsList.add("four_years")
        paramsList.add("five_years")

        var filesList = ArrayList<File>()
        documentPath.forEachIndexed { index, s ->
            val fileURI = Uri.parse(s)
            val file = File(fileURI.path ?: "")
            filesList.add(file)
        }

        var api =
            AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "add_medical_information")
        if (filesList.size > 0) {
            api.addMultipartFileList("files[]", filesList)
        }

        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
        api.addMultipartParameter("child_id", (selectedChild?.id ?: 0).toString())

        paramsList.forEachIndexed { index, s ->
            if (pos == index) {
                api.addMultipartParameter(s, "200")
            } else {
                api.addMultipartParameter(s, "")
            }
        }

        api.setTag("UploadMedicalReport")
        api.setPriority(Priority.HIGH)
        api.build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                Log.e(
                    "progress",
                    ((bytesUploaded)).toString() + " TotalBytes: " + totalBytes
                )
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("Response", response.toString())

                    var data = gson.fromJson(
                        response.toString(),
                        UpdateProfileResponse::class.java
                    )
                    if (data.status == true) {
                        // do anything with response
                    } else {
                        showErrorToast(this@MedicalReportActivity, data.message ?: "")
                    }
                    hideProgressBar()

                    callAPItoGetMedicalInformation()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(this@MedicalReportActivity, error.message ?: "")
                }
            })
    }

    private fun showProgressBar() {
        binding.tvUpload.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvUpload.viewVisible()
        binding.progressbar.viewGone()
    }
}