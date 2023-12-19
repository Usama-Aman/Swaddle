package com.android.swaddle.activities.providers


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.provider_adapter.StaffListAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityStaffBinding
import com.android.swaddle.databinding.BottomSheetStaffBinding
import com.android.swaddle.databinding.DialogStaffInvitatoinBinding
import com.android.swaddle.fragments.bottomSheetFragment.ManageStaffBottomSheet
import com.android.swaddle.fragments.dialog.StaffDismissalPopup
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class StaffActivity : BaseActivity(),
    DateAdapter.ClickListener {

    var attendanceDate = ""
    var signIn = 0
    var signOut = 0
    var absent = 0
    var others = 0

    private lateinit var binding: ActivityStaffBinding
    val days = ArrayList<CalendarModel>()
    var daysAdapter: DateAdapter? = null
    var staffsList = ArrayList<User>()
    var adapter: StaffListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listeners()
        allDaysOfWeek()

        if (userManager.user?.type == Constants.parent || userManager.user?.type == Constants.authorized_adult || userManager.user?.type == Constants.staff) {
            binding.ivAdd.viewGone()
        } else {
            binding.ivAdd.viewVisible()
        }
    }

    private fun listeners() {
        binding.ivAdd.setOnClickListener {
            createInviteDialog()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun callAPItoGetStaffsData(attendanceData: String) { //20-05-2021 dd-mm-yyyy
        showLoading()
        val call: Call<GetStaffResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getStaffsDetails("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<GetStaffResponse> {
            override fun onResponse(
                call: Call<GetStaffResponse>,
                response: Response<GetStaffResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    staffsList = response.body()?.data ?: ArrayList()
                }
                setStaffRecView()
            }

            override fun onFailure(
                call: Call<GetStaffResponse>,
                t: Throwable
            ) {
                showErrorToast(this@StaffActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
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

                attendanceDate = "$day-$monthNumber-$year"
                Log.e("date", attendanceDate)
            } else {
                if (DateFormat.format("dd", calendar.time).toString()
                        .toInt() > DateFormat.format("dd", Calendar.getInstance().time)
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
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
                }
                //days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter

        callAPItoGetStaffsData(attendanceDate)
    }

    private fun setStaffRecView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recStaff.layoutManager = manager
            adapter = StaffListAdapter(this, staffsList)
            binding.recStaff.adapter = adapter
            adapter?.setListener(object : StaffListAdapter.ItemClickListener {
                override fun onItemClick(pos: Int) {
                    /*  val intent = Intent(this@StaffActivity, MyProfileActivity::class.java)
                      intent.putExtra("user", staffsList[pos])
                      startActivity(intent)*/
                }

                override fun onMarkAttendanceClick(pos: Int) {

                }

                override fun onMoreOptionClick(pos: Int) {
                    var sheet = ManageStaffBottomSheet(staffsList[pos])
                    sheet.setListener(object : ManageStaffBottomSheet.ClickListener {
                        override fun onMarkAttendanceClicked(selectedClassRoom: ClassroomDetails?) {
                            showBottomSheetDialog(staffsList[pos])
                        }

                        override fun onDeleteStaffClicked(selectedClassRoom: ClassroomDetails?) {
                            var sheet = StaffDismissalPopup()
                            sheet.staff = staffsList[pos]
                            sheet.clickListeners = object : StaffDismissalPopup.ClickListener {
                                override fun onDismissed(dialogFragment: DialogFragment?) {
                                    dialogFragment?.dismiss()
                                }

                                override fun onCancelClicked() {
                                }
                            }
                            sheet.show(supportFragmentManager, "StaffDismissalPopup")
                        }

                        override fun onCancelClicked() {

                        }
                    })
                    sheet.show(supportFragmentManager, "ManageStaffBottomSheet")
                }
            })
        } else {
            adapter?.setItems(staffsList)
            adapter?.notifyDataSetChanged()
        }
        if (staffsList.size > 0)
            staffsList.forEachIndexed { index, user ->
                if (user.attendance?.size ?: 0 > 0)
                    when (user.attendance?.first()?.status) {
                        "Absent", "absent" -> {
                            absent++
                        }
                        "Sign in", "signIn", "signin", "sign in" -> {
                            signIn++
                        }
                        "Sign out", "signOut", "signout", "sign out" -> {
                            signOut++
                        }
                        else -> {
                            others++
                        }
                    }
            }
        binding.tvOtherCount.text = others.toString()
        binding.tvSignOutCount.text = signOut.toString()
        binding.tvSignInCount.text = signIn.toString()
        binding.tvAbsentCount.text = absent.toString()

        if (staffsList.size > 0) {
            binding.tvNoDataFound.viewGone()
        } else {
            binding.tvNoDataFound.viewVisible()
        }
    }

    private fun showBottomSheetDialog(user: User) {
        val mBinding: BottomSheetStaffBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_staff,
            null,
            false
        )
        var otherText = ""
        var text = ""
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(mBinding.root)

        if (user.attendance?.size ?: 0 > 0)
            when (user.attendance?.first()?.status) {
                "Absent" -> {
                    mBinding.rbAbsent.isChecked = true
                    text = mBinding.rbAbsent.text.toString()
                    mBinding.rgOthers.viewGone()
                }
                "Sign in" -> {
                    mBinding.rbSignIn.isChecked = true
                    text = mBinding.rbSignIn.text.toString()
                    mBinding.rgOthers.viewGone()
                }
                "Sign out" -> {
                    mBinding.rbSignOut.isChecked = true
                    text = mBinding.rbSignOut.text.toString()
                    mBinding.rgOthers.viewGone()
                }

                "sick" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbSick.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "vacation" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbVacation.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "appointment" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbDocAppointment.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
                "late" -> {
                    mBinding.rbOther.isChecked = true
                    mBinding.rgOthers.viewVisible()

                    mBinding.rbLate.isChecked = true
                    otherText = mBinding.rbSick.text.toString()
                    text = otherText
                }
            }

        mBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == mBinding.rbOther.id) {
                mBinding.rgOthers.viewVisible()
            } else {
                mBinding.rgOthers.viewGone()
            }

            when (checkedId) {
                mBinding.rbSignOut.id -> {
                    text = "Sign out"
                }
                mBinding.rbSignIn.id -> {
                    text = "Sign in"
                }
                mBinding.rbAbsent.id -> {
                    text = "Absent"
                }
                mBinding.rbOther.id -> {
                    text = "Other"
                }
            }
        }

        mBinding.rgOthers.setOnCheckedChangeListener { group, checkedId ->
            mBinding.rbOther.isChecked = true
            when (checkedId) {
                mBinding.rbSick.id -> {
                    otherText = "sick"
                    text = otherText
                }
                mBinding.rbVacation.id -> {
                    otherText = "vacation"
                    text = otherText
                }
                mBinding.rbDocAppointment.id -> {
                    otherText = "appointment"
                    text = otherText
                }
                mBinding.rbLate.id -> {
                    otherText = "late"
                    text = otherText
                }
            }
        }

        mBinding.ivCross.setOnClickListener {
            dialog.dismiss()
        }
        mBinding.tvUpdate.setOnClickListener {
            if (validateAttendance(text))
                callAPItoUpdateAttendance(
                    mBinding.tvUpdate,
                    mBinding.progressbar, dialog, text, (user.id ?: 0).toString()
                )
        }
        dialog.show()
    }

    private fun createInviteDialog() {
        val dialogBinding = DialogStaffInvitatoinBinding.inflate(LayoutInflater.from(this))
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        alertDialog.setView(dialogBinding.root)

        dialogBinding.ivCross.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.tvInvite.setOnClickListener {
            if (validate(dialogBinding.etEmail.text.toString().trim())) {
                callAPItoInvite(
                    dialogBinding.etEmail.text.toString().trim(),
                    dialogBinding.tvInvite,
                    dialogBinding.progressbar, alertDialog
                )
            }
        }
        alertDialog.show()
    }

    private fun validate(email: String): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@StaffActivity, "No internet connection!")
            return false
        }

        if (email.isBlank()) {
//            binding.etEmail.error = "Please enter email address"
            showErrorToast(this@StaffActivity, "Please enter email address!")
            return false
        }
        if (!email.isValidEmail()) {
//            binding.etEmail.error = "Please enter email address"
            showErrorToast(this@StaffActivity, "Please enter valid email address!")
            return false
        }

        return true
    }

    private fun validateAttendance(text: String): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@StaffActivity, "No internet connection!")
            return false
        }

        if (text.isEmpty()) {
            showErrorToast(this@StaffActivity, "Please select attendance!")
            return false
        }

        return true
    }

    private fun initVar() {

    }

    private fun callAPItoInvite(
        email: String, tv: TextView, progressbar: ProgressBar, alertDialog: AlertDialog
    ) {
        showProgressBar(tv, progressbar)
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .sendInvitation(
                "Bearer " + userManager.accessToken ?: "",
                Constants.staff,
                email
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@StaffActivity, response.body()?.message ?: "")
                    hideProgressBar(tv, progressbar)
                    alertDialog.dismiss()
                } else {
                    try {
                        hideProgressBar(tv, progressbar)
                        showSuccessToast(this@StaffActivity, response.body()?.message ?: "")

//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        showErrorToast(this@StaffActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
//                        Toast.makeText(this@StaffActivity, e.message, Toast.LENGTH_LONG)
//                            .show()
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@StaffActivity, "Can't Connect to Server!")
                hideProgressBar(tv, progressbar)
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoUpdateAttendance(
        tv: TextView,
        progressbar: ProgressBar,
        alertDialog: BottomSheetDialog,
        status: String,
        staff_id: String
    ) {
        showProgressBar(tv, progressbar)
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .staffAttendance(
                "Bearer " + userManager.accessToken ?: "",
                staff_id,
                status
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@StaffActivity, response.body()?.message ?: "")
                    hideProgressBar(tv, progressbar)

                    absent = 0
                    signIn = 0
                    signOut = 0
                    others = 0
                    callAPItoGetStaffsData(attendanceDate)
                    alertDialog.dismiss()
                } else {
                    try {
                        hideProgressBar(tv, progressbar)
                        showSuccessToast(this@StaffActivity, response.body()?.message ?: "")

//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        showErrorToast(this@StaffActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
//                        Toast.makeText(this@StaffActivity, e.message, Toast.LENGTH_LONG)
//                            .show()
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@StaffActivity, "Can't Connect to Server!")
                hideProgressBar(tv, progressbar)
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun showProgressBar(tvSignin: TextView, progressbar: ProgressBar) {
        tvSignin.viewGone()
        progressbar.viewVisible()
    }

    private fun hideProgressBar(tvSignin: TextView, progressbar: ProgressBar) {
        tvSignin.viewVisible()
        progressbar.viewGone()
    }

    override fun onITemClick(position: Int, item: CalendarModel) {
//        days.forEachIndexed { _, calendarModel ->
//            calendarModel.isSelected = false
//        }
//        days[position].isSelected = !days[position].isSelected
//        daysAdapter?.notifyDataSetChanged()
//        val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
//        val day = DateFormat.format("dd", item.date) as String // 20
//        val monthString = DateFormat.format("MMM", item.date) as String // Jun
//        val monthNumber = DateFormat.format("MM", item.date) as String // 06
//        val year = DateFormat.format("yyyy", item.date) as String // 2013

//        attendanceDate = "$day-$monthNumber-$year"
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

//
//        if (date != "" && selectedChild != null) {
            //showLoading()
            signIn = 0
            signOut = 0
            absent = 0
            others = 0
            callAPItoGetStaffsData(attendanceDate)
        }
        Log.e("date", attendanceDate)
    }
}