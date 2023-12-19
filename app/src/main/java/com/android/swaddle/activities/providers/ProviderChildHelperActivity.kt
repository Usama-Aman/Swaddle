package com.android.swaddle.activities.providers

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ChildProfileActivity
import com.android.swaddle.activities.providers.class_room_ui.CreateClassRoom
import com.android.swaddle.activities.providers.reports.ProviderAttendanceActivity
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.provider_adapter.ProviderHelpersListAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityProviderHelperBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.codingpixel.a2i.response.ApiCallBack
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProviderChildHelperActivity : BaseActivity(), ProviderHelpersListAdapter.ItemClickListener,
    ApiCallBack, DateAdapter.ClickListener {
    private lateinit var binding: ActivityProviderHelperBinding
    private var adapter: ProviderHelpersListAdapter? = null
    var classRoomsList = ArrayList<ClassroomDetails>()
    var childesList = ArrayList<ChildInfo>()
    var unfilteredChildList = ArrayList<ChildInfo>()

    val days = ArrayList<CalendarModel>()
    var selectedChild: ChildInfo? = null

    private var mCalendar: Calendar = Calendar.getInstance()
    var daysAdapter: DateAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    var selectedClassRoom: ClassroomDetails? = null
    var classroom_id: Int = -1
    var date = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderHelperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        getIntentData()
        callAPItoGetClassRooms()
        allDaysOfWeek()
        listener()
        selectRandomDate()
    }
    private fun getIntentData(){
        if(intent.hasExtra("classroom_id"))
            classroom_id = intent.getIntExtra("classroom_id",-1)
    }
    private fun allDaysOfWeek() {
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

                date = "$day-$monthNumber-$year"
                Log.e("date", date)
            } else {
                if (toDayAdded)
                    days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
                else
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])

            //callAPItoGetChildes(selectedClassRoom?.id, date)
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter

        if (selectedClassRoom != null)
            callAPItoGetChildes(selectedClassRoom?.id, date)
    }

    private fun selectRandomDate() {
        var date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvSelectDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)

                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate: String = df.format(mCalendar.time)


                val dayOfTheWeek = DateFormat.format("EEEE", mCalendar.time) as String // Thursday
                val day = DateFormat.format("dd", mCalendar.time) as String // 20
                val monthString = DateFormat.format("MMM", mCalendar.time) as String // Jun
                val monthNumber = DateFormat.format("MM", mCalendar.time) as String // 06
                val year = DateFormat.format("yyyy", mCalendar.time) as String // 2013

                date = "$day-$monthNumber-$year"

                callAPItoGetChildes(selectedClassRoom?.id, formattedDate)
//                callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)
//                callAPItoGetAuthorizedAdults((selectedChild?.id ?: 0))
//                callAPItoGetChildDailyReport()
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

    private fun callAPItoGetClassRooms() {

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
                showErrorToast(this@ProviderChildHelperActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter =
                ClassSpinnerAdapter(this@ProviderChildHelperActivity, classRoomsList)
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
                    callAPItoGetChildes(classRoomsList[position].id, date)
                    Log.e("SelectedRoom", classRoomsList[position].id.toString())
                    //   showSuccessToast(this@ProviderChildHelperActivity,"Click")
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
            //callAPItoGetChildes(classRoomsList.first().id, date)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerClassRoomNoData.viewVisible()
            selectedChild = null
        }

//        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val formattedDate: String = df.format(mCalendar.time)
//        callAPItoGetChildes(selectedClassRoom?.id, formattedDate)
    }

    private fun callAPItoGetChildes(id: Int?, formattedDate: String) {
        Log.e("formattedDate", formattedDate)
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesListOfRoomWithAttendance(
                "Bearer " + userManager.accessToken ?: "",
                (id ?: 0).toString(), "0", formattedDate
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                childesList = ArrayList()
                unfilteredChildList = ArrayList()
                Log.e("https response", response.body().toString())
                childesList = response.body()?.data ?: ArrayList()
                unfilteredChildList.addAll(childesList)

                filterChildStatus(binding.buttonGroup.position)
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(
                    this@ProviderChildHelperActivity,
                    "Can't Connect to Server!"
                )
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    override fun onResume() {
        super.onResume()
        callAPItoGetClassRooms()
    }


    private fun setRecyclerView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(this)
            binding.recViewClassList.layoutManager = manager
            adapter = ProviderHelpersListAdapter(this, childesList, this)
            binding.recViewClassList.adapter = adapter
        } else {
            adapter?.setItems(childesList)
            adapter?.notifyDataSetChanged()
        }

        if (childesList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }

    private fun listener() {
        binding.noData.retryBtn.setOnClickListener { callAPItoGetClassRooms() }

        binding.imgAdd.setOnClickListener {
            val intent = Intent(this, CreateClassRoom::class.java)
            intent.putExtra("type", "create")
            startActivity(intent)
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.tvSubmit.setOnClickListener {
            if (isNetworkConnected()) {
                startActivity(
                    Intent(
                        this@ProviderChildHelperActivity,
                        ProviderAttendanceActivity::class.java
                    )
                )
                finish()
            } else {
                CustomToasts.failureToast(
                    this@ProviderChildHelperActivity,
                    "No internet connection!"
                )
            }
        }

        binding.buttonGroup.setOnPositionChangedListener { position ->
            filterChildStatus(position)
        }
    }

    private fun filterChildStatus(position: Int) {
        Log.d("UnfileredListSize", unfilteredChildList.size.toString())
        childesList.clear()
        when (position) {
            0 -> {
                childesList.addAll(unfilteredChildList)
            }
            1 -> { // Signed in students

                unfilteredChildList.forEachIndexed { index, childInfo ->
                    if (childInfo.attendance?.childAttendanceMultiple?.isNotEmpty() == true && childInfo.attendance?.childAttendanceMultiple?.last()?.arrivalTime != null && childInfo.attendance?.childAttendanceMultiple?.last()?.departureTime == null)
                        childesList.add(childInfo)
                }
            }
            2 -> {
                unfilteredChildList.forEachIndexed { index, childInfo ->
                    if (childInfo.attendance?.childAttendanceMultiple?.isNotEmpty() == true && childInfo.attendance?.childAttendanceMultiple?.last()?.departureTime != null)
                        childesList.add(childInfo)
                }
            }
            3 -> {
                unfilteredChildList.forEachIndexed { index, childInfo ->
                    if (childInfo.attendance?.childAttendanceMultiple?.isNotEmpty() == true && childInfo.attendance?.childAttendanceMultiple?.last()?.arrivalTime == null)
                        childesList.add(childInfo)
                }
            }
        }
        Log.d("fileredListSize", childesList.size.toString())
        adapter = null
        setRecyclerView()
    }

    private fun init() {

    }

    override fun onResponse(call: Any?, response: Any, responseType: String) {
        hideLoading()
        callAPItoGetClassRooms()
    }

    override fun onFailed(call: Any?, t: Throwable, responseType: String) {
        showErrorToast(this@ProviderChildHelperActivity, "Try again")
        hideLoading()
    }

    override fun onSuccessFalse(call: Any?, response: Any, responseType: String) {
        showErrorToast(this@ProviderChildHelperActivity, "Try again")
        hideLoading()
    }

    override fun onITemClick(position: Int, item: CalendarModel) {
        if (item.isEnabled && !item.isSelected) {
            days.forEachIndexed { _, calendarModel ->
                calendarModel.isSelected = false
            }
            days[position].isSelected = !days[position].isSelected
            daysAdapter?.setItems(days)
            daysAdapter?.notifyDataSetChanged()
            val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
            val day = DateFormat.format("dd", item.date) as String // 20
            val monthString = DateFormat.format("MMM", item.date) as String // Jun
            val monthNumber = DateFormat.format("MM", item.date) as String // 06
            val year = DateFormat.format("yyyy", item.date) as String // 2013

            date = "$day-$monthNumber-$year"
            Log.e("date", date)

            if (date != "" && selectedClassRoom != null) {
                showLoading()
                callAPItoGetChildes(selectedClassRoom?.id, date)
            }
        }
    }

    override fun onItemClicked(position: Int, item: ChildInfo) {
        if (item.deletedAt == null) {
            startActivity<ChildProfileActivity>("item" to item)
        }
    }


}