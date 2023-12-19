package com.android.swaddle.activities.providers.class_room_ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.parent.MedicalReportActivity
import com.android.swaddle.activities.providers.RelationshipsActivity
import com.android.swaddle.activities.providers.reports.ProviderAttendanceActivity
import com.android.swaddle.adapters.provider_adapter.ClassRoomListAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.bottomsheets.ClassroomOptionsBottomSheet
import com.android.swaddle.databinding.ActivityClassListBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassRoomsResponse
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.response.ApiController
import com.android.swaddle.response.ResponseTypes
import com.android.swaddle.utils.CustomToasts
import com.codingpixel.a2i.response.ApiCallBack
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import kotlinx.android.synthetic.main.no_data_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClassListActivity : BaseActivity(), ClassRoomListAdapter.ItemClickListener, ApiCallBack {
    private lateinit var binding: ActivityClassListBinding
    private var adapter: ClassRoomListAdapter? = null
    var list = ArrayList<ClassroomDetails>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        callAPItoGetClassRooms()

        listener()
    }


    override fun onResume() {
        super.onResume()
        callAPItoGetClassRooms()
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
                    list = response.body()?.data ?: ArrayList()
                    setRecyclerView()
                } else {
                    setRecyclerView()
                    hideLoading()
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ClassListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(this)
            binding.recViewClassList.layoutManager = manager
            adapter = ClassRoomListAdapter(this, list, this)
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
        binding.noData.retryBtn.setOnClickListener { callAPItoGetClassRooms() }

        binding.imgAdd.setOnClickListener {
            val intent = Intent(this, CreateClassRoom::class.java)
            intent.putExtra("type", "create")
            startActivity(intent)
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun init() {

    }

    override fun onItemClick(position: Int, item: ClassroomDetails) {
        startActivity<ClassRoomChildDetail>("item" to item, "selectedPos" to position)
    }

    override fun onMedicalClick(position: Int, item: ClassroomDetails) {
        startActivity<MedicalReportActivity>("position" to position)
    }

    override fun onAttendanceClick(position: Int, item: ClassroomDetails) {
        if (isNetworkConnected()) {
            startActivity<ProviderAttendanceActivity>("position" to position)
        } else {
            CustomToasts.failureToast(this@ClassListActivity, "No internet connection!")
        }
    }

    override fun onPaymentClick(position: Int, item: ClassroomDetails) {
        startActivity<ParentPaymentMainActivity>("position" to position)
    }

    override fun onRelationshipClick(position: Int, item: ClassroomDetails) {
        startActivity<RelationshipsActivity>("position" to position)
    }

    override fun onMoreClick(position: Int, item: ClassroomDetails) {

        var sheet = ClassroomOptionsBottomSheet()
        sheet.show(supportFragmentManager, "VideoViewsBottomSheet")

        sheet.setListener(object : ClassroomOptionsBottomSheet.ItemClickListener {
            override fun editClassRoom() {

                val intent = Intent(this@ClassListActivity, CreateClassRoom::class.java)
                intent.putExtra("item", item)
                intent.putExtra("type", "update")
                startActivity(intent)
                sheet.dismiss()
            }

            override fun deleteClassRoom() {
                sheet.dismiss()
                val alert =
                    AlertView(
                        "Delete Classroom?",
                        "Are you sure you want to delete classroom?",
                        AlertStyle.DIALOG
                    )
                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                    showLoading()
                    GlobalScope.launch {
                        ApiController.getInstance(this@ClassListActivity)
                            .removeClassRoom(
                                "Bearer ${userManager.accessToken}",
                                this@ClassListActivity, ResponseTypes.REMOVE_ROOM, item.id!!
                            )
                    }
                })
                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                    sheet.dismiss()
                })
                alert.show(this@ClassListActivity)

            }
        }
        )

    }

    override fun onResponse(call: Any?, response: Any, responseType: String) {
        hideLoading()
        callAPItoGetClassRooms()
    }

    override fun onFailed(call: Any?, t: Throwable, responseType: String) {
        showErrorToast(this@ClassListActivity, "Try again")
        hideLoading()
    }

    override fun onSuccessFalse(call: Any?, response: Any, responseType: String) {
        showErrorToast(this@ClassListActivity, "Try again")
        hideLoading()
    }

}