package com.android.swaddle.activities.parent

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.providers.ChildProfileDetailActivity
import com.android.swaddle.adapters.parent_adapters.ChildProfileListAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.toArrayList
import com.android.swaddle.databinding.ActivityChildProfileListBinding
import com.android.swaddle.databinding.DialogPromotChildBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddChildBottomSheetDialogFragment
import com.android.swaddle.fragments.bottomSheetFragment.PromoteChildBottomSheet
import com.android.swaddle.helper.*
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildProfileListActivity : BaseActivity() {
    private lateinit var binding: ActivityChildProfileListBinding
    var adapter: ChildProfileListAdapter? = null
    var list = ArrayList<ChildInfo>()
    var classRoomsList = ArrayList<ClassroomDetails>()
    var selectedTab = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildProfileListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listeners()
        if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff") || userManager.user?.type.equals(
                "parent"
            )
        ) {
            binding.ivAdd.viewVisible()
        } else {
            binding.ivAdd.viewGone()
        }
        if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
//            callAPItoGetProviderChildes()
            callAPItoGetAllChildes()
            callAPItoGetClassRooms()
        } else {
            callAPItoGetChildes()
        }
    }

    private fun resetAndSelectTab(tv: TextView) {
        binding.tvActive.textColor = resources.getColor(R.color.txt_white_gray)
        binding.tvDeleted.textColor = resources.getColor(R.color.txt_white_gray)
        binding.viewActive.viewInVisible()
        binding.viewDeleted.viewInVisible()

        tv.textColor = resources.getColor(R.color.black)
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
                if (response.body()?.status == true) {
                    classRoomsList = response.body()?.data ?: ArrayList()
                } else {
                    Handler().postDelayed({
                        callAPItoGetClassRooms()
                    }, 2000)
                }
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

    private fun listeners() {
        binding.ivAdd.setOnClickListener {
            startActivity<AddChildActivity>("fromList" to true)
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.noData.retryBtn.setOnClickListener {
            if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
//                callAPItoGetProviderChildes()
                callAPItoGetAllChildes()
            } else {
                callAPItoGetChildes()
            }
        }
        binding.tvActive.setOnClickListener {
            resetAndSelectTab(binding.tvActive)
            binding.viewActive.viewVisible()
            selectedTab = 0
            setRecyclerView(list)
            binding.etSearch.setText("")
        }
        binding.tvDeleted.setOnClickListener {
            resetAndSelectTab(binding.tvDeleted)
            binding.viewDeleted.viewVisible()
            selectedTab = 1
            setRecyclerView(list)
            binding.etSearch.setText("")
        }
    }

    private fun setRecyclerView(mData: ArrayList<ChildInfo>) {

        var tempList = ArrayList<ChildInfo>()
        tempList.addAll(mData)

        tempList = if (selectedTab == 0) {
            tempList.filter { it.deletedAt == null } as ArrayList

        } else {
            tempList.filter { it.deletedAt != null } as ArrayList
        }

//        if (adapter == null) {
        val manager = LinearLayoutManager(this)
        binding.recViewClassList.layoutManager = manager
        adapter = ChildProfileListAdapter(this@ChildProfileListActivity, tempList)
        binding.recViewClassList.adapter = adapter
        adapter?.setListener(object : ChildProfileListAdapter.ItemClickListener {
            override fun onItemClick(position: Int, item: ChildInfo) {
                if (item.deletedAt == null) {
                    val intent =
                        Intent(this@ChildProfileListActivity, ChildProfileActivity::class.java)
                    intent.putExtra("item", item)
                    startActivity(intent)
                }
            }

            override fun onPromoteClick(position: Int) {
                var sheet = PromoteChildBottomSheet(
                    this@ChildProfileListActivity,
                    tempList[position],
                    classRoomsList
                )
                sheet.setListener(object : PromoteChildBottomSheet.ClickListener {
                    override fun onPromoteClicked(selectedClassRoom: ClassroomDetails?) {
                        callAPItoPromoteChild(tempList[position], selectedClassRoom)
                    }

                    override fun onCancelClicked() {

                    }
                })
                sheet.show(supportFragmentManager, "PromoteChildBottomSheet")
            }

            override fun onOptionItemClicked(position: Int) {
                showBottomSheet(tempList[position], position)
            }
        })
//        } else {
//            adapter?.setItems(tempList)
//            adapter?.notifyDataSetChanged()
//        }

        if (tempList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }

    private fun init() {
        onSetupViewGroup(findViewById(R.id.content))
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    var tempList = ArrayList<ChildInfo>()
                    tempList.addAll(list)
                    tempList = tempList.filter {
                        (it.firstName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.firstName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.firstName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.firstName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.firstName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.firstName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.lastName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        )
                    }.toArrayList()
                    setRecyclerView(tempList)
                } else {
                    setRecyclerView(list)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun showBottomSheet(childInfo: ChildInfo, position: Int) {
        val bottomFragment =
            AddChildBottomSheetDialogFragment(
                childInfo,
                this@ChildProfileListActivity,
                fromChildListing = true
            )
        bottomFragment.show(
            supportFragmentManager,
            "bottom_sheet"
        )

        bottomFragment.setListener(object : AddChildBottomSheetDialogFragment.ClickListener {
            override fun onPromoteClicked(dialog: BottomSheetDialogFragment) {
                dialog.dismiss()
                var sheet = PromoteChildBottomSheet(
                    this@ChildProfileListActivity,
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
                val intent =
                    Intent(this@ChildProfileListActivity, ChildProfileActivity::class.java)
                intent.putExtra("item", childInfo)
                startActivity(intent)
                bottomFragment.dismiss()

//                startActivity<ChildProfileDetailActivity>("item" to childInfo)
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
                alert.show(this@ChildProfileListActivity)
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
                alert.show(this@ChildProfileListActivity)
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
                alert.show(this@ChildProfileListActivity)
            }

            override fun onCancelClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
            }

            override fun onEditProfileClicked(dialog: BottomSheetDialogFragment) {
                bottomFragment.dismiss()
                var intent = Intent(this@ChildProfileListActivity, AddChildActivity::class.java)
                intent.putExtra("childitem", childInfo)
                intent.putExtra(Constants.child_type, Constants.update_child)
                startActivity(intent)
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
                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
                        callAPItoGetAllChildes()
                        callAPItoGetClassRooms()
                    } else {

                        callAPItoGetChildes()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ChildProfileListActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ChildProfileListActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
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
                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
                        callAPItoGetAllChildes()
                        callAPItoGetClassRooms()
                    } else {

                        callAPItoGetChildes()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ChildProfileListActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ChildProfileListActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
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
                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
                        callAPItoGetAllChildes()
                        callAPItoGetClassRooms()
                    } else {

                        callAPItoGetChildes()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ChildProfileListActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ChildProfileListActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
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
                showErrorToast(
                    this@ChildProfileListActivity,
                    "Please select Classroom to promote!"
                )
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
            alert.show(this@ChildProfileListActivity)
        }

        dialogBinding.ivCross.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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
                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
                        callAPItoGetAllChildes()
                        callAPItoGetClassRooms()
                    } else {

                        callAPItoGetChildes()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ChildProfileListActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ChildProfileListActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetChildes(
    ) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "1")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
                    hideLoading()
                    list = response.body()?.data ?: ArrayList()
//                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    setRecyclerView(list)
                } else {
                    setRecyclerView(list)
                    hideLoading()
//                    showErrorToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ChildProfileListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetProviderChildes(
    ) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesOfNoClass("Bearer " + userManager.accessToken ?: "", "1")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
                    hideLoading()
                    list = response.body()?.data ?: ArrayList()
//                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    setRecyclerView(list)
                } else {
                    setRecyclerView(list)
                    hideLoading()
//                    showErrorToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
                hideLoading()
                setRecyclerView(list)
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetAllChildes(
    ) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getAllChilds("Bearer " + userManager.accessToken ?: "", "1")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
                    hideLoading()
                    list = response.body()?.data ?: ArrayList()
//                    showSuccessToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                    setRecyclerView(list)
                } else {
                    setRecyclerView(list)
                    hideLoading()
//                    showErrorToast(this@ChildProfileListActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ChildProfileListActivity, "Can't Connect to Server!")
                hideLoading()
                setRecyclerView(list)
                t.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
            callAPItoGetAllChildes()
        } else {
            callAPItoGetChildes()
        }
    }
}