package com.android.swaddle.activities.providers.reports

import android.Manifest
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.parent.AttendanceHistoryAdapter
import com.android.swaddle.adapters.WeeklyChildAttendanceAdapter
import com.android.swaddle.adapters.child_adpaters.ChildIncidentReportAdapter
import com.android.swaddle.adapters.provider_adapter.DailyReportAdapter
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityDailyReportBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DailyReportActivity : BaseActivity(), DateAdapter.ClickListener,
    AttendanceHistoryAdapter.ItemClickListener {
    private var deletedPosition = -1
    private lateinit var binding: ActivityDailyReportBinding
    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1
    var downloadedPosition = 0

    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    var date = ""
    var dateTosave = ""
    var formatedAttendanceDate = ""
    var isDatePickerClicked = false

    val days = ArrayList<CalendarModel>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var reportDocumentsList = ArrayList<DailyReport>()
    var childesList = ArrayList<ChildInfo>()

    private var daysAdapter: DateAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var dailyReportAdapter: DailyReportAdapter? = null
    private var childesAdapter: SelectChildSpinnerAdapter? = null

    private var selectedPositionToCallApiWhenDraft = -1

    private lateinit var mcont: Context

    private var isFromAttendance: Boolean? = false
    private var incidentAdapter: ChildIncidentReportAdapter? = null
    var list = ArrayList<ChildInfo>()
    var reportsList = ArrayList<ChildInfo>()
    var classroom_id: Int = -1
    var child_id: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        init()
        mcont = this@DailyReportActivity
        listener()
        formatedAttendanceDate = DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time) as String
        date = DateFormat.format("dd-MM-yyyy", Calendar.getInstance().time) as String
        dateTosave = (DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time) as String) + DateFormat.format("HH:mm:ss", Calendar.getInstance().time).toString()
        if (userManager.user?.type == Constants.parent || userManager.user?.type == Constants.authorized_adult) {
            binding.ivAdd.viewGone()
            callAPItoGetParentsChildes()
        } else {
            binding.ivAdd.viewVisible()
            callAPItoGetClassRooms()
        }
        //allDaysOfWeek(Calendar.getInstance())
        selectRandomDate()

    }
    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
        child_id = intent.getIntExtra("child_id",-1)
    }
    private fun setReportRecyclerView() {
        if (dailyReportAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recDailyReport.layoutManager = manager
            dailyReportAdapter =
                DailyReportAdapter(
                    this@DailyReportActivity,
                    reportDocumentsList,
                    object :
                        DailyReportAdapter.ItemClickListener {
                        override fun onItemClick(position: Int) {
                            if (userManager.user?.type == Constants.provider || userManager.user?.type == Constants.staff) {
                                if (selectedChild != null) {
                                    startActivity<AddReportActivity>(
                                        "item" to selectedChild,
                                        "draft" to reportDocumentsList[position],
                                        "dateToSave" to dateTosave
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
                                        "dateToSave" to dateTosave
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
                                    this@DailyReportActivity,
                                    "No Child Selected!"
                                )
                            }
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
                                    reportDocumentsList[position].filePath ?: "",
                                    position, reportDocumentsList[position].id ?: 0
                                )
                            })
                            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                            })
                            alert.show(this@DailyReportActivity)


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
            .deleteChildDailyReport("Bearer " + userManager.accessToken ?: "", "$medicalInfoId")
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(
                        this@DailyReportActivity,
                        response.body()?.message ?: ""
                    )
                    callAPItoGetChildDailyReport()
                    callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@DailyReportActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@DailyReportActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@DailyReportActivity, classRoomsList)
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
                callAPItoGetChildes(selectedClassRoom?.id ?: 0)
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
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
            //callAPItoGetChildes(selectedClassRoom?.id ?: 0)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            selectedChild = null
            reportDocumentsList.clear()
            dailyReportAdapter?.setItems(reportDocumentsList)
            dailyReportAdapter?.notifyDataSetChanged()
            binding.tvNoReportsFound.viewVisible()
        }
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivAdd.setOnClickListener {


////            if (selectedChild != null)
////                if (/*(selectedChild?.absent_notes != null && selectedChild?.absent_notes?.is_absent == 0) ||*/
////                    (selectedChild?.attendance != null && selectedChild?.attendance?.arrivalTime != null && !selectedChild?.attendance?.arrivalTime.isNullOrEmpty())
////                ) {
//                    startActivity<AddReportActivity>(
//                        "item" to selectedChild,
//                        "dateToSave" to dateTosave
//                    )
////                } else {
////                    showErrorToast(mcont, "Selected child is absent today")
////                }
////            else {
////                showErrorToast(mcont, "Please select child")
////            }
//

            if (isTodayDate(date)) {
                if (selectedChild != null)
                    if (selectedChild?.childAttendance == null ||
                        (selectedChild?.childAttendance != null && selectedChild?.childAttendance?.childAttendanceMultiple?.isEmpty() == true) ||
                        (selectedChild?.childAttendance != null && selectedChild?.childAttendance?.childAttendanceMultiple?.last()?.droppedOffBy == null)
                    ) {
                        showErrorToast(mcont, "Selected child is absent today")
                    } else {
                        startActivity<AddReportActivity>(
                            "item" to selectedChild,
                            "dateToSave" to dateTosave
                        )
                    }
                else {
                    showErrorToast(mcont, "Please select child")
                }
            } else {
                startActivity<AddReportActivity>(
                    "item" to selectedChild,
                    "dateToSave" to dateTosave
                )
            }
        }
    }

    private fun isTodayDate(date: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val answer: String = current.format(formatter)
            answer == date
        } else {
            val current = Date()
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            val answer: String = formatter.format(current)
            answer == date
        }
    }

    private fun init() {
        //val fromAttendance = intent?.getBooleanExtra("fromAttendance", false)
        isFromAttendance = intent?.getBooleanExtra("fromAttendance", false)
        binding.toolBarTitle.text = if (isFromAttendance == true) "Attendance" else "Daily Reports"
        
        if (isFromAttendance == false) {
            binding.recIncidentReport.viewGone()
            binding.tvIncidentReport.viewGone()
        }


        if (userManager.user?.type == Constants.parent || userManager.user?.type == Constants.authorized_adult) {
            binding.llAttendanceDetails.viewVisible()
            binding.tvClassRoom.viewVisible()
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
        } else {
            binding.llAttendanceDetails.viewGone()
            binding.tvClassRoom.viewGone()
            binding.tvClass.viewVisible()
            binding.cvSpinnerClass.viewVisible()
        }

        val manager = LinearLayoutManager(
            this@DailyReportActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this@DailyReportActivity, days, this@DailyReportActivity)
        binding.recDate.adapter = daysAdapter
    }

    override fun onResume() {
        super.onResume()
        if (selectedChild != null) {
            callAPItoGetChildDailyReport()
            callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
        }
    }

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
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetChildes(id: Int?) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesListOfRoom(
                "Bearer " + userManager.accessToken ?: "",
                (id ?: 0).toString(),
                "0"
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
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        childesAdapter = null
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@DailyReportActivity, childesList)
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
                    getWeeklyChildAttendance(formatedAttendanceDate)
                    callAPItoGetChildDailyReport()
                    callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        populateChild()
        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
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
            //callAPItoGetChildDailyReport()
            //callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            reportDocumentsList.clear()
            dailyReportAdapter?.setItems(reportDocumentsList)
            dailyReportAdapter?.notifyDataSetChanged()
            binding.tvNoReportsFound.viewVisible()
            selectedChild = null
        }
    }

    private fun callAPItoGetCurrentDayAttendance(childId: Int) {
        val call: Call<GetAttendanceResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildAttendanceOfToday(
                "Bearer " + userManager.accessToken ?: "",
                childId.toString(),
                date
            )
        call.enqueue(object : Callback<GetAttendanceResponse> {
            override fun onResponse(
                call: Call<GetAttendanceResponse>,
                response: Response<GetAttendanceResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                setUpAttendance(response.body()?.attendance)

            }

            override fun onFailure(
                call: Call<GetAttendanceResponse>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
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
                DateConverter.convertTime24Attendance(attendance?.arrivalTime ?: "")
            binding.tvDropOff.text =
                lastAttendance.droppedOffBy?.firstName + " (" + lastAttendance.droppedOffBy?.relation + ")"
            Glide.with(applicationContext)
                .load(
                    Constants.IMG_BASE_PATH + lastAttendance.droppedOffBy?.profilePicture ?: ""
                )
                .placeholder(
                    R.drawable.ic_user_placeholder
                ).into(binding.ivDropOff)
            binding.tvDropOffNoData.viewGone()
            binding.clDropOff.viewVisible()
        } else {
            binding.tvDropOffNoData.viewVisible()
            binding.tvArrival.text = ""
            binding.clDropOff.viewGone()
        }

        try {
            if (lastAttendance?.departureTime != null) {
                binding.tvDeparture.text =
                    DateConverter.convertTime24Attendance(lastAttendance?.departureTime ?: "")
                binding.tvPickUp.text =
                    lastAttendance.pickedUpBy?.firstName + " (" + lastAttendance.pickedUpBy?.relation + ")"
                Glide.with(mcont)
                    .load(
                        Constants.IMG_BASE_PATH + lastAttendance.pickedUpBy?.profilePicture
                            ?: ""
                    )
                    .placeholder(
                        R.drawable.ic_user_placeholder
                    ).into(binding.ivPickUp)
                binding.ivPickUpNoData.viewGone()
                binding.clPickUp.viewVisible()
            } else {
                binding.tvDeparture.text = ""
                binding.ivPickUpNoData.viewVisible()
                binding.clPickUp.viewGone()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setRecycler(attendance?.childAttendanceMultiple ?: ArrayList())
//        if (attendance?.childAttendanceMultiple != null && (attendance.childAttendanceMultiple?.size
//                ?: 0) > 1
//        ) {
//
//        }
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

    private fun populateChild() {
        if (selectedChild?.classroomDetails != null) {
            binding.tvClassRoom.text = selectedChild?.classroomDetails?.name ?: ""
        } else {
            binding.tvClassRoom.text = ""
        }
    }

    private fun callAPItoGetParentsChildes() {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {

                hideLoading()
                childesList = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetChildDailyReport() {
        val call: Call<DailyReportResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildDailyReportDetails(
                "Bearer " + userManager.accessToken ?: "",
                (selectedChild?.id ?: 0).toString(),
                date
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
                if (isFromAttendance == true) {
                    callAPItoGetIncidenceReports()
                }
            }

            override fun onFailure(
                call: Call<DailyReportResponse>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun selectRandomDate() {
        val mCalendar: Calendar = Calendar.getInstance()
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvSelectDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)

                days.clear()
                isDatePickerClicked = true
                date = "$dayOfMonth-$${monthOfYear+1}-$year"
                dateTosave =
                    "$year-${monthOfYear+1}-$dayOfMonth " + DateFormat.format("HH:mm:ss", mCalendar.time)
                        .toString()
                formatedAttendanceDate = "$year-${monthOfYear+1}-$dayOfMonth"
                getWeeklyChildAttendance(formatedAttendanceDate)
                callAPItoGetChildDailyReport()
                callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                //allDaysOfWeek(mCalendar)
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

    private fun allDaysOfWeek(selectedDateCalender: Calendar) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedDateCalender.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, selectedDateCalender.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, selectedDateCalender.get(Calendar.DAY_OF_MONTH))
        //calendar.add(selectedDateCalender.get(Calendar.DAY_OF_MONTH),selectedDateCalender.get(Calendar.MONDAY) - selectedDateCalender.get(calendar.get(Calendar.DAY_OF_WEEK)))
        calendar.firstDayOfWeek = selectedDateCalender.get(Calendar.MONDAY)
        calendar[Calendar.DAY_OF_WEEK] = selectedDateCalender.get(Calendar.MONDAY)
        var toDayAdded = false
        Log.e("week--->", "" + selectedDateCalender.get(Calendar.MONDAY))
        Log.e("day--->", "" + calendar[Calendar.DAY_OF_WEEK])
        Log.e(
            "--->",
            calendar.get(Calendar.DAY_OF_MONTH)
                .toString() + "," + selectedDateCalender.get(Calendar.DAY_OF_MONTH)
        )
        //  val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            //days[i] = format.format(calendar.time)
            if (calendar.get(Calendar.DAY_OF_MONTH) == selectedDateCalender.get(Calendar.DAY_OF_MONTH)) {
//                toDayAdded = true
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))

                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
                val day = DateFormat.format("dd", days[i].date) as String // 20
                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
                val year = DateFormat.format("yyyy", days[i].date) as String // 2013

                date = "$day-$monthNumber-$year"
                dateTosave =
                    "$year-$monthNumber-$day " + DateFormat.format("HH:mm:ss", calendar.time)
                        .toString()
                formatedAttendanceDate = "$year-$monthNumber-$day"
                Log.e("date", date)

                if (isSameDay(Calendar.getInstance(), selectedDateCalender))
                    toDayAdded = true
                if (isDatePickerClicked) {
                    isDatePickerClicked = false
                    showLoading()
                    callAPItoGetChildDailyReport()
                    callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                }


            } else {
                if (toDayAdded)
                    days.add(
                        CalendarModel(
                            calendar.time,
                            isEnabled = false,
                            isSelected = false
                        )
                    )
                else {
                    //days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
                    if (DateFormat.format("dd", calendar.time).toString()
                            .toInt() > DateFormat.format("dd", Calendar.getInstance().time)
                            .toString().toInt() && DateFormat.format("MM", calendar.time)
                            .toString()
                            .toInt() >= DateFormat.format("MM", Calendar.getInstance().time)
                            .toString().toInt()
                    ) {
                        days.add(
                            CalendarModel(
                                calendar.time,
                                isEnabled = false,
                                isSelected = false
                            )
                        )
                    } else if (DateFormat.format("yyyy", calendar.time).toString()
                            .toInt() > DateFormat.format("yyyy", Calendar.getInstance().time)
                            .toString().toInt()
                    ) {
                        days.add(
                            CalendarModel(
                                calendar.time,
                                isEnabled = false,
                                isSelected = false
                            )
                        )
                    } else {
                        days.add(
                            CalendarModel(
                                calendar.time,
                                isEnabled = true,
                                isSelected = false
                            )
                        )
                    }
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)

            Log.i("adsf", "" + days[i].date + "--->" + calendar.get(Calendar.DAY_OF_MONTH))
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter

    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        if (cal1 == null || cal2 == null) {
            throw IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }


//    private fun allDaysOfWeek() {
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
//                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
//                val day = DateFormat.format("dd", days[i].date) as String // 20
//                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
//                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
//                val year = DateFormat.format("yyyy", days[i].date) as String // 2013
//
//                date = "$day-$monthNumber-$year"
//                Log.e("date", date)
//            } else {
//                if (toDayAdded)
//                    days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
//                else
//                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
//            }
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1)
//            Log.i("adsf", "" + days[i])
//        }
//
//        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        binding.recDate.layoutManager = manager
//        daysAdapter = DateAdapter(this, days, this)
//        binding.recDate.adapter = daysAdapter
//    }

    override fun onITemClick(position: Int, item: CalendarModel) {
        if (item.isEnabled && !item.isSelected) {
            days.forEachIndexed { _, calendarModel ->
                calendarModel.isSelected = false
            }
            days[position].isSelected = !days[position].isSelected
            daysAdapter?.notifyDataSetChanged()
            val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
            val day = DateFormat.format("dd", item.date) as String // 20
            val monthString = DateFormat.format("MMM", item.date) as String // Jun
            val monthNumber = DateFormat.format("MM", item.date) as String // 06
            val year = DateFormat.format("yyyy", item.date) as String // 2013

            date = "$day-$monthNumber-$year"
            dateTosave = "$year-$monthNumber-$day " + DateFormat.format(
                "HH:mm:ss",
                Calendar.getInstance().time
            ).toString()
            formatedAttendanceDate = "$year-$monthNumber-$day"
            Log.e("date", date)
//
//        if (date != "" && selectedChild != null) {
            showLoading()
            callAPItoGetChildDailyReport()
            callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
        }
//    }
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
                .setDestinationInExternalFilesDir(
                    applicationContext,
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
//                .setDestinationInExternalPublicDir(
//                    Environment.DIRECTORY_DOWNLOADS,
//                    "/.Swaddle/$fileName"
//                )
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
        val result = ContextCompat.checkSelfPermission(this@DailyReportActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@DailyReportActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@DailyReportActivity,
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
                        this@DailyReportActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun callAPItoGetIncidenceReports() {
        var childId =
            if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
                ""
            } else {
                (selectedChild?.id ?: 0).toString()
            }

        var classId =
            if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
                (selectedClassRoom?.id ?: 0).toString()
            } else {
                ""
            }
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getIncidenceReport(
                "Bearer " + userManager.accessToken ?: "",
                childId,
                classId,
                date
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                reportsList = ArrayList()
                reportsList = response.body()?.data ?: ArrayList()
                var newReportedList =
                    reportsList.filter { it.incidentReports != null } as ArrayList
                setIngRecView(newReportedList)
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setIngRecView(data: ArrayList<ChildInfo>) {
        if (incidentAdapter == null) {
            val manager = LinearLayoutManager(
                this@DailyReportActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.recIncidentReport.layoutManager = manager
            incidentAdapter = ChildIncidentReportAdapter(this@DailyReportActivity, data)
            binding.recIncidentReport.adapter = incidentAdapter
        } else {
            incidentAdapter?.setItems(data)
            incidentAdapter?.notifyDataSetChanged()
        }

        if (data.size > 0) {
            binding.tvNoIncidentReportsFound.viewGone()
            binding.recIncidentReport.viewVisible()
        } else {
            binding.tvNoIncidentReportsFound.viewVisible()
            binding.recIncidentReport.viewGone()
        }
    }
    private fun getWeeklyChildAttendance(myDate: String) {
        val call: Call<WeeklyChildAttendanceModel> =
            RetrofitClass.getInstance().webRequestsInstance
                .getWeeklyChildAttendance(
                    "Bearer " + userManager.accessToken ?: "",
                    (selectedChild?.id ?: 0).toString(),
                    myDate
                )
        call.enqueue(object : Callback<WeeklyChildAttendanceModel> {
            override fun onResponse(
                call: Call<WeeklyChildAttendanceModel>,
                response: Response<WeeklyChildAttendanceModel>
            ) {

                try {
                    val dayList: ArrayList<WeeklyChildAttendanceData> = ArrayList()
                    val model = response.body()
                    days.clear()
                    dayList.addAll(model?.data!!)

                    var todayAdded = false

                    val today = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val selectedDate = dateFormat.parse(myDate)
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.time = selectedDate ?: Date()

                    Log.d("Calendar.DAY_OF_MONTH", "${today.get(Calendar.DAY_OF_MONTH)}")
                    Log.d(
                        "Selected.DAY_OF_MONTH",
                        "${selectedCalendar.get(Calendar.DAY_OF_MONTH)}"
                    )

                    for (i in dayList.indices) {
                        when {
                            selectedCalendar.get(Calendar.DAY_OF_MONTH) == dayList[i].day.toInt() -> {
                                dayList[i].isSelected = true

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

                                date = "$day-$monthNumber-$year"
                                dateTosave = "$year-$monthNumber-$day"
                                formatedAttendanceDate = "$year-$monthNumber-$day"

                                if (!todayAdded) {
                                    dayList[i].isEnabled = true
                                    todayAdded = isToday(dateFormat.parse(dayList[i].date))
                                } else {
                                    dayList[i].isEnabled = false
                                }
                                days.add(
                                    CalendarModel(
                                        dateFormat.parse(dayList[i].date),
                                        dayList[i].isEnabled,
                                        dayList[i].isSelected
                                    )
                                )
                            }
                            else -> {
                                dayList[i].isSelected = false

                                if (!todayAdded) {
                                    dayList[i].isEnabled = true
                                    todayAdded = isToday(dateFormat.parse(dayList[i].date))
                                } else {
                                    dayList[i].isEnabled = false
                                }
                                days.add(
                                    CalendarModel(
                                        dateFormat.parse(dayList[i].date),
                                        isEnabled = dayList[i].isEnabled,
                                        isSelected = dayList[i].isSelected
                                    )
                                )
                                Log.d(
                                    "isToda",
                                    "${isToday(dateFormat.parse(dayList[i].date))}"
                                )
                            }
                        }

                    }


                    binding.recDate.adapter?.notifyDataSetChanged()
//                    if (selectedChild != null)
//                        callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                call: Call<WeeklyChildAttendanceModel>,
                t: Throwable
            ) {
                showErrorToast(this@DailyReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }
}

