package com.android.swaddle.fragments.common

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.child_adpaters.ChildIncidentReportAdapter
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.FragmentIncidentReportBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ChildesResponse
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.DateConverter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IncidentReportFragment : BaseFragment() {

    private var mCalendar: Calendar = Calendar.getInstance()
    private var mCalendar2: Calendar = Calendar.getInstance()
    private var picker: DatePickerDialog? = null

    var adapter: ChildIncidentReportAdapter? = null
    var list = ArrayList<ChildInfo>()


    var startDate = ""
    var endDate = ""
    var duration = "week"

    private lateinit var binding: FragmentIncidentReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            childInfo = it.getSerializable(ARG_PARAM1) as ChildInfo?
            classroomDetails = it.getSerializable(ARG_PARAM2) as ClassroomDetails?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIncidentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun callAPItoGetIncidenceReport(
    ) {

        var childId =
            if (mActivity.userManager.user?.type == "provider" || mActivity.userManager.user?.type == "staff") {
                //""
                (childInfo?.id ?: 0).toString()
            } else {
                (childInfo?.id ?: 0).toString()
            }

        var classId =
            if (mActivity.userManager.user?.type == "provider" || mActivity.userManager.user?.type == "staff") {
                //(classroomDetails?.id ?: 0).toString()
                ""
            } else {
                ""
            }

        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getAllIncidenceReport(
                "Bearer " + mActivity.userManager.accessToken,
                childId,
                classId, duration,
                startDate, endDate
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                list.clear()
                mActivity.hideLoading()
                if (response.body()?.data != null)
                    list = response.body()?.data!!
                setRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
//                showErrorToast(mActivity, "Can't Connect to Server!")
                setRecyclerView()
                mActivity.hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun datePickerDialog(type: String) {
        val day: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val year: Int = mCalendar.get(Calendar.YEAR)
        picker = DatePickerDialog(
            mActivity, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvDate1.text = DateConverter.formatePickerDateNew2(mCalendar.time)
                startDate = DateConverter.formatePickerDateNEw(mCalendar.time)
                resetColor()
                duration = ""

                /* if (type == "a") {
                     binding.tvDate1.text = DateConverter.formatePickerDate(mCalendar.time)
                     startDate = DateConverter.formatePickerDateNEw(mCalendar.time)
                 } else {
                     binding.tvDate2.text = DateConverter.formatePickerDate(mCalendar.time)
                     endDate = DateConverter.formatePickerDateNEw(mCalendar.time)
                 }*/
                callAPItoGetIncidenceReport()
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }, year, month, day
        )
        picker?.datePicker?.maxDate = mCalendar2.timeInMillis
        picker?.show()
    }

    private fun datePickerDialog2() {
        val day: Int = mCalendar2.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar2.get(Calendar.MONTH)
        val year: Int = mCalendar2.get(Calendar.YEAR)
        picker = DatePickerDialog(
            mActivity, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->

                mCalendar2.set(Calendar.YEAR, year)
                mCalendar2.set(Calendar.MONTH, monthOfYear)
                mCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                binding.tvDate2.text = DateConverter.formatePickerDateNew2(mCalendar2.time)
                endDate = DateConverter.formatePickerDateNEw(mCalendar2.time)

                resetColor()
                duration = ""

                callAPItoGetIncidenceReport()


            }, year, month, day
        )

        picker?.datePicker?.minDate = mCalendar.timeInMillis
        picker?.show()
    }

    private fun resetColor() {
        binding.tvWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
        binding.tvMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
        binding.tvYear.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        binding.tvDate1.text = DateConverter.formatePickerDateNew2(mCalendar.time)
        startDate = DateConverter.formatePickerDateNEw(mCalendar.time)

        mCalendar2.set(Calendar.DAY_OF_WEEK, 7)
        mCalendar2.add(Calendar.DATE, 1)
        binding.tvDate2.text = DateConverter.formatePickerDateNew2(mCalendar2.time)
        endDate = DateConverter.formatePickerDateNEw(mCalendar2.time)


        listener()
        callAPItoGetIncidenceReport()


//        callAPItoGetAttendanceReport()
    }

    private fun listener() {
        binding.lnrDateStart.setOnClickListener {
            datePickerDialog("a")
        }
        binding.lnrDateEnd.setOnClickListener {
            datePickerDialog2()
        }

        binding.tvWeek.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()
//            val currentDayOfWeek: Int = (mCalendar.get(Calendar.DAY_OF_WEEK) + 7 - mCalendar.getFirstDayOfWeek()) % 7
//            mCalendar.add(Calendar.DAY_OF_YEAR, -currentDayOfWeek)
//            mCalendar2.set(Calendar.DAY_OF_WEEK, 7)
            mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            mCalendar2.set(Calendar.DAY_OF_WEEK, 7)
            mCalendar2.add(Calendar.DATE, 1)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            startDate = ""
            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            endDate = ""

            changeColor(binding.tvWeek)
            duration = "week"
            mActivity.showLoading()
            callAPItoGetIncidenceReport()
        }

        binding.tvMonth.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()
            mCalendar.set(Calendar.DAY_OF_MONTH, 1)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            startDate = ""
            mCalendar2.add(Calendar.MONTH, 1)
            mCalendar2.set(Calendar.DAY_OF_MONTH, 1)
            mCalendar2.add(Calendar.DATE, -1)
            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            endDate = ""

            changeColor(binding.tvMonth)
            duration = "month"
            mActivity.showLoading()
            callAPItoGetIncidenceReport()
        }

        binding.tvYear.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()

            mCalendar.set(Calendar.MONTH, Calendar.JANUARY)
            mCalendar.set(Calendar.DATE, 1)


//            //mCalendar.set(Calendar.DAY_OF_YEAR, 1)
//            mCalendar.add(Calendar.YEAR, 0)
//            mCalendar.set(Calendar.MONTH, Calendar.JANUARY)
//            mCalendar.add(Calendar.DATE, 0)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            startDate = ""
//            mCalendar2.add(Calendar.YEAR, 0)
//            mCalendar2.set(Calendar.DAY_OF_YEAR, 1)
//            mCalendar2.add(Calendar.DATE, -1)
//            mCalendar2.add(Calendar.YEAR, 0)
//            mCalendar2.set(Calendar.MONTH, Calendar.DECEMBER)
//            mCalendar2.add(Calendar.DATE, 30)

            mCalendar2.set(Calendar.MONTH, Calendar.DECEMBER)
            mCalendar2.set(Calendar.DAY_OF_MONTH, 31)
            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            endDate = ""

            changeColor(binding.tvYear)
            duration = "year"
            mActivity.showLoading()
            callAPItoGetIncidenceReport()
        }
    }

    private fun setRecyclerView() {

        val manager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        adapter = ChildIncidentReportAdapter(mActivity, list)
        binding.rvReports.layoutManager = manager
        binding.rvReports.adapter = adapter
        adapter?.setItems(list)

        if (list.size > 0) {
            binding.tvNoDocuments.viewGone()
        } else {
            binding.tvNoDocuments.viewVisible()
        }
    }

    private fun changeColor(tv: TextView) {
        binding.tvWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
        binding.tvMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
        binding.tvYear.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGhost))
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colroBlue))
    }

    private var childInfo: ChildInfo? = null
    private var classroomDetails: ClassroomDetails? = null

    companion object {

        @JvmStatic
        fun newInstance(param1: ChildInfo?, param2: ClassroomDetails?) =
            IncidentReportFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putSerializable(ARG_PARAM2, param2)
                }
            }
    }

    fun updateObjects(selectedChild: ChildInfo?, selectedClassRoom: ClassroomDetails?) {
        this.childInfo = selectedChild
        this.classroomDetails = selectedClassRoom
        callAPItoGetIncidenceReport()
    }

}