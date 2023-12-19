package com.android.swaddle.fragments.providers


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.parent.ChildProfileListActivity
import com.android.swaddle.activities.parent.MedicalReportActivity
import com.android.swaddle.activities.parent.ParentDailyActivity
import com.android.swaddle.activities.providers.AddActivity
import com.android.swaddle.activities.providers.ProviderChildHelperActivity
import com.android.swaddle.activities.providers.StaffActivity
import com.android.swaddle.activities.providers.reports.ChildIncidentReportActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.activities.providers.reports.IncidentReportActivity
import com.android.swaddle.adapters.notification_adapter.NotificationsAdapter
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.FragmentNotificationBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.NotificationDetail
import com.android.swaddle.models.NotificationsResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import kotlinx.android.synthetic.main.no_data_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : BaseFragment(), NotificationsAdapter.NotificationInterface {
    var adapter: NotificationsAdapter? = null
    private lateinit var binding: FragmentNotificationBinding
    val userManager: LoginData
        get() {
            return UserData.user(requireContext())
        }
    var list = ArrayList<NotificationDetail>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        callAPItoGetNotifications()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callAPItoGetNotifications()
        clickListeners()
    }

    private fun clickListeners() {
        binding.noData.retryBtn.setOnClickListener {
            mActivity.showLoading()
            callAPItoGetNotifications()
        }
    }

    private fun callAPItoGetNotifications(
    ) {

        val call: Call<NotificationsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getNotifications("Bearer " + mActivity.userManager.accessToken ?: "")
        call.enqueue(object : Callback<NotificationsResponse> {
            override fun onResponse(
                call: Call<NotificationsResponse>,
                response: Response<NotificationsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                if (response.body()?.data != null)
                    list = response.body()?.data!!
                mActivity.hideLoading()
//                classRoomsList = response.body()?.data ?: ArrayList()
                setRecyclerView()
            }

            override fun onFailure(
                call: Call<NotificationsResponse>,
                t: Throwable
            ) {
//                showErrorToast(t, "Can't Connect to Server!")
                mActivity.hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            adapter = NotificationsAdapter(mActivity, list, this)
            binding.recNotification.layoutManager = manager
            binding.recNotification.adapter = adapter
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

    override fun onNotificationClicked(position: Int) {
//        parent, provider, staff,authorized_adult
        when (list[position].type) {
//            "activity" -> {
//                if (mActivity.userManager.user?.type == "parent"
//                    || mActivity.userManager.user?.type == "authorized_adult"
//                ) {
//                    startActivity(Intent(context, ParentDailyActivity::class.java))
//                } else if (mActivity.userManager.user?.type == "provider"
//                    || mActivity.userManager.user?.type == "staff"
//                ) {
//                    startActivity(Intent(context, AddActivity::class.java))
//                }
//            }
//            "invoice" -> {
//                if (mActivity.userManager.user?.type == "parent"
//                    || mActivity.userManager.user?.type == "authorized_adult"
//                ) {
//                    val intent = Intent(context, ParentPaymentMainActivity::class.java)
//                    startActivity(intent)
//                } else if (mActivity.userManager.user?.type == "provider"
//                    || mActivity.userManager.user?.type == "staff"
//                ) {
//                    val intent = Intent(context, ParentPaymentMainActivity::class.java)
//                    intent.putExtra("type", "provider")
//                    startActivity(intent)
//                }
//            }
//            "daily_report" -> {
//                if (mActivity.userManager.user?.type == "parent"
//                    || mActivity.userManager.user?.type == "authorized_adult"
//                ) {
//                    startActivity(Intent(context, DailyReportActivity::class.java))
//                } else if (mActivity.userManager.user?.type == "provider"
//                    || mActivity.userManager.user?.type == "staff"
//                ) {
//                    startActivity(Intent(context, DailyReportActivity::class.java))
//                }
//            }
//            "incident_report" -> {
//                if (mActivity.userManager.user?.type == "parent"
//                    || mActivity.userManager.user?.type == "authorized_adult"
//                ) {
//                    val intent = Intent(context, ChildIncidentReportActivity::class.java)
//                    startActivity(intent)
//                } else if (mActivity.userManager.user?.type == "provider"
//                    || mActivity.userManager.user?.type == "staff"
//                ) {
//                    val intent = Intent(context, IncidentReportActivity::class.java)
//                    startActivity(intent)
//                }
//            }


            /*---------------------------------*/

            "activity" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), ParentDailyActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), AddActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                }
            }
            "invoice" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), ParentPaymentMainActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), ParentPaymentMainActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "daily_report" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "incident_report" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), ChildIncidentReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), IncidentReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "parent_paid_to_provider" -> {
                val sintent = Intent(requireContext(), ParentPaymentMainActivity::class.java)
                sintent.putExtra("classroom_id", list[position].classroom_id)
                sintent.putExtra("child_id", list[position].child_id)
                startActivity(sintent)
            }
            "medical_information_updated" -> {//done
                val sintent = Intent(requireContext(), MedicalReportActivity::class.java)
                sintent.putExtra("classroom_id", list[position].classroom_id)
                sintent.putExtra("child_id", list[position].child_id)
                startActivity(sintent)
            }
            "medical_information_deleted" -> {//done
                val sintent = Intent(requireContext(), MedicalReportActivity::class.java)
                sintent.putExtra("classroom_id", list[position].classroom_id)
                sintent.putExtra("child_id", list[position].child_id)
                startActivity(sintent)
            }
            "update_child_attendance" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "child_attendance_signin" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "staff_attendance_signin" -> {//done
                if (userManager.user?.type == Constants.provider
                ) {
                    val sintent = Intent(context, StaffActivity::class.java)
                    startActivity(sintent)
                }
            }
            "child_attendance_signout" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
            "center_closing_alert" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
                else{
                    val sintent = Intent(requireContext(), ProviderChildHelperActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                }
            }
            "child_created" ->{
                val intent = Intent(requireContext(), ChildProfileListActivity::class.java)
                startActivity(intent)
            }
            "todays_activity_missing" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), ParentDailyActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), AddActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                }
            }
            "child_signed_out_before_dailyReport" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
                else{
                    val sintent = Intent(requireContext(), ProviderChildHelperActivity::class.java)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    startActivity(sintent)
                }
            }
            "provide_missing_daiylyReport_items" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(requireContext(), DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", list[position].classroom_id)
                    sintent.putExtra("child_id", list[position].child_id)
                    startActivity(sintent)
                }
            }
        }
    }
}