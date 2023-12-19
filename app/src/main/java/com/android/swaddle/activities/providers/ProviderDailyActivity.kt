package com.android.swaddle.activities.providers

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityProviderDailyBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ProviderDailyActivity : BaseActivity(), DateAdapter.ClickListener {

    private lateinit var binding: ActivityProviderDailyBinding
    var list = ArrayList<ChildInfo>()
    var item: ChildInfo? = null
    var classRoomsList = ArrayList<ClassroomDetails>()

    val days = ArrayList<CalendarModel>()
    var daysAdapter: DateAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null

    var selectedClassRoom: ClassroomDetails? = null
    var date = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        callAPItoGetClassRooms()
    }

    private fun init() {
        binding.ivAdd.setOnClickListener {
            startActivity<AddActivity>()
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
                if (response.body()?.status == true) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                    hideLoading()
                    classRoomsList = response.body()?.data ?: ArrayList()
                    setClassSpinner()
                } else {
                    hideLoading()
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ProviderDailyActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@ProviderDailyActivity, classRoomsList)
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
                allDaysOfWeek()
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            allDaysOfWeek()
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
        }
    }


    private fun allDaysOfWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

        //  val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            //days[i] = format.format(calendar.time)
            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))

                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
                val day = DateFormat.format("dd", days[i].date) as String // 20
                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
                val year = DateFormat.format("yyyy", days[i].date) as String // 2013

                date = "$day-$monthNumber-$year"
                Log.e("date", date)
            } else {
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter


//        callAPItoGetChildDailyReport()
//        callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)

    }

    override fun onITemClick(position: Int, item: CalendarModel) {
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
        Log.e("date", date)

        showLoading()
//        callAPItoGetChildDailyReport()
//        callAPItoGetCurrentDayAttendance(selectedChild?.id ?: 0)

    }


}