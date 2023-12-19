package com.android.swaddle.activities.providers.class_room_ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.providers.ChildProfileDetailActivity
import com.android.swaddle.adapters.parent_adapters.ChildProfileListAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityClassRoomChildDetailBinding
import com.android.swaddle.databinding.DialogPromotChildBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddChildBottomSheetDialogFragment
import com.android.swaddle.fragments.bottomSheetFragment.PromoteChildBottomSheet
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClassRoomChildDetail : BaseActivity() {
    private lateinit var binding: ActivityClassRoomChildDetailBinding
    private var adapter: ChildProfileListAdapter? = null
    var list = ArrayList<ChildInfo>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var item: ClassroomDetails? = null
    var selectedPos = 0
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassRoomChildDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent?.getSerializableExtra("item") as ClassroomDetails
        selectedPos = intent?.getIntExtra("selectedPos", 0) ?: 0

        init()

        listener()

        callAPItoGetChildes(item?.id)
        callAPItoGetClassRooms()
    }

    private fun callAPItoGetClassRooms(
    ) {
//        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getClassRooms("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
//                hideLoading()

                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                Handler().postDelayed({
                    callAPItoGetClassRooms()
                }, 2000)
//                showErrorToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
//                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@ClassRoomChildDetail, classRoomsList)
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
                item = classRoomsList[position]
                callAPItoGetChildes(item?.id)
                Log.e("SelectedRoom", item?.id.toString())
                list.clear()
                //   showSuccessToast(this@MedicalReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
            callAPItoGetChildes(item?.id)

        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()

        }

        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            item = classRoomsList[selectedPos]
        }
    }

    private fun callAPItoPromoteChild(childInfo: ChildInfo, selectedClassRoom: ClassroomDetails?) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .promoteChild(
                "Bearer " + userManager.accessToken ?: "",
                "${childInfo.id}",
                "${selectedClassRoom?.id}"
            )
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(this@ClassRoomChildDetail, response.body()?.message ?: "")
                    callAPItoGetChildes(item?.id)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoDeleteChild(childInfo: ChildInfo) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteChild("Bearer " + userManager.accessToken ?: "", "${childInfo.id}")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {

                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(this@ClassRoomChildDetail, response.body()?.message ?: "")
                    callAPItoGetChildes(item?.id)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoRemoveChildFromClass(childInfo: ChildInfo) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .removeChildOfClassRoom("Bearer " + userManager.accessToken ?: "", "${childInfo.id}")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {

                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(this@ClassRoomChildDetail, response.body()?.message ?: "")
                    callAPItoGetChildes(item?.id)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoRestore(childInfo: ChildInfo) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .restoreChild("Bearer " + userManager.accessToken ?: "", "${childInfo.id}")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    showSuccessToast(this@ClassRoomChildDetail, response.body()?.message ?: "")
                    callAPItoGetChildes(item?.id)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ClassRoomChildDetail,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(this)
            binding.recViewClassList.layoutManager = manager
            adapter = ChildProfileListAdapter(this, list)
            adapter?.setListener(object :
                ChildProfileListAdapter.ItemClickListener {
                override fun onItemClick(position: Int, item: ChildInfo) {
                    if (item.deletedAt == null) {
                        startActivity<ChildProfileDetailActivity>("item" to list[position])
                    }
                }

                override fun onPromoteClick(position: Int) {
                    var sheet = PromoteChildBottomSheet(
                        this@ClassRoomChildDetail,
                        list[position],
                        classRoomsList
                    )
                    sheet.setListener(object : PromoteChildBottomSheet.ClickListener {
                        override fun onPromoteClicked(selectedClassRoom: ClassroomDetails?) {
                            callAPItoPromoteChild(list[position], selectedClassRoom)
                        }

                        override fun onCancelClicked() {

                        }
                    })
                    sheet.show(supportFragmentManager, "PromoteChildBottomSheet")
                }

                override fun onOptionItemClicked(position: Int) {
                    showBottomSheet(list[position], position)

                }
            })
            binding.recViewClassList.adapter = adapter

        } else {
            adapter?.setItems(list)
            adapter?.notifyDataSetChanged()
        }

        if (list.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }

    private fun listener() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.noData.retryBtn.setOnClickListener {
            callAPItoGetChildes(item?.id)
            callAPItoGetClassRooms()
        }
    }

    private fun callAPItoGetChildes(id: Int?) {
        resetAttendanceCounts()
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
                list = ArrayList()
                list = response.body()?.data ?: ArrayList()
                setRecyclerView()
                binding.tvHeading.text = item?.name ?: ""
                setAttendanceCounts(response.body()?.counts)
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ClassRoomChildDetail, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setAttendanceCounts(counts: Counts?) {
        if (counts?.signInCount != null)
            binding.tvSignInCount.text = "${counts.signInCount}"
        if (counts?.signOutCount != null)
            binding.tvSignOutCount.text = "${counts.signOutCount}"
        if (counts?.absentCount != null)
            binding.tvAbsentCount.text = "${counts.absentCount}"
        if (counts?.absentCount != null)
            binding.tvOtherCount.text = "0"
    }

    private fun resetAttendanceCounts() {
        binding.tvSignInCount.text = "-"
        binding.tvSignOutCount.text = "-"
        binding.tvAbsentCount.text = "-"
        binding.tvOtherCount.text = "-"
    }

    private fun init() {
        binding.tvHeading.text = item?.name ?: ""
    }

    private fun showBottomSheet(childInfo: ChildInfo, position: Int) {
        val bottomFragment =
            AddChildBottomSheetDialogFragment(childInfo, this@ClassRoomChildDetail, true)
        bottomFragment.show(
            supportFragmentManager,
            "bottom_sheet"
        )

        bottomFragment.setListener(object : AddChildBottomSheetDialogFragment.ClickListener {
            override fun onPromoteClicked(dialog: BottomSheetDialogFragment) {
                dialog.dismiss()
                var sheet = PromoteChildBottomSheet(
                    this@ClassRoomChildDetail,
                    childInfo,
                    classRoomsList
                )
                sheet.setListener(object : PromoteChildBottomSheet.ClickListener {
                    override fun onPromoteClicked(selectedClassRoom: ClassroomDetails?) {
                        callAPItoPromoteChild(childInfo, selectedClassRoom)
                        bottomFragment.dismiss()
                    }

                    override fun onCancelClicked() {

                    }
                })
                sheet.show(supportFragmentManager, "PromoteChildBottomSheet")
            }

            override fun onOpenProfileClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
                startActivity<ChildProfileDetailActivity>("item" to childInfo)
            }

            override fun onRestoreClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
                val alert =
                    AlertView(
                        "Restore?",
                        "Are you sure you want to Restore ${childInfo.firstName} ${childInfo.lastName}?",
                        AlertStyle.DIALOG
                    )
                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                    //=call api to remove from Classroom...
                    callAPItoRestore(childInfo)
                })
                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                })
                alert.show(this@ClassRoomChildDetail)
            }

            override fun onDeleteClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
                val alert =
                    AlertView(
                        "Remove?",
                        "Are you sure you want to Delete ${childInfo.firstName} ${childInfo.lastName}?",
                        AlertStyle.DIALOG
                    )
                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                    //=call api to remove from Classroom...
                    callAPItoDeleteChild(childInfo)
                })
                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                })
                alert.show(this@ClassRoomChildDetail)
            }

            override fun onRemoveFromClassClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
                val alert =
                    AlertView(
                        "Remove?",
                        "Are you sure you want to Remove ${childInfo.firstName} ${childInfo.lastName} from class?",
                        AlertStyle.DIALOG
                    )
                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                    //=call api to remove from Classroom...
                    callAPItoRemoveChildFromClass(childInfo)
                })
                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                })
                alert.show(this@ClassRoomChildDetail)
            }
//            override fun onPromoteClicked(dialog: BottomSheetDialogFragment) {
//                bottomFragment.dismiss()
//                var sheet = PromoteChildBottomSheet(
//                    this@ClassRoomChildDetail,
//                    list[position],
//                    classRoomsList
//                )
//                sheet.setListener(object : PromoteChildBottomSheet.ClickListener {
//                    override fun onPromoteClicked(selectedClassRoom: ClassroomDetails?) {
//                        callAPItoPromoteChild(list[position], selectedClassRoom)
//                    }
//                    override fun onCancelClicked() {
//
//                    }
//                })
//                sheet.show(supportFragmentManager, "PromoteChildBottomSheet")
////                createPromoteChild(childInfo)
//
//
//            }


            override fun onCancelClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
            }

            override fun onEditProfileClicked(dialog: BottomSheetDialogFragment) {

            }
        })
    }

    private fun createPromoteChild(childInfo: ChildInfo) {
        val dialogBinding = DialogPromotChildBinding.inflate(LayoutInflater.from(this))
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        alertDialog.setView(dialogBinding.root)
        var selectedClassRoom: ClassroomDetails? = null

        val adapter = ClassSpinnerAdapter(this, classRoomsList)
        dialogBinding.spClassRoom.adapter = adapter
        dialogBinding.spClassRoom.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedClassRoom = classRoomsList[position]
                    //   showSuccessToast(this@DailyReportActivity,"Click")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        dialogBinding.btnPromote.setOnClickListener {

            if (selectedClassRoom == null) {
                showErrorToast(this@ClassRoomChildDetail, "Please select Classroom to promote!")
                return@setOnClickListener
            }
            alertDialog.dismiss()
            val alert =
                AlertView(
                    "Promote?",
                    "Are you sure you want to Promote ${childInfo.firstName} ${childInfo.lastName}?",
                    AlertStyle.DIALOG
                )
            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                callAPItoPromoteChild(childInfo, selectedClassRoom)
            })
            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
            })
            alert.show(this@ClassRoomChildDetail)
        }

        dialogBinding.ivCross.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}