package com.android.swaddle.activities.providers.class_room_ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.providers.class_room_ui.CreateClassRoom.Companion.createClassRoom
import com.android.swaddle.adapters.provider_adapter.SelectChildrenAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.toArrayList
import com.android.swaddle.databinding.ActivitySelectChildrensBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ChildesResponse
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.networkManager.RetrofitClass
import kotlinx.android.synthetic.main.no_data_layout.view.*
import kotlinx.android.synthetic.main.progress.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class SelectChildrenActivity : BaseActivity(), SelectChildrenAdapter.ClickListener {
    private lateinit var binding: ActivitySelectChildrensBinding
    private var adapter: SelectChildrenAdapter? = null
    private var list = ArrayList<ChildInfo>()
    var name = ""
    var desc = ""
    var type = ""
    var classRoomId = ""
    var selectedIds = ArrayList<String>()

    var classRoomDetail: ClassroomDetails? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectChildrensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        name = intent?.getStringExtra("name") ?: ""
        desc = intent?.getStringExtra("desc") ?: ""
        type = intent?.getStringExtra("type") ?: ""
        classRoomId = intent?.getStringExtra("room_id") ?: ""
        classRoomDetail = intent.getSerializableExtra("item") as ClassroomDetails?

        init()
        showLoading()
        callAPItoGetAllChildes()
        setRecyclerView(list)
        listener()
    }

    private fun setRecyclerView(data: ArrayList<ChildInfo>) {
        data.forEachIndexed { index, childInfo ->
            data[index].isSelected = classRoomDetail?.childs?.any { it.id == childInfo.id }
        }

        if (adapter == null) {
            val manager = LinearLayoutManager(this)
            binding.rvChildren.layoutManager = manager
            adapter = SelectChildrenAdapter(this, data, this, classRoomDetail)
            binding.rvChildren.adapter = adapter
        } else {
            adapter?.setItems(data)
            adapter?.notifyDataSetChanged()
        }

        if (list.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }

//        var tempList = ArrayList<ChildInfo>()
//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//                if (s.toString() == "") {
//                    tempList.clear()
//                    tempList.addAll(list)
//                    adapter?.setItems(tempList)
//                    adapter?.notifyDataSetChanged()
//                } else {
//                    tempList.clear()
//                    Log.e("size1", tempList.size.toString())
//                    tempList.addAll(list.filter {
//                        it.firstName?.contains(
//                            s.toString(),
//                            ignoreCase = true
//                        ) ?: false || it.lastName?.contains(
//                            s.toString(),
//                            ignoreCase = true
//                        ) ?: false || it.middleName?.contains(
//                            s.toString(),
//                            ignoreCase = true
//                        ) ?: false || it.nickName?.contains(
//                            s.toString(),
//                            ignoreCase = true
//                        ) ?: false
//                    })
//                    Log.e("size2", tempList.size.toString())
//                    adapter?.setItems(tempList)
//                    adapter?.notifyDataSetChanged()
//                }
//            }
//        })
    }

    private fun listener() {

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

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
        })

        binding.noData.retryBtn.setOnClickListener { callAPItoGetAllChildes() }

        if (type == "create") {
            binding.btnCreate.setOnClickListener {
                if (validation()) {
                    callAPItoCreateClassRoom()
                }
            }
        } else {

            binding.tvCreate.text = "Update"

            binding.btnCreate.setOnClickListener {
                if (validation()) {
                    callAPItoCreateClassRoom()
                }
            }
        }


        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun validation(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@SelectChildrenActivity, "No internet connection!")
            return false
        }

        list.forEachIndexed { index, childInfo ->
            if (childInfo.isSelected == true) {
                selectedIds.add((childInfo.id ?: 0).toString())
            }
        }

        if (selectedIds.size < 1) {
            showErrorToast(
                this@SelectChildrenActivity,
                "Please select at least one child tp create Classroom!"
            )
            return false
        }
        return true
    }

    private fun callAPItoCreateClassRoom(
    ) {
        Log.i("jname", name)

        var ids = StringBuilder()
        selectedIds.forEachIndexed { index, s ->
            if (index == selectedIds.size - 1) {
                ids.append(s)
            } else {
                ids.append("$s, ")
            }
        }
        Log.e("selectedIds", ids.toString())
        showProgressBar()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .createOrUpdateClassRoom(
                "Bearer " + userManager.accessToken ?: "",
                classRoomId,
                name,
                desc,
                "[${ids}]"
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@SelectChildrenActivity, response.body()?.message ?: "")
                    hideProgressBar()
                    createClassRoom?.finish()
                    finish()

                } else {
                    hideProgressBar()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@SelectChildrenActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@SelectChildrenActivity, response.body()?.message ?: "")
                    }
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@SelectChildrenActivity, "Can't Connect to Server!")
                hideProgressBar()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetAllChildes(
    ) {
        hideLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getAllChilds("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                if (response.body()?.status == true) {
//                    showSuccessToast(this@SelectChildrenActivity, "Classroom Created Successfully")
                    hideLoading()
                    list = response.body()?.data ?: ArrayList()
                    setRecyclerView(list)
                } else {
                    setRecyclerView(list)
                    hideLoading()
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@SelectChildrenActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun init() {

    }


    private fun showProgressBar() {
        binding.tvCreate.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvCreate.viewVisible()
        binding.progressbar.viewGone()
    }

    override fun onItemCheckedChange(position: Int, item: ChildInfo) {
        if (list[position].isSelected == true) {
            list[position].isSelected = false
            adapter?.notifyItemChanged(position)
//            selectedIds.remove((item.id ?: 0).toString())
        } else {
            list[position].isSelected = true
            adapter?.notifyItemChanged(position)
//            selectedIds.add((item.id ?: 0).toString())
        }
//        selectedIds = ArrayList()
//        list.forEachIndexed { index, childInfo ->
//            if (childInfo.isSelected == true) {
//                selectedIds.add((childInfo.id ?: 0).toString())
//            }
//        }
    }

    override fun onItemChecked(position: Int, item: ChildInfo) {
        if (list[position].isSelected == true) {
            list[position].isSelected = false
            adapter?.notifyItemChanged(position)
//            selectedIds.remove((item.id ?: 0).toString())
        } else {
            list[position].isSelected = true
            adapter?.notifyItemChanged(position)
//            selectedIds.add((item.id ?: 0).toString())
        }
//        var isChecked = item.isSelected ?: false
//        list.forEachIndexed { index, item ->
//            list[index].isSelected = false
//        }
//        list[position].isSelected = !isChecked
//        adapter?.setItems(list)
//        adapter?.notifyDataSetChanged()

    }
}