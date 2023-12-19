package com.android.swaddle.fragments.providers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.providers.messages.ChatActivity
import com.android.swaddle.activities.providers.messages.NewMessageActivity
import com.android.swaddle.adapters.provider_adapter.ChatsAdapter
import com.android.swaddle.baseClasses.BaseApplication
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.baseClasses.toArrayList
import com.android.swaddle.databinding.FragmentMessageBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.IOSocketManager

import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.networkManager.SocketNames
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.userManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagesFragment : BaseFragment(), ChatsAdapter.ClickListener {
    private lateinit var binding: FragmentMessageBinding
    private var adapter: ChatsAdapter? = null
    var list = ArrayList<Chat>()
    public var sharedSocket = IOSocketManager()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        callAPItoGetChats()
        listener()
        socketReceiveMessage()
    }

    private fun listener() {
//        if (mActivity.userManager.user?.type == Constants.parent || mActivity.userManager.user?.type == Constants.authorized_adult ) {
//            binding.ivEdit.viewGone()
//        } else {
//            binding.ivEdit.viewVisible()
//        }

        binding.ivEdit.setOnClickListener {
            val intent = Intent(activity, NewMessageActivity::class.java)
            startActivity(intent)
        }

        binding.noData.retryBtn.setOnClickListener {
            mActivity.showLoading()
            callAPItoGetChats()
        }
    }

    private fun socketReceiveMessage() {
        sharedSocket.init(SocketNames.socketReceiveMessage) { obj ->
            Log.e("socketReceiveMessage", obj.toString())
            try {
                if (!BaseApplication.isChatScreenOpen)
                    callAPItoGetChats()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    private fun init() {
        if (UserData.getUserType(requireContext()) == "staff") {
            binding.ivEdit.viewGone()
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    var tempList = ArrayList<Chat>()
                    tempList.addAll(list)
                    tempList = tempList.filter {
                        (it.members?.first()?.user?.firstName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.first()?.user?.firstName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.first()?.user?.firstName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.first()?.user?.lastName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.first()?.user?.lastName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.first()?.user?.lastName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.firstName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.firstName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.firstName ?: "").endsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.lastName ?: "").contains(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.lastName ?: "").startsWith(
                            s.toString(),
                            ignoreCase = true
                        ) || (it.members?.last()?.user?.lastName ?: "").endsWith(
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

    override fun onResume() {
        super.onResume()
        callAPItoGetChats()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onItemClick(pos: Int, item: Chat) {

        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("chat_id", "${item.id}")
        intent.putExtra("main_name", item.mainTitle)
        startActivity(intent)
    }

    private fun callAPItoGetChats(
    ) {

        val call: Call<ChatsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChats("Bearer " + mActivity.userManager.accessToken ?: "")
        call.enqueue(object : Callback<ChatsResponse> {
            override fun onResponse(
                call: Call<ChatsResponse>,
                response: Response<ChatsResponse>
            ) {

                if (response.body()?.data != null)
                    list = response.body()?.data!!
                mActivity.hideLoading()
                setRecyclerView(list)
            }

            override fun onFailure(
                call: Call<ChatsResponse>,
                t: Throwable
            ) {
                mActivity.hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRecyclerView(mList: ArrayList<Chat>) {
        var testList = ArrayList<Chat>()
        testList.addAll(mList)
        testList.reverse()
        if (adapter == null) {
            val manager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            adapter = ChatsAdapter(mActivity, testList, this)
            binding.recView.layoutManager = manager
            binding.recView.adapter = adapter
        } else {
            adapter?.setData(testList)
            adapter?.notifyDataSetChanged()
        }

        if (testList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }
}