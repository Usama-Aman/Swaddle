package com.android.swaddle.activities.parent

import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.providers.ProviderMainActivity

import com.android.swaddle.adapters.parent_adapters.CallsYouListAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityCallsYouBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ChildesResponse
import com.android.swaddle.models.LoginResponse

import com.android.swaddle.networkManager.RetrofitClass
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetCallYouWithChildForAuthorizedAdults : BaseActivity(),
    CallsYouListAdapter.ItemClickListener {
    private lateinit var binding: ActivityCallsYouBinding
    private var adapter: CallsYouListAdapter? = null
    var childesList = ArrayList<ChildInfo>()
    var showBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallsYouBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showBack = intent.getBooleanExtra("showBack", false)
        init()
        callAPItoGetParentsChildes()
        if (showBack) {
            binding.imgBack.viewVisible()
        } else {
            binding.imgBack.viewGone()
        }
        listener()
    }


    override fun onResume() {
        super.onResume()
        callAPItoGetParentsChildes()
    }

    private fun callAPItoGetParentsChildes() {
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {

                hideLoading()
                childesList = response.body()?.data ?: ArrayList()
                setRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showErrorToast(
                    this@GetCallYouWithChildForAuthorizedAdults,
                    "Can't Connect to Server!"
                )
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoUpdateCallsYou(ids: ArrayList<String>, callsYou: ArrayList<String>) {
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .submitCallsYou("Bearer " + userManager.accessToken ?: "", ids, callsYou)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

                hideLoading()
                hideProgressBar()
                if (response.body()?.status == true) {
                    startActivity<ProviderMainActivity>()
                } else {

                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                showErrorToast(
                    this@GetCallYouWithChildForAuthorizedAdults,
                    "Can't Connect to Server!"
                )
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(this)
            binding.recViewClassList.layoutManager = manager
            adapter =
                CallsYouListAdapter(this@GetCallYouWithChildForAuthorizedAdults, childesList, this)
            binding.recViewClassList.adapter = adapter
        } else {
            adapter?.setItems(childesList)
            adapter?.notifyDataSetChanged()
        }

        if (childesList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }
    }

    private fun listener() {
        binding.noData.retryBtn.setOnClickListener { callAPItoGetParentsChildes() }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.tvUpdate.setOnClickListener {
            var ids = ArrayList<String>()
            var callsYou = ArrayList<String>()
            childesList.forEachIndexed { index, childInfo ->
                ids.add("${childInfo.id ?: 0}")
                callsYou.add(childInfo.callsYou ?: "")
                Log.e("id", "${childInfo.id ?: 0} CallsYou:${childInfo.callsYou}")
            }
            if (validate()) {
                showProgressBar()
                callAPItoUpdateCallsYou(ids, callsYou)
            }
        }
    }

    private fun init() {

    }

    override fun onItemClick(position: Int, item: ChildInfo) {
        val intent =
            Intent(this@GetCallYouWithChildForAuthorizedAdults, ChildProfileActivity::class.java)
        intent.putExtra("item", item)
        startActivity(intent)
    }

    override fun onSubmitClick(position: Int, item: ChildInfo) {

    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@GetCallYouWithChildForAuthorizedAdults, "No internet connection!")
            return false
        }
        return true
    }

    private fun showProgressBar() {
        binding.tvUpdate.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvUpdate.viewVisible()
        binding.progressbar.viewGone()
    }
}