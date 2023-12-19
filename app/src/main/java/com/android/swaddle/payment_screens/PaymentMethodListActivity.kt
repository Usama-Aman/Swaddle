package com.android.swaddle.payment_screens

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.swaddle.adapters.payment_adapters.PaymentMethodListAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityPaymentMethodeListBinding
import com.android.swaddle.fragments.bottomSheetFragment.NewPaymentBottomSheet
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.CardData
import com.android.swaddle.models.PaymentCardsResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import kotlinx.android.synthetic.main.no_data_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentMethodListActivity : BaseActivity(), PaymentMethodListAdapter.ClickListener {
    private lateinit var binding: ActivityPaymentMethodeListBinding
    var adapter: PaymentMethodListAdapter? = null
    var list = ArrayList<CardData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentMethodeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()

        listener()

        showLoading()
        callAPItoGetPaymentMethods()

    }

    private fun listener() {

        binding.swipeRefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding.swipeRefresh.setRefreshing(true)
                callAPItoGetPaymentMethods()
            }

        })
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivAdd.setOnClickListener {
            showBottomSheet()
        }
        binding.noData.retryBtn.setOnClickListener {
            showLoading()
            callAPItoGetPaymentMethods()
        }
    }

    private fun showBottomSheet() {
        val bottomFragment: NewPaymentBottomSheet =
            NewPaymentBottomSheet()
        bottomFragment.show(
            supportFragmentManager,
            "bottom_sheet"
        )
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            adapter = PaymentMethodListAdapter(this@PaymentMethodListActivity, list, this)
            binding.rec.adapter = adapter
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

    private fun initVar() {

    }

    private fun callAPItoGetPaymentMethods() {
        val call: Call<PaymentCardsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getPaymentMethods("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<PaymentCardsResponse> {
            override fun onResponse(
                call: Call<PaymentCardsResponse>,
                response: Response<PaymentCardsResponse>
            ) {
                binding.swipeRefresh.isRefreshing = false
                hideLoading()
                list = response.body()?.data ?: ArrayList()
                setRecyclerView()
            }

            override fun onFailure(
                call: Call<PaymentCardsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@PaymentMethodListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoMakeDefault(position: Int, item: CardData) {
        val call: Call<PaymentCardsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .makeDefaultPaymentMethod(
                "Bearer " + userManager.accessToken ?: "",
                (item.id ?: 0).toString()
            )
        call.enqueue(object : Callback<PaymentCardsResponse> {
            override fun onResponse(
                call: Call<PaymentCardsResponse>,
                response: Response<PaymentCardsResponse>
            ) {
                if (response.isSuccessful) {
                    callAPItoGetPaymentMethods()
                    showSuccessToast(this@PaymentMethodListActivity, response.body()?.message ?: "")
                } else {
                    showErrorToast(this@PaymentMethodListActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<PaymentCardsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@PaymentMethodListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoDeleteCard(position: Int, item: CardData) {
        val call: Call<PaymentCardsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deletedCard(
                "Bearer " + userManager.accessToken ?: "",
                (item.id ?: 0).toString()
            )
        call.enqueue(object : Callback<PaymentCardsResponse> {
            override fun onResponse(
                call: Call<PaymentCardsResponse>,
                response: Response<PaymentCardsResponse>
            ) {
                if (response.isSuccessful) {
                    callAPItoGetPaymentMethods()
                    showSuccessToast(this@PaymentMethodListActivity, response.body()?.message ?: "")
                } else {
                    showErrorToast(this@PaymentMethodListActivity, response.body()?.message ?: "")
                }
            }

            override fun onFailure(
                call: Call<PaymentCardsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@PaymentMethodListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        callAPItoGetPaymentMethods()
    }

    override fun onITemClick(position: Int, item: CardData) {

    }

    override fun onDeleteClick(position: Int, item: CardData) {
        if (item.isDefault == 1) {
            showErrorToast(this@PaymentMethodListActivity, "Default Card can't be deleted!")
            return
        }
        val alert =
            AlertView(
                "Delete Card?",
                "Are you sure you want to Delete this card?",
                AlertStyle.DIALOG
            )
        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
            callAPItoDeleteCard(position, item)
        })
        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
        })
        alert.show(this@PaymentMethodListActivity)
    }

    override fun onMakeDefaultClick(position: Int, item: CardData) {
        val alert =
            AlertView(
                "Make Default?",
                "Are you sure you want to make this card default?",
                AlertStyle.DIALOG
            )
        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
            callAPItoMakeDefault(position, item)
        })
        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
        })
        alert.show(this@PaymentMethodListActivity)

    }
}