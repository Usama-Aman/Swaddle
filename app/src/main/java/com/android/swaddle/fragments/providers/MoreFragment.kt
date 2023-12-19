package com.android.swaddle.fragments.providers


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.activities.common.InvitePeopleActivity
import com.android.swaddle.activities.common.SplashActivity
import com.android.swaddle.activities.common.SummaryReportActivity
import com.android.swaddle.activities.common.TransactionHistoryActivity
import com.android.swaddle.activities.parent.CenterInformationActivity
import com.android.swaddle.activities.parent.ChildProfileListActivity
import com.android.swaddle.activities.parent.MedicalReportActivity
import com.android.swaddle.activities.providers.*
import com.android.swaddle.activities.providers.class_room_ui.ClassListActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.activities.providers.reports.IncidentReportActivity
import com.android.swaddle.activities.providers.reports.ProviderAttendanceActivity
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.FragmentMoreBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.onesignal.OneSignalNotificationManager
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.CustomToasts
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import org.jetbrains.anko.startActivity


class MoreFragment : BaseFragment() {
    private lateinit var binding: FragmentMoreBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        listeners()
    }

    private fun listeners() {
        binding.lnrChildren.setOnClickListener {
            val intent = Intent(context, ChildProfileListActivity::class.java)
            startActivity(intent)
        }

        binding.lnrCenterInfo.setOnClickListener {
            startActivity(Intent(context, CenterInformationActivity::class.java))
        }

        binding.lnrDailyReport.setOnClickListener {
            if (mActivity.isNetworkConnected()) {
                startActivity(Intent(context, DailyReportActivity::class.java))
            } else {
                CustomToasts.failureToast(mActivity, "No internet connection!")
            }
        }

        binding.lnrAttendance.setOnClickListener {
            if (mActivity.isNetworkConnected()) {
//                startActivity(Intent(context, ProviderAttendanceActivity::class.java))
                startActivity(Intent(context, ProviderChildHelperActivity::class.java))
            } else {
                CustomToasts.failureToast(mActivity, "No internet connection!")
            }
        }

        binding.lnrActivity.setOnClickListener {
            startActivity(Intent(context, AddActivity::class.java))
        }

        binding.lnrCommunication.setOnClickListener {
            (activity as ProviderMainActivity).gotoMessages()
        }

        binding.lnrClassRooms.setOnClickListener {
            val intent = Intent(context, ClassListActivity::class.java)
            startActivity(intent)

        }

        binding.lnrRelation.setOnClickListener {
            val intent = Intent(context, RelationshipsActivity::class.java)
            startActivity(intent)
        }

        binding.lnrSummaryReport.setOnClickListener {
            val intent = Intent(context, SummaryReportActivity::class.java)
            startActivity(intent)
        }

        binding.lnrStaff.setOnClickListener {
            val intent = Intent(context, StaffActivity::class.java)
            startActivity(intent)
        }
        binding.lnrProfile.setOnClickListener {
            val intent = Intent(context, MyProfileActivity::class.java)
            intent.putExtra("user", mActivity.userManager.user)
            intent.putExtra("canNotEdit", false)
            startActivity(intent)
        }

        binding.lnrPayment.setOnClickListener {
            val intent = Intent(context, ParentPaymentMainActivity::class.java)
            intent.putExtra("type", "provider")
            startActivity(intent)
        }
        binding.lnrTransactionHistory.setOnClickListener {
            val intent = Intent(context, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.lnrAccountSetting.setOnClickListener {
            val intent = Intent(context, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.lnrInvite.setOnClickListener {
            val intent = Intent(context, InvitePeopleActivity::class.java)
            startActivity(intent)
        }

        binding.lnrIncidentReport.setOnClickListener {
            val intent = Intent(context, IncidentReportActivity::class.java)
            startActivity(intent)
        }

        binding.lnrMedical.setOnClickListener {
            if (mActivity.isNetworkConnected()) {
                startActivity(Intent(context, MedicalReportActivity::class.java))
            } else {
                CustomToasts.failureToast(mActivity, "No internet connection!")
            }
        }

        binding.llLogout.setOnClickListener {
            val alert =
                AlertView("Logout?", "Are you sure you want to logout?", AlertStyle.DIALOG)
            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                UserData.setUserLogin(mActivity, false)
                OneSignalNotificationManager.removeUserTags()
                mActivity.startActivity<SplashActivity>()

            })
            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
            })
            alert.show(mActivity)
        }
    }

    private fun init() {
        if (mActivity.userManager.user?.type == "staff") {
            binding.viewStaff.viewGone()
            binding.lnrStaff.viewGone()
            binding.lnrPayment.viewGone()
            binding.viewPayment.viewGone()
        } else {
            binding.viewStaff.viewVisible()
            binding.lnrStaff.viewVisible()
            binding.lnrPayment.viewVisible()
            binding.viewPayment.viewVisible()
        }


    }


}