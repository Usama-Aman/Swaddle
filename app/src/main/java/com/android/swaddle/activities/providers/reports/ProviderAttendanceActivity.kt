package com.android.swaddle.activities.providers.reports

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.parent.AttendanceHistoryAdapter
import com.android.swaddle.adapters.WeeklyChildAttendanceAdapter
import com.android.swaddle.adapters.provider_adapter.DailyReportAdapter
import com.android.swaddle.adapters.provider_adapter.IncidentReportAdapter
import com.android.swaddle.adapters.spinneradapter.AuthorizedAdultsSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityProviderAttandanceBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddIncidentReportBottomSheet
import com.android.swaddle.fragments.bottomSheetFragment.ClickListener
import com.android.swaddle.fragments.bottomSheetFragment.ParentsBottomSheet
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.progress.view.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// Used Only for Provider & Staff
class ProviderAttendanceActivity : BaseActivity(), WeeklyChildAttendanceAdapter.ClickListener,
    AttendanceHistoryAdapter.ItemClickListener, IncidentReportAdapter.ClickListener {

    private lateinit var binding: ActivityProviderAttandanceBinding
    private var mCalendar: Calendar = Calendar.getInstance()
    private var picker: DatePickerDialog? = null
    var arrivalTime = ""
    var departureTime = ""
    var attendanceDate = ""
    var formatedAttendanceDate = ""

    var dropOffMarked = false
    var pickUpMarked = false

    var childesList = ArrayList<ChildInfo>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var authorizedAdultsList = ArrayList<User>()

    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    var selectedDropOff: User? = null
    var selectedPickUp: User? = null

    var days: MutableList<WeeklyChildAttendanceData> = ArrayList()

    private var daysAdapter: WeeklyChildAttendanceAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var authorizedAdultsSpinnerAdapter: AuthorizedAdultsSpinnerAdapter? = null
    private var childesAdapter: SelectChildSpinnerAdapter? = null

    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1

    var downloadedPosition = 0
    var selectedPos = 0
    var reportDocumentsList = ArrayList<DailyReport>()
    private var deletedPosition = -1

    private lateinit var mcont: Context

    var list = ArrayList<ChildInfo>()
    var incidentAdapter: IncidentReportAdapter? = null
    private var dailyReportAdapter: DailyReportAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderAttandanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mcont = this@ProviderAttendanceActivity
        selectedPos = intent.getIntExtra("position", 0)

        init()

        selectRandomDate()
    }

    override fun onResume() {
        super.onResume()
        callAPItoGetClassRooms()
    }

    private fun selectRandomDate() {
        var date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvSelectDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)
                //TODO call api to get child attendance

                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate: String = df.format(mCalendar.time)
                getWeeklyChildAttendance(formattedDate)

                val dayOfTheWeek = DateFormat.format("EEEE", mCalendar.time) as String // Thursday
                val day = DateFormat.format("dd", mCalendar.time) as String // 20
                val monthString = DateFormat.format("MMM", mCalendar.time) as String // Jun
                val monthNumber = DateFormat.format("MM", mCalendar.time) as String // 06
                val year = DateFormat.format("yyyy", mCalendar.time) as String // 2013

                attendanceDate = "$day-$monthNumber-$year"
                formatedAttendanceDate = formattedDate

                callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                callAPItoGetAuthorizedAdults((selectedChild?.id ?: 0))
                callAPItoGetChildDailyReport()
            }

        binding.tvSelectDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)
        binding.cvSelectDate.setOnClickListener {
            val datepicker = DatePickerDialog(
                this, R.style.MySpinnerDatePickerStyle, date, mCalendar
                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datepicker.datePicker.maxDate = System.currentTimeMillis()
            datepicker.show()
        }
    }

    private fun callAPItoGetAuthorizedAdults(childId: Int) {
        val call: Call<AuthorizedAdultsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getAuthorizedAdults("Bearer " + userManager.accessToken ?: "", childId.toString())
        call.enqueue(object : Callback<AuthorizedAdultsResponse> {
            override fun onResponse(
                call: Call<AuthorizedAdultsResponse>,
                response: Response<AuthorizedAdultsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                authorizedAdultsList = response.body()?.users ?: ArrayList()
                listeners()
                setAuthorizedAdultsSpinner()
            }

            override fun onFailure(
                call: Call<AuthorizedAdultsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetCurrentDayAttendance(childId: Int) {
        val call: Call<GetAttendanceResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildAttendanceOfToday(
                "Bearer " + userManager.accessToken ?: "",
                childId.toString(),
                attendanceDate
            )
        call.enqueue(object : Callback<GetAttendanceResponse> {
            override fun onResponse(
                call: Call<GetAttendanceResponse>,
                response: Response<GetAttendanceResponse>
            ) {
                hideLoading()

                binding.tvArrival.text = ""
                binding.tvDeparture.text = ""

                arrivalTime = ""
                departureTime = ""

                selectedDropOff = null
                selectedPickUp = null


//                if (response.body()?.attendance == null) {
//                    binding.recDate.viewGone()
//                    binding.noData.viewVisible()
//                } else {
//                    binding.recDate.viewVisible()
//                    binding.noData.viewGone()
                setUpAttendance(response.body()?.attendance)
//                    Log.e("StudentAttendance", response.body()?.attendance.toString())
//                }

            }

            override fun onFailure(
                call: Call<GetAttendanceResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setUpAttendance(attendance: Attendance?) {
        var lastAttendance: ChildAttendanceMultiple? = null
        if (attendance?.childAttendanceMultiple?.size ?: 0 > 0)
            lastAttendance = attendance?.childAttendanceMultiple?.last()
        if (lastAttendance?.arrivalTime != null) {
            binding.tvArrival.text =
                DateConverter.convertTime24Attendance(lastAttendance.arrivalTime ?: "")
            arrivalTime = lastAttendance.arrivalTime ?: ""
            setUpParent(lastAttendance.droppedOffBy, binding.ivChildDropOff, binding.tvDropOff)
            selectedDropOff = lastAttendance.droppedOffBy
        } else {
            arrivalTime = ""
            binding.tvArrival.text = ""
            dropOffMarked = false
            binding.tvDropOff.text = ""
            selectedDropOff = null
            Glide.with(applicationContext)
                .load("")
                .placeholder(R.drawable.ic_acc_circle_small).into(binding.ivChildDropOff)
        }
        if (lastAttendance?.departureTime != null) {
            binding.tvDeparture.text =
                DateConverter.convertTime24Attendance(lastAttendance?.departureTime ?: "")
            departureTime = lastAttendance.departureTime ?: ""
            setUpParent(lastAttendance.pickedUpBy, binding.ivChildPickup, binding.tvPickUp)
            selectedPickUp = lastAttendance.pickedUpBy
        } else {
            binding.tvDeparture.text = ""
            departureTime = ""
            selectedPickUp = null
            pickUpMarked = false
            binding.tvPickUp.text = ""
            Glide.with(applicationContext)
                .load("")
                .placeholder(R.drawable.ic_acc_circle_small).into(binding.ivChildPickup)
        }
        setRecycler(attendance?.childAttendanceMultiple ?: ArrayList())
//        if (attendance?.childAttendanceMultiple != null && (attendance.childAttendanceMultiple?.size
//                ?: 0) > 1
//        ) {
//
//        } else {
//
//        }
    }

    private fun getListPosition(id: Int?, callback: (Int) -> Unit) {
        authorizedAdultsList.forEachIndexed { index, user ->
            if (user.id == id) {
                callback(index)
                return@forEachIndexed
            }
        }

    }

    private fun setRecycler(childAttendanceMultiple: ArrayList<ChildAttendanceMultiple>) {
        if (childAttendanceMultiple.size > 0)
            childAttendanceMultiple.removeLast()
        binding.rvArrival.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvArrival.adapter =
            AttendanceHistoryAdapter(applicationContext, childAttendanceMultiple, this, true)

        binding.rvDeparture.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvDeparture.adapter =
            AttendanceHistoryAdapter(applicationContext, childAttendanceMultiple, this, false)
    }

    private fun callAPItoGetClassRooms(
    ) {

        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getClassRooms("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
                hideLoading()
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()

            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callADIToSubmitAttendance(
        childInfo: ChildInfo
    ) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .submitAttendance(
                "Bearer " + userManager.accessToken ?: "",
                "${childInfo.id}",
                attendanceDate,
                arrivalTime,
                departureTime,
                "${selectedDropOff?.id}",
                "${selectedPickUp?.id}"
            )
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {

                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(
                        this@ProviderAttendanceActivity,
                        response.body()?.message ?: ""
                    )
                    finish()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ProviderAttendanceActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ProviderAttendanceActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvArrival.setOnClickListener {
            if (!dropOffMarked) {
                showTimerClock { sTime, oTime ->
                    binding.tvArrival.text = sTime
                    arrivalTime = sTime
                }
            }
        }

        binding.tvDeparture.setOnClickListener {
            if (!pickUpMarked) {
                showTimerClock(true) { sTime, oTime ->
                    if(binding.tvArrival.text.toString().isEmpty()){
                        showErrorToast(this, "Please Select Arrival time first.")
                        return@showTimerClock
                    }
                    if(validTime(binding.tvArrival.text.toString(),sTime)){
                        binding.tvDeparture.text = sTime
                        departureTime = sTime
                    }
                    else{
                        binding.tvDeparture.text = ""
                        departureTime = ""
                        showErrorToast(this, "Departure time should be greater than From Arrival time.")
                    }
                }
            }
        }

        binding.tvSubmit.setOnClickListener {
            if (!dropOffMarked && !pickUpMarked) {
                if (validateAttendance()) {
                    callADIToSubmitAttendance(selectedChild ?: ChildInfo())
                }
            }
        }

        binding.cvSpinnerPickedBy.setOnClickListener {
            if (selectedPickUp != null) {
                authorizedAdultsList.filter { it.isUserSelected }.forEachIndexed() { index, user ->
                    authorizedAdultsList[index].isUserSelected = selectedPickUp?.id == user.id
                }

            } else {
                authorizedAdultsList.filter { it.isUserSelected }.forEach {
                    it.isUserSelected = false
                }

            }


            var sheet = ParentsBottomSheet(
                this@ProviderAttendanceActivity,
                "Picked up by",
                authorizedAdultsList
            )
            sheet.setListener(object : ParentsBottomSheet.ClickListener {
                override fun onSelectNowClicked(selectedParents: User?) {
                    selectedPickUp = selectedParents
                    Log.e("selectedPickUp", selectedParents?.id.toString())
                    setUpParent(selectedParents, binding.ivChildPickup, binding.tvPickUp)
                }

                override fun onCancelClicked() {

                }
            })
            sheet.show(supportFragmentManager, "ParentsBottomSheet")
        }
        binding.cvSpinnerDropOff.setOnClickListener {
            if (selectedDropOff != null) {
//                authorizedAdultsList.forEachIndexed { index, user ->
//                    authorizedAdultsList[index].isUserSelected = selectedDropOff?.id == user.id
//                }
//            }


                authorizedAdultsList.filter { it.isUserSelected }.forEachIndexed() { index, user ->
                    authorizedAdultsList[index].isUserSelected = selectedDropOff?.id == user.id
                }

            } else {
                authorizedAdultsList.filter { it.isUserSelected }.forEach {
                    it.isUserSelected = false
                }

            }

            var sheet = ParentsBottomSheet(
                this@ProviderAttendanceActivity,
                "Dropped off by",
                authorizedAdultsList
            )
            sheet.setListener(object : ParentsBottomSheet.ClickListener {
                override fun onSelectNowClicked(selectedParents: User?) {
                    selectedDropOff = selectedParents
                    Log.e("selectedDropOff", selectedParents?.id.toString())
                    setUpParent(selectedParents, binding.ivChildDropOff, binding.tvDropOff)
                }

                override fun onCancelClicked() {

                }
            })
            sheet.show(supportFragmentManager, "ParentsBottomSheet")
        }
    }

    private fun init() {

    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter =
                ClassSpinnerAdapter(this@ProviderAttendanceActivity, classRoomsList)
            binding.spClassRoom.adapter = classRoomsAdapter
        } else {
            classRoomsAdapter?.setItems(classRoomsList)
            classRoomsAdapter?.notifyDataSetChanged()
        }
        binding.spClassRoom.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedClassRoom = classRoomsList[position]
                    callAPItoGetChildes(classRoomsList[position].id)
                    Log.e("SelectedRoom", classRoomsList[position].id.toString())
                    //   showSuccessToast(this@ProviderAttendanceActivity,"Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()

            //callAPItoGetChildes(classRoomsList.first().id)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.tvArrival.text = ""
            binding.tvDeparture.text = ""
            binding.clDropOff.viewGone()
            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.clPickUp.viewGone()
            selectedChild = null
            selectedPickUp = null
            selectedDropOff = null
        }
        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun setUpParent(selectedParents: User?, iv: CircleImageView, tv: TextView) {

        if (selectedParents?.firstName == null || selectedParents.relation == null) {
            tv.text = ""
        } else {
            tv.text =
                selectedParents?.firstName + " (" + selectedParents?.relation + ")"
            iv.let {
                try {
                    Glide.with(applicationContext)
                        .load(Constants.IMG_BASE_PATH + selectedParents?.profilePicture ?: "")
                        .placeholder(R.drawable.ic_acc_circle_small).into(it)
                }catch (ex: Exception){

                }

            }
        }
    }

    private fun setAuthorizedAdultsSpinner() {

//        if (authorizedAdultsSpinnerAdapter == null) {
//            authorizedAdultsSpinnerAdapter =
//                AuthorizedAdultsSpinnerAdapter(
//                    this@ProviderAttendanceActivity,
//                    authorizedAdultsList
//                )
//            binding.spDropOff.adapter = authorizedAdultsSpinnerAdapter
//            binding.spPickedBy.adapter = authorizedAdultsSpinnerAdapter
//        } else {
//            authorizedAdultsSpinnerAdapter?.setItems(authorizedAdultsList)
//            authorizedAdultsSpinnerAdapter?.notifyDataSetChanged()
//        }
//        binding.spDropOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                selectedDropOff = authorizedAdultsList[position]
//                Log.e("selectedDropOff", authorizedAdultsList[position].id.toString())
//                //   showSuccessToast(this@DailyReportActivity,"Click")
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//        }
//        binding.spPickedBy.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parent: AdapterView<*>,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    selectedPickUp = authorizedAdultsList[position]
//                    Log.e("selectedPickUp", authorizedAdultsList[position].id.toString())
//                    //   showSuccessToast(this@DailyReportActivity,"Click")
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//
//                }
//            }

        if (authorizedAdultsList.size > 0) {
            binding.tvSpinnerDropOffNoData.viewGone()
            binding.clDropOff.viewVisible()

            binding.tvSpinnerPickedByNoData.viewGone()
            binding.clPickUp.viewVisible()
        } else {
            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.tvArrival.text = ""
            binding.tvDeparture.text = ""
            binding.clDropOff.viewGone()

            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.clPickUp.viewGone()
        }
    }

    private fun setChildSpinner() {
        childesAdapter = null
        if (childesAdapter == null) {
            childesAdapter =
                SelectChildSpinnerAdapter(
                    this@ProviderAttendanceActivity,
                    childesList,
                    true
                )
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(childesList)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (childesList.size > 0) {
                        selectedChild = childesList[position]
                    }

                    if (selectedChild != null) {
                        getWeeklyChildAttendance(formatedAttendanceDate)
                        callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                        callAPItoGetAuthorizedAdults((selectedChild?.id ?: 0))
                        callAPItoGetChildDailyReport()
                    }
                    //  showSuccessToast(this@PaymentDetailActivity, "Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            selectedChild = childesList.first()
            //callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
            //callAPItoGetAuthorizedAdults((selectedChild?.id ?: 0))
            //callAPItoGetChildDailyReport()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()

            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.clDropOff.viewGone()
            binding.tvArrival.text = ""
            binding.tvDeparture.text = ""
            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.clPickUp.viewGone()
            selectedChild = null
            selectedPickUp = null
            selectedDropOff = null
        }

//        allDaysOfWeek()

        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate: String = df.format(mCalendar.time)
        formatedAttendanceDate = DateFormat.format("yyyy-MM-dd", mCalendar.time) as String
        attendanceDate = DateFormat.format("yyyy-MM-dd", mCalendar.time) as String
        getWeeklyChildAttendance(formatedAttendanceDate)
    }

    private fun getWeeklyChildAttendance(date: String) {
        val call: Call<WeeklyChildAttendanceModel> =
            RetrofitClass.getInstance().webRequestsInstance
                .getWeeklyChildAttendance(
                    "Bearer " + userManager.accessToken ?: "",
                    (selectedChild?.id ?: 0).toString(),
                    date
                )
        call.enqueue(object : Callback<WeeklyChildAttendanceModel> {
            override fun onResponse(
                call: Call<WeeklyChildAttendanceModel>,
                response: Response<WeeklyChildAttendanceModel>
            ) {

                try {
                    val model = response.body()
                    days.clear()
                    days.addAll(model?.data!!)

                    var todayAdded = false

                    val today = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val selectedDate = dateFormat.parse(date)
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.time = selectedDate ?: Date()

                    Log.d("Calendar.DAY_OF_MONTH", "${today.get(Calendar.DAY_OF_MONTH)}")
                    Log.d(
                        "Selected.DAY_OF_MONTH",
                        "${selectedCalendar.get(Calendar.DAY_OF_MONTH)}"
                    )

                    for (i in days.indices) {
                        when {
                            selectedCalendar.get(Calendar.DAY_OF_MONTH) == days[i].day.toInt() -> {
                                days[i].isSelected = true

                                val dayOfTheWeek =
                                    DateFormat.format(
                                        "EEEE",
                                        selectedDate
                                    ) as String // Thursday
                                val day =
                                    DateFormat.format("dd", selectedDate) as String // 20
                                val monthString =
                                    DateFormat.format("MMM", selectedDate) as String // Jun
                                val monthNumber =
                                    DateFormat.format("MM", selectedDate) as String // 06
                                val year = DateFormat.format(
                                    "yyyy",
                                    selectedDate
                                ) as String // 2013

                                attendanceDate = "$day-$monthNumber-$year"
                                formatedAttendanceDate = "$year-$monthNumber-$day"
                                Log.e("date", attendanceDate)

                                if (!todayAdded) {
                                    days[i].isEnabled = true
                                    todayAdded = isToday(dateFormat.parse(days[i].date))
                                } else {
                                    days[i].isEnabled = false
                                }

                            }
                            else -> {
                                days[i].isSelected = false

                                if (!todayAdded) {
                                    days[i].isEnabled = true
                                    todayAdded = isToday(dateFormat.parse(days[i].date))
                                } else {
                                    days[i].isEnabled = false
                                }

                                Log.d(
                                    "isToda",
                                    "${isToday(dateFormat.parse(days[i].date))}"
                                )
                            }
                        }
                    }

                    val manager = LinearLayoutManager(
                        this@ProviderAttendanceActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.recDate.layoutManager = manager
                    daysAdapter =
                        WeeklyChildAttendanceAdapter(
                            this@ProviderAttendanceActivity,
                            days,
                            this@ProviderAttendanceActivity
                        )
                    binding.recDate.adapter = daysAdapter

                    if (selectedChild != null)
                        callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                call: Call<WeeklyChildAttendanceModel>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

//    private fun allDaysOfWeek() {
//        days = ArrayList()
//
//        val calendar = Calendar.getInstance()
//        val today = Calendar.getInstance()
//        calendar.firstDayOfWeek = Calendar.MONDAY
//        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
//        var toDayAdded = false
//        //  val days = arrayOfNulls<String>(7)
//        for (i in 0..6) {
//            //days[i] = format.format(calendar.time)
//            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
//                toDayAdded = true
//                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))
//
//                val dayOfTheWeek =
//                    DateFormat.format("EEEE", days[i].date) as String // Thursday
//                val day = DateFormat.format("dd", days[i].date) as String // 20
//                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
//                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
//                val year = DateFormat.format("yyyy", days[i].date) as String // 2013
//
//                attendanceDate = "$day-$monthNumber-$year"
//                Log.e("date", attendanceDate)
//            } else {
//                if (toDayAdded)
//                    days.add(
//                        CalendarModel(
//                            calendar.time,
//                            isEnabled = false,
//                            isSelected = false
//                        )
//                    )
//                else
//                    days.add(
//                        CalendarModel(
//                            calendar.time,
//                            isEnabled = true,
//                            isSelected = false
//                        )
//                    )
//            }
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1)
//            Log.i("adsf", "" + days[i])
//        }
//
//        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        binding.recDate.layoutManager = manager
//        daysAdapter = WeeklyChildAttendanceAdapter(this, days, this)
//        binding.recDate.adapter = daysAdapter
//
//        if (selectedChild != null)
//            callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
//
//    }

    private fun callAPItoGetChildDailyReport() {
        val call: Call<DailyReportResponse> =
            RetrofitClass.getInstance().webRequestsInstance
                .getChildDailyReportDetails(
                    "Bearer " + userManager.accessToken ?: "",
                    (selectedChild?.id ?: 0).toString(),
                    attendanceDate
                )
        call.enqueue(object : Callback<DailyReportResponse> {
            override fun onResponse(
                call: Call<DailyReportResponse>,
                response: Response<DailyReportResponse>
            ) {
                reportDocumentsList = ArrayList()
                reportDocumentsList.clear()
                if (response.body()?.data != null) {
                    reportDocumentsList.add(response.body()?.data ?: DailyReport())
                }
                setReportRecyclerView()
                //hideLoading()
                callAPItoGetIncidenceReports()

            }

            override fun onFailure(
                call: Call<DailyReportResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setReportRecyclerView() {
        if (dailyReportAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recDailyReport.layoutManager = manager
            dailyReportAdapter =
                DailyReportAdapter(
                    this@ProviderAttendanceActivity,
                    reportDocumentsList,
                    object :
                        DailyReportAdapter.ItemClickListener {
                        override fun onItemClick(position: Int) {
                            if (userManager.user?.type == Constants.provider || userManager.user?.type == Constants.staff) {
                                if (selectedChild != null) {
                                    startActivity<AddReportActivity>(
                                        "item" to selectedChild,
                                        "draft" to reportDocumentsList[position],
                                        "dateToSave" to formatedAttendanceDate
                                    )
                                }
                            } else {
                                openFileInBrowser(
                                    position,
                                    Constants.IMG_BASE_PATH + reportDocumentsList[position].filePath
                                        ?: ""
                                )
                            }
                        }

                        override fun onDownloadClick(position: Int) {
                            permissionAccess(position)
                            downloadedPosition = position
                        }

                        override fun onOpenDraftClick(position: Int) {
                            if (selectedChild != null) {
                                if (reportDocumentsList[position].draft == 1 && (userManager.user?.type == Constants.provider || userManager.user?.type == Constants.staff)) {
                                    startActivity<AddReportActivity>(
                                        "item" to selectedChild,
                                        "draft" to reportDocumentsList[position],
                                        "dateToSave" to formatedAttendanceDate
                                    )
                                } else {
                                    openFileInBrowser(
                                        position,
                                        Constants.IMG_BASE_PATH + reportDocumentsList[position].filePath
                                            ?: ""
                                    )
                                }
                            } else {
                                CustomToasts.failureToast(
                                    this@ProviderAttendanceActivity,
                                    "No Child Selected!"
                                )
                            }
                        }

                        override fun onDeleteClick(position: Int) {
                            deletedPosition = position

                            val alert =
                                AlertView(
                                    "Delete?",
                                    "Are you sure you want to Delete this Report?",
                                    AlertStyle.DIALOG
                                )
                            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                                //=call api to remove from Classroom...
                                callAPItoDeleteReport(
                                    reportDocumentsList[position].filePath ?: "",
                                    position, reportDocumentsList[position].id ?: 0
                                )
                            })
                            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                            })
                            alert.show(this@ProviderAttendanceActivity)


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
    }

    private fun callAPItoDeleteReport(media: String, pos: Int, medicalInfoId: Int) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteChildDailyReport(
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
                        this@ProviderAttendanceActivity,
                        response.body()?.message ?: ""
                    )
                    callAPItoGetChildDailyReport()
                    callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ProviderAttendanceActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ProviderAttendanceActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun setDropOffSpinner() {
        if (childesAdapter == null) {
            childesAdapter =
                SelectChildSpinnerAdapter(this@ProviderAttendanceActivity, childesList)
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(childesList)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (childesList.size > 0) {
                        selectedChild = childesList[position]
                    }
                    //  showSuccessToast(this@PaymentDetailActivity, "Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
        }
    }

    private fun setPickUpSpinner() {
        if (childesAdapter == null) {
            childesAdapter =
                SelectChildSpinnerAdapter(this@ProviderAttendanceActivity, childesList)
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(childesList)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (childesList.size > 0) {
                        selectedChild = childesList[position]
                    }
                    //  showSuccessToast(this@PaymentDetailActivity, "Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTimerClock(
        setMin: Boolean = false,
        callback: (String, String) -> Unit
    ) {
        // var miniTimeCalendar = Calendar.getInstance()
        val picker = android.app.TimePickerDialog(
            this, R.style.MySpinnerTimePickerStyle,
            { _, hourOfDay, minute ->
                val h = String.format("%02d", hourOfDay)
                val m = String.format("%02d", minute)
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                mCalendar.set(Calendar.MINUTE, minute)

                val time = "$h:$m"

                //tvTime.text = DateConverter.getFormattedTime(hourOfDay, minute)

                //callback(DateConverter.convertToFromTimePicker(time))
                //callback(time)

                var AM_PM = " AM"
                var mm_precede = ""
                var hourr = hourOfDay
                if (hourOfDay >= 12) {
                    AM_PM = " PM"
                    if (hourOfDay in 13..23) {
                        hourr -= 12
                    } else {
                        hourr = 12
                    }
                } else if (hourOfDay === 0) {
                    hourr = 12
                }
                if (minute < 10) {
                    mm_precede = "0"
                }

                val selectedTime = "$hourr:$mm_precede$minute$AM_PM"
                callback(selectedTime, time)
            }, mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            false
        )
        picker.show()
        /*val dpd = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, second ->
                val h = String.format("%02d", hourOfDay)
                val m = String.format("%02d", minute)
                val s = String.format("%02d", second)
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                mCalendar.set(Calendar.MINUTE, minute)
                mCalendar.set(Calendar.SECOND, second)

                val time = "$h:$m:$s"

                //tvTime.text = DateConverter.getFormattedTime(hourOfDay, minute)

                //callback(DateConverter.convertToFromTimePicker(time))
                //callback(time)

                var AM_PM = " AM"
                var mm_precede = ""
                var hourr = hourOfDay
                if (hourOfDay >= 12) {
                    AM_PM = " PM"
                    if (hourOfDay in 13..23) {
                        hourr -= 12
                    } else {
                        hourr = 12
                    }
                } else if (hourOfDay === 0) {
                    hourr = 12
                }
                if (minute < 10) {
                    mm_precede = "0"
                }

                val selectedTime = "$hourr:$mm_precede$minute$AM_PM"
                callback(selectedTime, time)
            },
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            mCalendar.get(Calendar.SECOND),
            false
        )
        if (setMin) {
            dpd.setMinTime(
                mCalendar.get(Calendar.HOUR_OF_DAY),
                (mCalendar.get(Calendar.MINUTE) + 1),
                mCalendar.get(Calendar.SECOND)
            )
        }
        dpd.version = TimePickerDialog.Version.VERSION_1
        //dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "Datepickerdialog")*/
    }

    private fun callAPItoGetChildes(id: Int?) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesListOfRoom(
                "Bearer " + userManager.accessToken ?: "",
                (id ?: 0).toString(), "0"
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
                showErrorToast(
                    this@ProviderAttendanceActivity,
                    "Can't Connect to Server!"
                )
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun validateAttendance(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@ProviderAttendanceActivity, "No internet connection!")
            return false
        }

        if (selectedClassRoom == null) {
            showErrorToast(this@ProviderAttendanceActivity, "Please Select Classroom!")
            return false
        }

        if (selectedChild == null) {
            showErrorToast(
                this@ProviderAttendanceActivity,
                "Please Select Child for Attendance!"
            )
            return false
        }

        if (attendanceDate == "") {
            showErrorToast(
                this@ProviderAttendanceActivity,
                "Please Select Attendance Date!"
            )
            return false
        }

//        if (arrivalTime == "" || departureTime == "") {
//            showErrorToast(
//                this@ProviderAttendanceActivity,
//                "Please select drop off and pick time!"
//            )
//            return false
//        }

        if (arrivalTime != "" && selectedDropOff == null) {
            showErrorToast(
                this@ProviderAttendanceActivity,
                "Please select drop off parent!"
            )
            return false
        }
        if (departureTime != "" && selectedPickUp == null) {
            showErrorToast(
                this@ProviderAttendanceActivity,
                "Please select pickup parent!"
            )
            return false
        }

        if (arrivalTime == "" && departureTime != "") {
            showErrorToast(
                this@ProviderAttendanceActivity,
                "Please mark drop off attendance before pick attendance!"
            )
            return false
        }
        return true
    }

    override fun onITemClick(position: Int, item: WeeklyChildAttendanceData) {

        if (item.isEnabled && !item.isSelected) {
            days.forEachIndexed { _, calendarModel ->
                calendarModel.isSelected = false
            }
            days[position].isSelected = !days[position].isSelected
            daysAdapter?.notifyDataSetChanged()


            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val selectedDate = dateFormat.parse(item.date)

            val dayOfTheWeek = DateFormat.format("EEEE", selectedDate) as String // Thursday
            val day = DateFormat.format("dd", selectedDate) as String // 20
            val monthString = DateFormat.format("MMM", selectedDate) as String // Jun
            val monthNumber = DateFormat.format("MM", selectedDate) as String // 06
            val year = DateFormat.format("yyyy", selectedDate) as String // 2013

            attendanceDate = "$day-$monthNumber-$year"
            formatedAttendanceDate = "$year-$monthNumber-$day"
            Log.e("date", attendanceDate)
//
//        if (date != "" && selectedChild != null) {
            showLoading()
            callAPItoGetChildDailyReport()
            callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
        }
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

            val request = DownloadManager.Request(
                Uri.parse(
                    Constants.IMG_BASE_PATH + (reportDocumentsList[position].filePath ?: "")
                )
            )
            request.setTitle("Swaddle-$fileName")
                .setDescription("Downloading is in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
                .setMimeType(".pdf")
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this@ProviderAttendanceActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@ProviderAttendanceActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ProviderAttendanceActivity,
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
                        this@ProviderAttendanceActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun callAPItoGetIncidenceReports() {
        var childId = (selectedChild?.id ?: 0).toString()
//            if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
//                ""
//            } else {
//                (selectedChild?.id ?: 0).toString()
//            }

        var classId = ""
//            if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
//                (selectedClassRoom?.id ?: 0).toString()
//            } else {
//                ""
//            }
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getIncidenceReport(
                "Bearer " + userManager.accessToken ?: "",
                childId,
                classId,
                attendanceDate
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                list = response.body()?.data ?: ArrayList()
                setIngRecView()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setIngRecView() {
        if (list.size > 0) {
            if (list[0].incidentReports == null) {
                binding.tvNoIncidentReportsFound.viewVisible()
                list.clear()
            } else {
                binding.tvNoIncidentReportsFound.viewGone()
            }
        }
        if (incidentAdapter == null) {
            val manager = LinearLayoutManager(
                this@ProviderAttendanceActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.recIncidentReport.layoutManager = manager
            incidentAdapter = IncidentReportAdapter(this@ProviderAttendanceActivity, list, this, attendanceDate)
            binding.recIncidentReport.adapter = incidentAdapter
        } else {
            incidentAdapter?.setItems(list, attendanceDate)
            incidentAdapter?.notifyDataSetChanged()
        }

        if (list.size > 0) {
            binding.tvNoIncidentReportsFound.viewGone()
        } else {
            binding.tvNoIncidentReportsFound.viewVisible()
        }
    }

    override fun onITemClick(position: Int, item: ChildInfo) {
        /* if (item.incidentReports != null)
             startActivity<IncidentReportDetailsAndUpdate>("item" to item, "selectedDate" to attendanceDate)*/
    }


    override fun onViewClick(position: Int, item: ChildInfo) {
        if (item.incidentReports != null)
            startActivity<IncidentReportDetailsAndUpdate>(
                "item" to item,
                "selectedDate" to attendanceDate,
                "canUpdate" to false
            )
    }

    override fun onEditClick(position: Int, item: ChildInfo) {
        if (item.incidentReports != null)
            startActivity<IncidentReportDetailsAndUpdate>(
                "item" to item,
                "selectedDate" to attendanceDate,
                "canUpdate" to true,
            )
    }

    override fun onAddIncidenceReportClick(position: Int, item: ChildInfo) {
        val bottomFragment =
            AddIncidentReportBottomSheet(
                this@ProviderAttendanceActivity,
                item,
                selectedClassRoom ?: ClassroomDetails(),
                attendanceDate,
                false
            )
        bottomFragment.show(
            supportFragmentManager,
            "AddIncidentReportBottomSheet"
        )
        bottomFragment.setListener(object : ClickListener {
            override fun onCanceled(dialog: BottomSheetDialogFragment) {
                dialog.dismiss()
            }

            override fun onSuccessfullySubmitted(dialog: BottomSheetDialogFragment) {
                dialog.dismiss()
                callAPItoGetIncidenceReports()
            }
        })
    }

    private fun validTime(start: String, end: String): Boolean {
        val startHour = start.split(":")[0]
        val startMinute = start.split(":")[1].split(" ")[0]
        val startAmPM = start.split(" ")[1]

        val endHour = end.split(":")[0]
        val endMinute = end.split(":")[1].split(" ")[0]
        val endAmPM = end.split(" ")[1]
        if (startHour.toInt() == endHour.toInt()) {
            if (startMinute.toInt() <= endMinute.toInt()) {
                if (startAmPM == "AM" && endAmPM == "AM") {
                    return true
                }
                if (startAmPM == "PM" && endAmPM == "PM") {
                    return true
                }
            }
        }
        if (startHour.toInt() > endHour.toInt()) {
            if (startAmPM == "AM" && endAmPM == "PM") {
                return true
            }
            if (startHour.toInt() == 12 && startAmPM == "PM" && endAmPM == "PM") {
                return true
            }
        }
        if (startHour.toInt() < endHour.toInt()) {
            if (startAmPM == "AM" && endAmPM == "AM") {
                return true
            }
            if (startAmPM == "PM" && endAmPM == "PM") {
                return true
            }
            if (startAmPM == "AM" && endAmPM == "PM") {
                return true
            }
        }

        return false
    }
}