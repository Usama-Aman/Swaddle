package com.android.swaddle.activities.parent

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DownloadManager
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
import com.android.swaddle.activities.providers.reports.AddReportActivity
import com.android.swaddle.adapters.provider_adapter.DailyReportAdapter
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.spinneradapter.AuthorizedAdultsSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityParentAttandanceBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

// NOT USED   got to -> DailyReportActivity & ProviderAttendanceActivity
class ParentAttendanceActivity : BaseActivity(), DateAdapter.ClickListener {

    private lateinit var binding: ActivityParentAttandanceBinding
    private var mCalendar: Calendar = Calendar.getInstance()
    private var picker: DatePickerDialog? = null
    var arrivalTime = ""
    var departureTime = ""
    var attendanceDate = ""

    var childesList = ArrayList<ChildInfo>()
    var reportsList = ArrayList<DailyReport>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var authorizedAdultsList = ArrayList<User>()
    var reportDocumentsList = ArrayList<DailyReport>()

    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    var selectedDropOff: User? = null
    var selectedPickUp: User? = null

    var days = ArrayList<CalendarModel>()

    private var daysAdapter: DateAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var authorizedAdultsSpinnerAdapter: AuthorizedAdultsSpinnerAdapter? = null

    private var childesAdapter: SelectChildSpinnerAdapter? = null
    private var dailyReportAdapter: DailyReportAdapter? = null

    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1

    var downloadedPosition = 0
    var selectedPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentAttandanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        listeners()
        callAPItoGetChildes()
    }

    private fun callAPItoGetChildDailyReport() {
        val call: Call<DailyReportResponse> = RetrofitClass.getInstance().webRequestsInstance
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
                hideLoading()

            }

            override fun onFailure(
                call: Call<DailyReportResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
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
                    this@ParentAttendanceActivity,
                    reportDocumentsList,
                    object :
                        DailyReportAdapter.ItemClickListener {
                        override fun onItemClick(position: Int) {

                        }

                        override fun onDownloadClick(position: Int) {
                            permissionAccess(position)
                            downloadedPosition = position
                        }

                        override fun onOpenDraftClick(position: Int) {
                            if (selectedChild != null) {
                                startActivity<AddReportActivity>(
                                    "item" to selectedChild,
                                    "draft" to reportDocumentsList[position]
                                )
                            } else {
                                CustomToasts.failureToast(
                                    this@ParentAttendanceActivity,
                                    "No Child Selected!"
                                )
                            }
                        }

                        override fun onDeleteClick(position: Int) {

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

    private fun callAPItoGetAuthorizedAdults(childId: Int) {
        showLoading()
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
                setAuthorizedAdultsSpinner()
            }

            override fun onFailure(
                call: Call<AuthorizedAdultsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
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
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()

            }

            override fun onFailure(
                call: Call<GetAttendanceResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
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
                showErrorToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
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
                    showSuccessToast(this@ParentAttendanceActivity, response.body()?.message ?: "")
                    finish()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ParentAttendanceActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ParentAttendanceActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun validateTimeSelection(tvOpeningTime: TextView): Boolean {
        if (tvOpeningTime.text.toString().trim().isEmpty()) {
            CustomToasts.failureToast(
                this@ParentAttendanceActivity,
                "Please select opening time first!"
            )
            return false
        }
        return true
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvArrival.setOnClickListener {
            /*  showTimerClock(selectedTime = binding.tvArrival.text.trim().toString()){
                  binding.tvArrival.text = it
                  binding.tvDeparture.text = ""
              }*/
            showTimerClock { sTime, oTime ->
                binding.tvArrival.text = sTime
                arrivalTime = oTime
            }
        }

        binding.tvDeparture.setOnClickListener {
            /*  if (validateTimeSelection(binding.tvArrival))
                  showTimerClock(
                      setMinTime = binding.tvArrival.text.toString(),
                      selectedTime = binding.tvDeparture.text.trim().toString()
                  ) {
                      binding.tvDeparture.text = it
                  }*/
            showTimerClock { sTime, oTime ->
                binding.tvDeparture.text = sTime
                departureTime = oTime
            }
        }

        binding.tvSubmit.setOnClickListener {
            if (validateAttendance()) {
                callADIToSubmitAttendance(selectedChild ?: ChildInfo())
            }
        }
    }

    private fun init() {

    }

    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@ParentAttendanceActivity, classRoomsList)
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
                callAPItoGetChildes()
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()

            callAPItoGetChildes()
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.spDropOff.viewGone()
            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.spPickedBy.viewGone()
        }
    }

    private fun setAuthorizedAdultsSpinner() {
        if (authorizedAdultsSpinnerAdapter == null) {
            authorizedAdultsSpinnerAdapter =
                AuthorizedAdultsSpinnerAdapter(this@ParentAttendanceActivity, authorizedAdultsList)
            binding.spDropOff.adapter = authorizedAdultsSpinnerAdapter
            binding.spPickedBy.adapter = authorizedAdultsSpinnerAdapter
        } else {
            authorizedAdultsSpinnerAdapter?.setItems(authorizedAdultsList)
            authorizedAdultsSpinnerAdapter?.notifyDataSetChanged()
        }
        binding.spDropOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDropOff = authorizedAdultsList[position]
                Log.e("selectedDropOff", authorizedAdultsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.spPickedBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPickUp = authorizedAdultsList[position]
                Log.e("selectedPickUp", authorizedAdultsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (authorizedAdultsList.size > 0) {
            binding.tvSpinnerDropOffNoData.viewGone()
            binding.spDropOff.viewVisible()

            binding.tvSpinnerPickedByNoData.viewGone()
            binding.spPickedBy.viewVisible()
        } else {
            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.spDropOff.viewGone()

            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.spPickedBy.viewGone()
        }
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@ParentAttendanceActivity, childesList)
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
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            selectedChild = childesList.first()
            callAPItoGetAuthorizedAdults((selectedChild?.id ?: 0))
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()

            binding.tvSpinnerDropOffNoData.viewVisible()
            binding.spDropOff.viewGone()
            binding.tvSpinnerPickedByNoData.viewVisible()
            binding.spPickedBy.viewGone()
        }

        allDaysOfWeek()
    }

    private fun setDropOffSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@ParentAttendanceActivity, childesList)
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
            childesAdapter = SelectChildSpinnerAdapter(this@ParentAttendanceActivity, childesList)
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

    private fun allDaysOfWeek() {


        days = ArrayList()

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        var toDayAdded = false
        //  val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            //days[i] = format.format(calendar.time)
            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                toDayAdded = true
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))

                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
                val day = DateFormat.format("dd", days[i].date) as String // 20
                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
                val year = DateFormat.format("yyyy", days[i].date) as String // 2013

                attendanceDate = "$day-$monthNumber-$year"
                Log.e("date", attendanceDate)
            } else {
                if (toDayAdded)
                    days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
                else
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])

            callAPItoGetChildDailyReport()
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter

        if (selectedChild != null)
            callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
    }

    @SuppressLint("SetTextI18n")
    private fun showTimerClock(
        /* setMinTime: String = "",
         selectedTime: String,*/
        callback: (String, String) -> Unit
    ) {
        // var miniTimeCalendar = Calendar.getInstance()
        /*   val calendar = Calendar.getInstance()
           var miniTimeCalendar = Calendar.getInstance()

           if (setMinTime != "") {
               miniTimeCalendar.time = DateConverter.convertTimeForTimePicker(setMinTime)
           }
           if (selectedTime != "") {
               calendar.time = DateConverter.convertTimeForTimePicker(selectedTime)
           }*/

        val dpd = TimePickerDialog.newInstance(
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

                val selectedTime2 = "$hourr:$mm_precede$minute$AM_PM"
                callback(selectedTime2, time)
            },
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            mCalendar.get(Calendar.SECOND),
            false
        )
        /* if (setMinTime != "") {
             dpd.setMinTime(
                 miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
                 miniTimeCalendar.get(Calendar.MINUTE),
                 miniTimeCalendar.get(Calendar.SECOND)
             )
         }*/
//        dpd.setMinTime(
//            miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
//            miniTimeCalendar.get(Calendar.MINUTE),
//            miniTimeCalendar.get(Calendar.SECOND)
//        )
        dpd.version = TimePickerDialog.Version.VERSION_1
        //dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "Datepickerdialog")
    }

    private fun callAPItoGetChildes() {
        showLoading()
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
                showSuccessToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetParentChildes(id: Int?) {
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
                showSuccessToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

//    private fun callAPItoGetAttendanceDetails(id: Int?) {
//        showLoading()
//        val call: Call<DailyReportResponse> = RetrofitClass.getInstance().webRequestsInstance
//            .getAttendanceReport(
//                "Bearer " + userManager.accessToken ?: "",
//                (id ?: 0).toString(),
//                "",
//                ""
//            )
//        call.enqueue(object : Callback<DailyReportResponse> {
//            override fun onResponse(
//                call: Call<DailyReportResponse>,
//                response: Response<DailyReportResponse>
//            ) {
//                hideLoading()
//                reportsList = ArrayList()
////                reportsList = response.body()?.data ?: ArrayList()
//                setChildSpinner()
//
//                setReportRecyclerView()
//            }
//
//            override fun onFailure(
//                call: Call<DailyReportResponse>,
//                t: Throwable
//            ) {
//                showSuccessToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
//                hideLoading()
//                hideLoading()
//                t.printStackTrace()
//            }
//        })
//    }


    private fun callAPItoMarkAttendance(id: Int?) {
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
                showSuccessToast(this@ParentAttendanceActivity, "Can't Connect to Server!")
                hideLoading()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun validateAttendance(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@ParentAttendanceActivity, "No internet connection!")
            return false
        }

        if (selectedClassRoom == null) {
            showErrorToast(this@ParentAttendanceActivity, "Please Select Classroom!")
            return false
        }

        if (selectedChild == null) {
            showErrorToast(this@ParentAttendanceActivity, "Please Select Child for Attendance!")
            return false
        }

        if (attendanceDate == "") {
            showErrorToast(this@ParentAttendanceActivity, "Please Select Attendance Date!")
            return false
        }

        if (arrivalTime == "" && departureTime == "") {
            showErrorToast(this@ParentAttendanceActivity, "Please select drop off or pick time!")
            return false
        }

        if (arrivalTime == "" && departureTime != "") {
            showErrorToast(
                this@ParentAttendanceActivity,
                "Please mark drop off attendance before pick attendance!"
            )
            return false
        }
//        if (selectedDropOff != null && arrivalTime == "") {
//            showErrorToast(this@AttendanceActivity, "Please select drop off time!")
//            return false
//        }

//        if (selectedDropOff == null && arrivalTime != "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(
//                this@AttendanceActivity,
//                "Please enter name of person which dropped child!"
//            )
//            return false
//        }
//
//        if (selectedPickUp != null && departureTime == "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(this@AttendanceActivity, "Please select pickup time!")
//            return false
//        }
//
//        if (selectedPickUp == null && departureTime != "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(
//                this@AttendanceActivity,
//                "Please enter name of person which is picking up the child!"
//            )
//            return false
//        }


//        if (binding.etDropOffBy.text.toString().trim().isNotEmpty() && arrivalTime == "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(this@AttendanceActivity, "Please select drop off time!")
//            return false
//        }
//
//        if (binding.etDropOffBy.text.toString().trim().isEmpty() && arrivalTime != "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(
//                this@AttendanceActivity,
//                "Please enter name of person which dropped child!"
//            )
//            return false
//        }
//
//        if (binding.etPickedBy.text.toString().trim().isNotEmpty() && leavingTime == "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(this@AttendanceActivity, "Please select pickup time!")
//            return false
//        }
//
//        if (binding.etPickedBy.text.toString().trim().isEmpty() && leavingTime != "") {
////            binding.etPassword.error = "Please enter password"
//            showErrorToast(
//                this@AttendanceActivity,
//                "Please enter name of person which is picking up the child!"
//            )
//            return false
//        }

        return true
    }

    override fun onITemClick(position: Int, item: CalendarModel) {


        if (item.isEnabled && !item.isSelected) {
//            days.forEachIndexed { _, calendarModel ->
//                calendarModel.isSelected = false
//            }
//            days[position].isSelected = !days[position].isSelected
//            daysAdapter?.notifyDataSetChanged()
            val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
            val day = DateFormat.format("dd", item.date) as String // 20
            val monthString = DateFormat.format("MMM", item.date) as String // Jun
            val monthNumber = DateFormat.format("MM", item.date) as String // 06
            val year = DateFormat.format("yyyy", item.date) as String // 2013

            attendanceDate = "$day-$monthNumber-$year"

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
                "filePath", Constants.IMG_BASE_PATH + reportDocumentsList[position].filePath ?: ""
            )
            val fileName = (reportDocumentsList[position].filePath ?: "").split("/").last()

            val request = DownloadManager.Request(
                Uri.parse(Constants.IMG_BASE_PATH + (reportDocumentsList[position].filePath ?: ""))
            )
            request.setTitle("Swaddle-$fileName").setDescription("Downloading is in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
                .setMimeType("*/.pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this@ParentAttendanceActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@ParentAttendanceActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ParentAttendanceActivity,
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
                        this@ParentAttendanceActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}