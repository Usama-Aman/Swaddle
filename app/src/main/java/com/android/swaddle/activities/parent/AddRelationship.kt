package com.android.swaddle.activities.parent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.adapters.parent_adapters.AddRelationShipChildAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddRelationShipBinding
import com.android.swaddle.fragments.bottomSheetFragment.ChildesBottomSheet
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.isValidEmail
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRelationship : BaseActivity() {
    private lateinit var binding: ActivityAddRelationShipBinding
    var childesList = ArrayList<ChildInfo>()
    var selectedChildesList = ArrayList<ChildInfo>()
    private var adapter: AddRelationShipChildAdapter? = null

    var isParent = false
    var classRoomsList = ArrayList<ClassroomDetails>()
    var selectedClassRoom: ClassroomDetails? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    var selectedChild: ChildInfo? = null
    var selectedPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRelationShipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listener()

        if (isParent) {
            binding.cvSpinnerClass.viewGone()
            binding.tvClass.viewGone()
            callAPItoGetChildes()
        } else {
            binding.cvSpinnerClass.viewVisible()
            binding.tvClass.viewVisible()
            callAPItoGetClassRooms()
        }

    }

    private fun listener() {
        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.clDropOff.setOnClickListener {
            var sheet = ChildesBottomSheet(this@AddRelationship, childesList)
            sheet.show(supportFragmentManager, "ChildesBottomSheet")
            sheet.setListener(object : ChildesBottomSheet.ClickListener {
                override fun onSelectNowClicked(selectedChilds: ArrayList<ChildInfo>) {
                    if (childesList.size > 0) {
                        binding.tvSpinnerChildesNoData.viewGone()
                        binding.clDropOff.viewVisible()

                        selectedChildesList.clear()
                        selectedChildesList.addAll(selectedChilds)
                        setRecyclerView()

                    } else {
                        binding.tvSpinnerChildesNoData.viewVisible()
                        binding.clDropOff.viewGone()
                    }
                }

                override fun onCancelClicked() {

                }
            })
        }
    }

    private fun init() {

        isParent =
            (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult")




        binding.tvInviteNow.setOnClickListener {
            if (validation()) {
                callAPItoAdRelationShip()
            }
        }
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
                showErrorToast(this@AddRelationship, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter =
                ClassSpinnerAdapter(this@AddRelationship, classRoomsList)
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
            binding.clDropOff.viewGone()
            selectedChild = null
        }
        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun callAPItoGetChildes() {
        showLoading()
        val call: Call<ChildesResponse> =
            if (isParent) RetrofitClass.getInstance().webRequestsInstance
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
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@AddRelationship, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoAdRelationShip(
    ) {
        var ids = StringBuilder()
        selectedChildesList.forEachIndexed { index, childInfo ->
            if (selectedChildesList.size == 1)
                ids.append("${childInfo.id ?: 0}")
            else {
                if (selectedChildesList.size - 1 == index)
                    ids.append("${childInfo.id ?: 0}")
                else
                    ids.append("${childInfo.id ?: 0},")
            }
        }
        Log.e("ids", "$ids")
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .sendAuthrizedAdultsInvitation(
                "Bearer " + userManager.accessToken ?: "",
                "authorized_adult",
                binding.etEmail.text.toString().trim(), "[${ids}]"
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@AddRelationship, response.body()?.message ?: "")
                    hideProgressBar()
                    finish()
                } else {

                    hideProgressBar()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@AddRelationship, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@AddRelationship, response.body()?.message ?: "")
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@AddRelationship, "Can't Connect to Server!")
                hideProgressBar()
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun showProgressBar() {
        binding.tvInviteNow.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvInviteNow.viewVisible()
        binding.progressbar.viewGone()
    }


    private fun setChildSpinner() {

        setRecyclerView()
    }

    private fun setRecyclerView() {
        selectedChildesList.clear()
        selectedChildesList.addAll(childesList.filter { it.isSelected == true })

        if (adapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            adapter = AddRelationShipChildAdapter(this, selectedChildesList, object :
                AddRelationShipChildAdapter.ItemClickListener {
                override fun onItemClick(position: Int) {

                }

                override fun onItemRemovedClick(position: Int) {
                    childesList[position].isSelected = false
                    selectedChildesList
                    setRecyclerView()

                }
            })
            binding.recChild.layoutManager = manager
            binding.recChild.adapter = adapter
        } else {
            adapter?.setItems(selectedChildesList)
            adapter?.notifyDataSetChanged()
        }

        if (selectedChildesList.size > 0) {
            binding.tvNoDocuments.viewGone()
        } else {
            binding.tvNoDocuments.viewVisible()
        }
    }

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@AddRelationship, "No internet connection!")
            return false
        }
        val name = binding.etEmail.text.toString()

        if (name.isBlank()) {
            showErrorToast(
                this@AddRelationship,
                "Please enter email to invite authorized adult/add new relation!"
            )
            return false
        }
        if (!name.isValidEmail()) {
            showErrorToast(
                this@AddRelationship,
                "Please enter a valid email to invite authorized adult/add new relation!"
            )
            return false
        }

        if (selectedChildesList.size < 1) {
            showErrorToast(this@AddRelationship, "Please Select at least one child!")
            return false
        }

        return true
    }
}