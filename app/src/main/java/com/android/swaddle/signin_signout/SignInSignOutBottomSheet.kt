package com.android.swaddle.signin_signout

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.parent.DialogAddNoteForLateArrival
import com.android.swaddle.adapters.parents_bottom_sheet_adapter.ChildesBottomSheetAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.BottomSheetSigninSignoutBinding
import com.android.swaddle.enums.UserType
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.ProgressDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SignInSignOutBottomSheet(var baseActivity: BaseActivity) :
    BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetSigninSignoutBinding

    val userManager: LoginData
        get() {
            return UserData.user(requireContext())
        }
    private val pd = ProgressDialog.newInstance()
    var classRoomsList = ArrayList<ClassroomDetails>()
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    var selectedClassRoom: ClassroomDetails? = null
    var selectedPos = 0
    var adapter: ChildesBottomSheetAdapter? = null
    var childesList = ArrayList<ChildInfo>()
    var newList = ArrayList<ChildInfo>()
    var isParent = false
    var checkin = false
    var attendanceDate = ""
    var attendanceTime = ""

    companion object {
        var currentTab = "Pending"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = BottomSheetSigninSignoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        currentTab = "Pending"
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isParent =
            (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult")

        if (userManager.user?.type == UserType.PARENT.type || userManager.user?.type == UserType.AUTHORIZED_ADULT.type) {
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
            callAPItoGetChildes()
        } else if (userManager.user?.type == UserType.PROVIDER.type || userManager.user?.type == UserType.STAFF.type) {
            binding.tvClass.viewVisible()
            binding.cvSpinnerClass.viewVisible()
            callAPItoGetClassRooms()
        } else {
            binding.tvClass.viewGone()
            binding.cvSpinnerClass.viewGone()
            binding.childRecyclerView.viewGone()
            binding.noData.viewGone()
        }

        initListeners()

        binding.pendingLayout.setOnClickListener {
            binding.tvSelectAll.text = "Deselect all"
            binding.tvSelectAll.callOnClick()
            currentTab = "Pending"
            selectTab(binding.pendingView, binding.pendingText)
            binding.cvSignIn.viewVisible()
            binding.cvSignOut.viewGone()
            filterList()
        }

        binding.checkInLayout.setOnClickListener {
            binding.tvSelectAll.text = "Deselect all"
            binding.tvSelectAll.callOnClick()
            currentTab = "CheckedIn"
            selectTab(binding.checkinView, binding.checkinText)
            binding.cvSignIn.viewGone()
            binding.cvSignOut.viewVisible()
            filterList()
        }
    }

    private fun filterList() {
        try {

            if (childesList.isEmpty()) {
                binding.noData.viewVisible()
                binding.noData.text = "No Data found"
            } else {
                if (currentTab == "Pending") {
                    newList.clear()
                    Log.e("listBefore", childesList.size.toString())
                    newList.addAll(childesList.filter { it.childAttendance == null || (it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.isEmpty() == true) || (it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy != null) })
                    Log.e("listAfter", childesList.size.toString())
                    Log.e("listAfter", newList.size.toString())
                } else {
                    newList.clear()
                    Log.e("listBefore", childesList.size.toString())
                    newList.addAll(childesList.filter { it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.isNotEmpty() == true && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy == null })
//                newList.addAll(childesList.filter { it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy == null })
                    Log.e("listAfter", childesList.size.toString())
                    Log.e("listAfter", newList.size.toString())
                }
                if (newList.size > 0) {
                    binding.noData.viewGone()
                    binding.tvSelectAll.viewVisible()
                    binding.childRecyclerView.viewVisible()
                    binding.cvSignIn.isEnabled = true
                    binding.cvSignOut.isEnabled = true

                    binding.tvChildren.viewVisible()
                    if (currentTab == "Pending")
                        binding.cvSignIn.viewVisible()
                    else
                        binding.cvSignOut.viewVisible()

                    binding.cvSignIn.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_700
                        )
                    )
                    binding.cvSignOut.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorOrange
                        )
                    )
                } else {
                    binding.noData.viewVisible()
                    binding.tvSelectAll.viewGone()
                    binding.tvChildren.viewGone()

                    if (currentTab == "Pending") {
                        binding.noData.text = "Children already signed in for the day"
                        binding.cvSignIn.viewGone()
                    } else {
                        binding.noData.text = "Children already signed out for the day"
                        binding.cvSignOut.viewGone()
                    }
                    binding.childRecyclerView.viewGone()
                    binding.cvSignIn.isEnabled = false
                    binding.cvSignOut.isEnabled = false
                    binding.cvSignIn.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_grey_400
                        )
                    )

                    binding.cvSignOut.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_grey_400
                        )
                    )
                }
                adapter?.setItems(newList)
                adapter?.notifyDataSetChanged()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectTab(selectedView: View, selectedText: TextView) {
        binding.checkinView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_300
            )
        )
        binding.pendingView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_300
            )
        )
        binding.pendingText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_300
            )
        )
        binding.checkinText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey_300
            )
        )
        selectedView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.quantum_orange
            )
        )

        selectedText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        newList.forEach { item ->
            item.isSelected = false
        }

    }

    private fun initListeners() {
        val c: Date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        attendanceDate = df.format(c)
        attendanceTime = dfTime.format(c)

        binding.tvSelectAll.setOnClickListener {
            if (currentTab == "Pending") {
                newList.filter { it.childAttendance == null }
            } else {
                newList.filter { it.childAttendance != null }
            }

            if (binding.tvSelectAll.text == "Select all") {
                binding.tvSelectAll.text = "Deselect all"
                newList.forEachIndexed { index, childInfo ->

                    if (userManager.user?.type == Constants.parent)
                        if (currentTab == "Pending") {
                            if (childInfo.absent_notes != null) {
                                if (childInfo.absent_notes.is_absent == 0)
                                    newList[index].isSelected = true
                            } else
                                newList[index].isSelected = true
                        } else
                            newList[index].isSelected = true
                    else
                        newList[index].isSelected = true
                }
            } else {
                binding.tvSelectAll.text = "Select all"
                newList.forEachIndexed { index, childInfo ->

                    newList[index].isSelected = false
                }
            }

            adapter?.setItems(newList)
            adapter?.notifyDataSetChanged()
        }

        binding.cvSignIn.setOnClickListener {
            markAttendance(signInSignOut = "signIn")
        }

        binding.cvSignOut.setOnClickListener {
            markAttendance(signInSignOut = "signOut")
        }

        binding.ivCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setRecyclerView() {
        try {

            if (childesList.isEmpty()) {
                binding.noData.viewVisible()
                binding.noData.text = "No Data found"
            } else {

                if (currentTab == "Pending") {
                    newList.clear()
                    newList.addAll(childesList.filter { it.childAttendance == null || (it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.isEmpty() == true) || (it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy != null) })

//                    newList.addAll(childesList.filter { it.childAttendance == null || (it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy != null) })
                    newList
                } else {
                    newList.clear()
                    newList.addAll(childesList.filter { it.childAttendance != null && it.childAttendance?.childAttendanceMultiple?.isNotEmpty() == true && it.childAttendance?.childAttendanceMultiple?.last()?.pickedUpBy == null })
                }

                if (adapter == null) {
                    val manager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = ChildesBottomSheetAdapter(requireContext(), newList, object :
                        ChildesBottomSheetAdapter.ItemClickListener {
                        override fun onItemChecked(position: Int, item: ChildInfo) {
                            val isChecked = item.isSelected ?: false
                            newList[position].isSelected = !isChecked
                            adapter?.setItems(newList)
                            adapter?.notifyDataSetChanged()
                        }

                        override fun onAddNoteClicked(position: Int, item: ChildInfo) {
                            DialogAddNoteForLateArrival(
                                item.id ?: -1, -1,
                                object : DialogAddNoteForLateArrival.AddNoteLateArrivalInterface {
                                    override fun onSaveNote(
                                        text: String,
                                        childId: Int,
                                        position: Int,
                                        isAbsent: Int
                                    ) {
                                        saveNoteForAbsent(text, childId, position, isAbsent)
                                    }
                                }).show(
                                activity?.supportFragmentManager!!,
                                "DialogAddNoteForLateArrival"
                            )
                        }
                    })
                    binding.childRecyclerView.layoutManager = manager
                    binding.childRecyclerView.adapter = adapter

                } else {
                    adapter?.setItems(newList)
                    adapter?.notifyDataSetChanged()
                }
                if (newList.size > 0) {
                    binding.childRecyclerView.viewVisible()
                    binding.tvSelectAll.viewVisible()
                    binding.tvChildren.viewVisible()
                    binding.cvSignIn.viewVisible()
                    binding.noData.viewGone()
                    binding.cvSignIn.isEnabled = true
                    binding.cvSignOut.isEnabled = true
                    binding.cvSignIn.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_700
                        )
                    )
                    binding.cvSignOut.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorOrange
                        )
                    )
                } else {
                    binding.childRecyclerView.viewGone()
                    binding.tvSelectAll.viewGone()
                    binding.tvChildren.viewGone()
                    binding.cvSignIn.viewGone()
                    binding.noData.viewVisible()
                    if (currentTab == "Pending")
                        binding.noData.text = "Children already signed in for the day"
                    else
                        binding.noData.text = "Children already signed out for the day"
                    binding.cvSignIn.isEnabled = false
                    binding.cvSignOut.isEnabled = false
                    binding.cvSignIn.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_grey_400
                        )
                    )

                    binding.cvSignOut.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_grey_400
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveNoteForAbsent(text: String, childId: Int, position: Int, isAbsent: Int) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .saveAbsentNote("Bearer " + userManager.accessToken ?: "", childId, text, isAbsent)

        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                if (response.isSuccessful) {

                    if (response.body()?.status == true) {

                        showSuccessToast(
                            requireContext(),
                            response.body()?.message ?: "Please try again"
                        )
                        childesList.clear()
                        newList.clear()
                        if (userManager.user?.type == UserType.PARENT.type || userManager.user?.type == UserType.AUTHORIZED_ADULT.type) {
                            binding.tvClass.viewGone()
                            binding.cvSpinnerClass.viewGone()
                            callAPItoGetChildes()
                        } else if (userManager.user?.type == UserType.PROVIDER.type || userManager.user?.type == UserType.STAFF.type) {
                            binding.tvClass.viewVisible()
                            binding.cvSpinnerClass.viewVisible()
                            callAPItoGetClassRooms()
                        } else {
                            binding.tvClass.viewGone()
                            binding.cvSpinnerClass.viewGone()
                            binding.childRecyclerView.viewGone()
                            binding.noData.viewGone()
                        }
                    } else {
                        showErrorToast(
                            requireContext(),
                            response.body()?.message ?: "Please try again"
                        )
                    }
                } else {
                    val j = JSONObject(response.errorBody().toString())
                    showErrorToast(requireContext(), j.getString("message"))
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                hideLoading()
                showErrorToast(requireContext(), t.localizedMessage)
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetChildes() {
        showLoading()
        val call: Call<ChildesResponse> =
            if (isParent)
                RetrofitClass.getInstance().webRequestsInstance
                    .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
            else
                RetrofitClass.getInstance().webRequestsInstance
                    .getChildesListOfRoom(
                        "Bearer " + userManager.accessToken ?: "",
                        (selectedClassRoom?.id ?: 0).toString(), "0"
                    )

        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()

//                var cl = ArrayList<ChildInfo>()
//                cl = response.body()?.data ?: ArrayList()
//
//                for (i in cl.indices)
//                    if (cl[i].child_attendance?.arrival_time == null || cl[i].child_attendance?.departure_time == null || cl[i].child_attendance == null)
//                        childesList.add(cl[i])

                setRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(requireContext(), "Can't Connect to Server!")
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
                hideLoading()
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()

            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(context!!, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter =
                ClassSpinnerAdapter(requireContext(), classRoomsList)
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
                    callAPItoGetChildes()
                    Log.e("SelectedRoom", classRoomsList[position].id.toString())
                    //   showSuccessToast(this@ProviderAttendanceActivity,"Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()

            selectedClassRoom = classRoomsList.first()
            callAPItoGetChildes()
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
        }
        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun markAttendance(
        signInSignOut: String
    ) {
        val ids = ArrayList<String>()
        val call: Call<LoginResponse> =
            if (userManager.user?.type == UserType.PARENT.type || userManager.user?.type == UserType.AUTHORIZED_ADULT.type) {
                newList.forEachIndexed { _, childInfo ->
                    if (childInfo.isSelected == true)
                        ids.add("${childInfo.id}")
                }

                if (ids.isEmpty()) {
                    showErrorToast(requireContext(), "Please select child")
                    return
                }

                val asda = StringUtils.join(",", ids)
                if (ids.size == 0) {
                    showErrorToast(baseActivity, "No Child Selected!")
                    return
                }
                RetrofitClass.getInstance().webRequestsInstance
                    .signInSignOut(
                        "Bearer " + userManager.accessToken ?: "",
                        userManager.user?.type,
                        signInSignOut,
                        attendanceDate,
                        TextUtils.join(",", ids),
                        attendanceTime,
                        0
                    )
            } else if (userManager.user?.type == UserType.PROVIDER.type || userManager.user?.type == UserType.STAFF.type) {
                newList.forEachIndexed { _, childInfo ->
                    if (childInfo.isSelected == true)
                        ids.add("${childInfo.id}")
                }

                if (ids.isEmpty()) {
                    showErrorToast(requireContext(), "Please select child")
                    return
                }

                RetrofitClass.getInstance().webRequestsInstance
                    .signInSignOut(
                        "Bearer " + userManager.accessToken ?: "",
                        userManager.user?.type,
                        signInSignOut,
                        attendanceDate,
                        TextUtils.join(",", ids),
                        attendanceTime,
                        0
                    )
            } else {
                RetrofitClass.getInstance().webRequestsInstance
                    .signInSignOut(
                        "Bearer " + userManager.accessToken ?: "",
                        userManager.user?.type,
                        signInSignOut,
                        attendanceDate,
                        ids.toString(),
                        attendanceTime,
                        userManager.user?.id!!
                    )
            }
        showLoading()

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(requireContext(), response.body()?.message ?: "")
                    hideLoading()

                    dismiss()

                } else {
                    hideLoading()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(requireContext(), jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(requireContext(), response.body()?.message ?: "")
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(requireContext(), "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    fun showLoading(text: String = "Please wait...", cancelable: Boolean = false) {

        activity?.runOnUiThread {
            if (!pd.isAdded) {
                try {
                    if (!pd.isVisible && !pd.isAdded) {
                        activity?.supportFragmentManager?.let { pd.show(it, "pd") }
                    }
                    Handler().postDelayed({
                        pd.txtProgress.text = text
                        pd.isCancelable = cancelable
                    }, 200)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun hideLoading() {
        activity?.runOnUiThread {
            try {
                if (pd.isAdded) pd.dismiss()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}