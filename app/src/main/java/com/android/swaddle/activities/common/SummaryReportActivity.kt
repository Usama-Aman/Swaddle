package com.android.swaddle.activities.common

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivitySummaryReportBinding
import com.android.swaddle.fragments.common.AttendanceReportFragment
import com.android.swaddle.fragments.common.IncidentReportFragment
import com.android.swaddle.fragments.common.PaymentReportFragment
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class SummaryReportActivity : BaseActivity() {
    private lateinit var binding: ActivitySummaryReportBinding
    private val titles = arrayOf("Attendance Report", "Payment Report", "Incident Report")
//    var list = ArrayList<ChildInfo>()

    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var childesAdapter: SelectChildSpinnerAdapter? = null

    var childesList = ArrayList<ChildInfo>()
    var classRoomsList = ArrayList<ClassroomDetails>()

    var selectedChild: ChildInfo? = null
    var selectedClassRoom: ClassroomDetails? = null

    private var pagerAdapter: ViewPagerFragmentAdapter? = null

    private lateinit var incidentReportFragment: IncidentReportFragment
    private lateinit var attendanceReportFragment: AttendanceReportFragment
    private lateinit var paymentReportFragment: PaymentReportFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
            callAPItoGetClassRooms()
        } else {
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
            binding.tvChild.viewVisible()
            binding.cvSpinnerCard.viewVisible()
            callAPItoGetChildesOfParent()
        }
        listener()
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
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@SummaryReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
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
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@SummaryReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter =
                ClassSpinnerAdapter(this@SummaryReportActivity, classRoomsList)
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
                    //   showSuccessToast(this@DailyReportActivity,"Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()

            callAPItoGetChildes(classRoomsList.first().id)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            selectedChild = null
            selectedClassRoom = null
        }
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
                showSuccessToast(this@SummaryReportActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter =
                SelectChildSpinnerAdapter(this@SummaryReportActivity, childesList)
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
                    settingTabLayout()
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            selectedChild = childesList.first()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()

        }
        settingTabLayout()
    }

    private fun settingTabLayout() {
        if (pagerAdapter == null) {
            pagerAdapter = ViewPagerFragmentAdapter(this, selectedChild, selectedClassRoom)
            binding.viewPager.adapter = pagerAdapter
            TabLayoutMediator(
                binding.tabLayout, binding.viewPager
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }.attach()
        } else {
            pagerAdapter?.setUpdatedObjects(selectedChild, selectedClassRoom)
            pagerAdapter?.notifyDataSetChanged()
        }
    }

    private fun listener() {
        binding.imgBack.setOnClickListener {
            finish()
        }

    }

    private fun init() {

    }

    inner class ViewPagerFragmentAdapter(
        fragmentActivity: FragmentActivity,
        var selectedChild: ChildInfo?,
        var selectedClassRoom: ClassroomDetails?
    ) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> {
                    attendanceReportFragment = AttendanceReportFragment.newInstance(
                        selectedChild,
                        selectedClassRoom
                    )
                    return attendanceReportFragment
                }
                1 -> {
                    paymentReportFragment =
                        PaymentReportFragment.newInstance(selectedChild, selectedClassRoom)
                    return paymentReportFragment
                }
                2 -> {
                    incidentReportFragment =
                        IncidentReportFragment.newInstance(selectedChild, selectedClassRoom)
                    return incidentReportFragment
                }

            }
            return AttendanceReportFragment()
        }

        fun setUpdatedObjects(selectedChild: ChildInfo?, selectedClassRoom: ClassroomDetails?) {
            this.selectedChild = selectedChild
            this.selectedClassRoom = selectedClassRoom
            if (::attendanceReportFragment.isInitialized)
                attendanceReportFragment.updateObjects(selectedChild, selectedClassRoom)
            if (::paymentReportFragment.isInitialized)
                paymentReportFragment.updateObjects(selectedChild, selectedClassRoom)
            if (::incidentReportFragment.isInitialized)
                incidentReportFragment.updateObjects(selectedChild, selectedClassRoom)
            pagerAdapter?.notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return 3
        }

    }

}