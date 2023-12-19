package com.android.swaddle.activities.providers.reports

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.adapters.child_adpaters.ChildIncidentReportAdapter
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.provider_adapter.IncidentReportAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityChildIncidentReportBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddIncidentReportBottomSheet
import com.android.swaddle.fragments.bottomSheetFragment.ClickListener
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ChildIncidentReportActivity : BaseActivity(), DateAdapter.ClickListener {
    var classRoomsList = ArrayList<ClassroomDetails>()

    var list = ArrayList<ChildInfo>()
    var reportsList = ArrayList<ChildInfo>()
    val days = ArrayList<CalendarModel>()

    var attendanceDate = ""
    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null

    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    var childesAdapter: SelectChildSpinnerAdapter? = null
    var daysAdapter: DateAdapter? = null
    var classroom_id: Int = -1
    var child_id: Int = -1
    private lateinit var binding: ActivityChildIncidentReportBinding
    private var adapter: ChildIncidentReportAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildIncidentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setChildIncRepRecView()
        getIntentData()
        init()
        binding.tvChild.viewVisible()
        binding.cvSpinnerCard.viewVisible()
        callAPItoGetChildesOfParent()

        listener()
        allDaysOfWeek()
    }
    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
        child_id = intent.getIntExtra("child_id",-1)
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
                reportsList = ArrayList()
                reportsList = response.body()?.data ?: ArrayList()
                var newReportedList = reportsList.filter { it.incidentReports != null } as ArrayList
                setIngRecView(newReportedList)
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildIncidentReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setIngRecView(data: ArrayList<ChildInfo>) {
        if (adapter == null) {
            val manager = LinearLayoutManager(
                this@ChildIncidentReportActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.rec.layoutManager = manager
            adapter = ChildIncidentReportAdapter(this@ChildIncidentReportActivity, data)
            binding.rec.adapter = adapter
        } else {
            adapter?.setItems(data)
            adapter?.notifyDataSetChanged()
        }

        if (data.size > 0) {
            binding.tvNoDataFound.viewGone()
            binding.rec.viewVisible()
        } else {
            binding.tvNoDataFound.viewVisible()
            binding.rec.viewGone()
        }
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    private fun init() {

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
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter


        callAPItoGetIncidenceReports()
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@ChildIncidentReportActivity, list)
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
                showSuccessToast(this@ChildIncidentReportActivity, "Can't Connect to Server!")
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
            Log.e("date", attendanceDate)

//        if (attendanceDate != "" && selectedClassRoom != null) {
            showLoading()
            callAPItoGetIncidenceReports()
//        }
        }
    }
}