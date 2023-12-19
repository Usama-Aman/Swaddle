package com.android.swaddle.activities.providers.messages

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.provider_adapter.UsersForNewChatAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.toArrayList
import com.android.swaddle.databinding.ActivityNewMessageBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.User
import com.android.swaddle.models.UsersForChatsResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NewMessageActivity : BaseActivity() {
    private var adapter: UsersForNewChatAdapter? = null
    private lateinit var binding: ActivityNewMessageBinding

    var list = ArrayList<User>()

    companion object {
        var newMessageActivity: NewMessageActivity? = null
        const val ImageSelectionRequestCode = 9008
        const val MediaDisplay = 9009
        const val MediaConfirmCode = 9010
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        callAPItoGetUsersForChat()
        listener()
    }

    private fun listener() {
        binding.noData.retryBtn.setOnClickListener {
            callAPItoGetUsersForChat()
        }
        binding.lnrGroupChat.setOnClickListener {
            val intent = Intent(this, NewGroupChatActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun init() {
        newMessageActivity = this@NewMessageActivity
        onSetupViewGroup(findViewById(R.id.content))
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    var tempList = ArrayList<User>()
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

    private fun setRecyclerView(tempList: ArrayList<User>) {
        if (adapter == null) {
            val manager =
                LinearLayoutManager(this@NewMessageActivity, LinearLayoutManager.VERTICAL, false)
            adapter = UsersForNewChatAdapter(this@NewMessageActivity, tempList)
            binding.recChat.layoutManager = manager
            binding.recChat.adapter = adapter
            adapter?.setListener(object : UsersForNewChatAdapter.ClickListener {
                override fun onItemClick(pos: Int) {
                    finish()
                    val mainName = if (userManager.user?.type == Constants.parent && list[pos].type == Constants.staff)
                        "${userManager.user?.firstName} & ${list[pos].firstName}"
                    else
                        "${list[pos].firstName} ${list[pos].lastName}"


                    startActivity<ChatActivity>(
                        "newChat" to true,
                        "userId" to "${list[pos].id}",
                        "type" to "single",
                        "main_name" to mainName,
                        "isReceiverStaff" to (list[pos].type == Constants.staff),
                        "title" to "${userManager.user?.firstName} & ${list[pos].firstName}"
                    )
                }

                override fun onProfileImageClick(pos: Int) {

                }
            })
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
                if (response.body()?.data != null)
                    list = response.body()?.data!!
                hideLoading()
                setRecyclerView(list)
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

    private fun validate(): Boolean {
//        if (!isNetworkConnected()) {
//            CustomToasts.failureToast(this@NewMessageActivity, "No internet connection!")
//            return false
//        }
//        if (binding.etMessage.text.toString().trim() == "" && selectedFilePath == "") {
//            CustomToasts.failureToast(
//                this@ChatActivity,
//                "Please type a valid message oy select media!"
//            )
//            return false
//        }
        return true
    }
}