package com.android.swaddle.activities.providers.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.provider_adapter.IncidentReportAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityIncidentReportBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddIncidentReportBottomSheet
import com.android.swaddle.fragments.bottomSheetFragment.ClickListener
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*

import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.DateConverter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class IncidentReportActivity : BaseActivity(), DateAdapter.ClickListener,
    IncidentReportAdapter.ClickListener {
    var classRoomsList = ArrayList<ClassroomDetails>()

    //    var incidentsReportsList = ArrayList<ChildInfo>()
    var list = ArrayList<ChildInfo>()
    val days = ArrayList<CalendarModel>()

    var attendanceDate = ""
    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null
    var dateTosave = ""
    var formatedAttendanceDate = ""

    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    var childesAdapter: SelectChildSpinnerAdapter? = null
    var adapter: IncidentReportAdapter? = null
    var daysAdapter: DateAdapter? = null
    var isDatePickerClicked = false
    var classroom_id: Int = -1
    var child_id: Int = -1
    private lateinit var binding: ActivityIncidentReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
            callAPItoGetClassRooms()
            binding.tvChild.viewGone()
            binding.tvClass.viewVisible()
            binding.cvSpinnerCard.viewGone()
            binding.cvSpinnerClass.viewVisible()
        } else {
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
            binding.tvChild.viewVisible()
            binding.cvSpinnerCard.viewVisible()
            callAPItoGetChildesOfParent()
        }
        listener()
        getWeeklyChildAttendance(formatedAttendanceDate)
        //allDaysOfWeek(Calendar.getInstance())
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
                showErrorToast(this@IncidentReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        callAPItoGetIncidenceReports()
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
                showErrorToast(this@IncidentReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setIngRecView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(
                this@IncidentReportActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.rec.layoutManager = manager
            adapter = IncidentReportAdapter(this@IncidentReportActivity, list, this, attendanceDate)
            binding.rec.adapter = adapter
        } else {
            adapter?.setItems(list, attendanceDate)
            adapter?.notifyDataSetChanged()
        }

        if (list.size > 0) {
            binding.tvNoDataFound.viewGone()
        } else {
            binding.tvNoDataFound.viewVisible()
        }
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    private fun init() {
        formatedAttendanceDate = DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time) as String
        attendanceDate = DateFormat.format("dd-MM-yyyy", Calendar.getInstance().time) as String
        dateTosave = (DateFormat.format("yyyy-MM-dd", Calendar.getInstance().time) as String) + DateFormat.format("HH:mm:ss", Calendar.getInstance().time).toString()
        val manager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter
        selectRandomDate()
    }

    private fun selectRandomDate() {
        val mCalendar: Calendar = Calendar.getInstance()
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvSelectDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)
                isDatePickerClicked = true
                days.clear()
                attendanceDate = "$dayOfMonth-${monthOfYear+1}-$year"
                dateTosave = "$year-${monthOfYear+1}-$dayOfMonth " + DateFormat.format("HH:mm:ss", Calendar.getInstance().time).toString()
                formatedAttendanceDate = "$year-${monthOfYear+1}-$dayOfMonth"
                callAPItoGetIncidenceReports()
                getWeeklyChildAttendance(formatedAttendanceDate)
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

        //calendar.add(Calendar.DAY_OF_MONTH,Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK));

        calendar.firstDayOfWeek = selectedDateCalender.get(Calendar.MONDAY)
        calendar[Calendar.DAY_OF_WEEK] = selectedDateCalender.get(Calendar.MONDAY)

        var toDayAdded = false

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

                attendanceDate = "$day-$monthNumber-$year"
                dateTosave = "$year-$monthNumber-$day " + DateFormat.format("HH:mm:ss",calendar.time).toString()
                formatedAttendanceDate = "$year-$monthNumber-$day"
                //dateTosave = "$year-$monthNumber-$day "
                Log.e("date", attendanceDate)

                if (isSameDay(Calendar.getInstance(), selectedDateCalender))
                    toDayAdded = true
                if(isDatePickerClicked) {
                    isDatePickerClicked = false
                    showLoading()

                }

            } else {
                if (toDayAdded)
                    days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
                else {
                    if(DateFormat.format("dd", calendar.time).toString().toInt() > DateFormat.format("dd", Calendar.getInstance().time).toString().toInt()
                        && DateFormat.format("MM", calendar.time).toString()
                            .toInt() >= DateFormat.format("MM", Calendar.getInstance().time)
                            .toString().toInt()){
                        days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
                    }
                    else if(DateFormat.format("yyyy", calendar.time).toString()
                            .toInt() > DateFormat.format("yyyy", Calendar.getInstance().time)
                            .toString().toInt()) {
                        days.add(
                            CalendarModel(
                                calendar.time,
                                isEnabled = false,
                                isSelected = false
                            )
                        )
                    }
                    else
                        days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter


        callAPItoGetIncidenceReports()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        if (cal1 == null || cal2 == null) {
            throw IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }


    private fun setChildSpinner() {
        childesAdapter = null
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@IncidentReportActivity, list)
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(list)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (list.size > 0) {
                    selectedChild = list[position]
                    callAPItoGetIncidenceReports()
                }

                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (list.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            if(selectedChild?.id == -1)
                selectedChild = list.first()
            if(child_id != -1) {
                list.forEachIndexed { index, item ->
                    if (item.id == child_id) {
                        binding.spChild.setSelection(index)
                        selectedChild = item
                    }
                }
                child_id = -1
            }
            //callAPItoGetIncidenceReports()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
        }
    }

    private fun setClassSpinner() {
        classRoomsAdapter = null
        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@IncidentReportActivity, classRoomsList)
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
                callAPItoGetIncidenceReports()
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
            //callAPItoGetIncidenceReports()

        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
        }
    }

    private fun callAPItoGetChildesOfParent() {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                list = ArrayList()
                list = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@IncidentReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

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

            attendanceDate = "$day-$monthNumber-$year"
            dateTosave = "$year-$monthNumber-$day " + DateFormat.format("HH:mm:ss", Calendar.getInstance().time).toString()
            formatedAttendanceDate = "$year-$monthNumber-$day"
            Log.e("date", attendanceDate)

//        if (attendanceDate != "" && selectedClassRoom != null) {
            showLoading()
            callAPItoGetIncidenceReports()
//        }
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
                this@IncidentReportActivity,
                item,
                selectedClassRoom ?: ClassroomDetails(),
                dateTosave,
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
    private fun getWeeklyChildAttendance(myDate: String) {
        val call: Call<WeeklyChildAttendanceModel> =
            RetrofitClass.getInstance().webRequestsInstance
                .getWeeklyChildAttendance(
                    "Bearer " + userManager.accessToken ?: "",
                    "0",
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

                                attendanceDate = "$day-$monthNumber-$year"
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

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                call: Call<WeeklyChildAttendanceModel>,
                t: Throwable
            ) {
                showErrorToast(this@IncidentReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }
}