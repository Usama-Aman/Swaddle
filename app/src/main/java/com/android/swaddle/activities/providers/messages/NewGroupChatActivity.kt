package com.android.swaddle.activities.providers.messages

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.adapters.provider_adapter.NewGroupChatUsersAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.toArrayList
import com.android.swaddle.databinding.ActivityNewGroupChatBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.User
import com.android.swaddle.models.UsersForChatsResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NewGroupChatActivity : BaseActivity() {
    var list = ArrayList<User>()
    var filteredList = ArrayList<User>()

    private lateinit var binding: ActivityNewGroupChatBinding
    private var adapter: NewGroupChatUsersAdapter? = null

    companion object {
        var newGroupChatActivity: NewGroupChatActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        callAPItoGetUsersForChat()
        listener()
    }

    private fun listener() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, MakeNewGroupActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.noData.retryBtn.setOnClickListener {
            callAPItoGetUsersForChat()
        }
        binding.btnNext.setOnClickListener {
            var items = filteredList.filter { it.isUserSelected } as ArrayList
            var members = ArrayList<String>()
            items.forEachIndexed { _, user ->
                if (user.isUserSelected) {
                    members.add("${user.id ?: 0}")
                }
            }

            if (members.size < 1) {
                CustomToasts.failureToast(
                    this@NewGroupChatActivity,
                    "Please select at lease one member to start chat!"
                )
                return@setOnClickListener
            }

//            if (userManager.user?.type == Constants.parent || userManager.user?.type == Constants.staff)
//                if (!members.contains(userManager.user?.linkedTo.toString()))
//                    members.add(userManager.user?.linkedTo.toString())

            Log.e("str", "${members.joinToString { "," }}")
            Log.e("str", "${TextUtils.join(",", members)}")

            startActivity<MakeNewGroupActivity>(
                "newChat" to true,
                "userId" to if (members.size == 1) {
                    members.first()
                } else {
                    "${TextUtils.join(",", members)}"
                },
                "type" to "group"
            )
            finish()
        }
    }

    private fun init() {
        newGroupChatActivity = this@NewGroupChatActivity

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    filteredList = list.filter {
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
                    setRecyclerView(filteredList)
                } else {
                    setRecyclerView(filteredList)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun setRecyclerView(tempList: ArrayList<User>) {
        if (adapter == null) {
            val manager =
                LinearLayoutManager(
                    this@NewGroupChatActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = NewGroupChatUsersAdapter(this@NewGroupChatActivity, tempList, object :
                NewGroupChatUsersAdapter.ClickListener {
                override fun onItemClick(pos: Int) {
                }

                override fun onCheckBoxClick(pos: Int) {
                    tempList[pos].isUserSelected = !tempList[pos].isUserSelected
                    adapter?.notifyDataSetChanged()
                }

                override fun onProfileImageClick(pos: Int) {

                }
            })
            binding.recChat.layoutManager = manager
            binding.recChat.adapter = adapter
        } else {
            adapter?.setData(tempList)
            adapter?.notifyDataSetChanged()
        }

        if (tempList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }

    private fun callAPItoGetUsersForChat(
    ) {
        val call: Call<UsersForChatsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getAllUsersForChat("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<UsersForChatsResponse> {
            override fun onResponse(
                call: Call<UsersForChatsResponse>,
                response: Response<UsersForChatsResponse>
            ) {
                hideLoading()
                if (response.body()?.data != null) {
                    list = response.body()?.data!!
                    filteredList = response.body()?.data!!
                    setRecyclerView(filteredList)
                }
            }

            override fun onFailure(
                call: Call<UsersForChatsResponse>,
                t: Throwable
            ) {
                hideLoading()
                t.printStackTrace()
            }
        })
    }

}