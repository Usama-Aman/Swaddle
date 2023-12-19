package com.android.swaddle.fragments.parent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.activities.common.SplashActivity
import com.android.swaddle.activities.common.SummaryReportActivity
import com.android.swaddle.activities.common.TransactionHistoryActivity
import com.android.swaddle.activities.parent.*
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.activities.providers.RelationshipsActivity
import com.android.swaddle.activities.providers.SettingActivity
import com.android.swaddle.activities.providers.other_reg_screens.ProviderRegistrationActivity
import com.android.swaddle.activities.providers.other_reg_screens.StaffRegistrationActivity
import com.android.swaddle.activities.providers.reports.ChildIncidentReportActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.FragmentParentMoreBinding
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.prefrences.UserData
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import org.jetbrains.anko.startActivity


class ParentMoreFragment : BaseFragment() {
    private lateinit var binding: FragmentParentMoreBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentParentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVar()
        listeners()
    }

    private fun listeners() {

        binding.lnrChildren.setOnClickListener {
//            if (mActivity.userManager.user?.type == "authorized_adult") {
//                mActivity.startActivity<GetCallYouWithChildForAuthorizedAdults>("showBack" to true)
//            } else {
            startActivity(Intent(context, ChildProfileListActivity::class.java))
//            }
        }
        binding.lnrIncidentReport.setOnClickListener {
            val intent = Intent(context, ChildIncidentReportActivity::class.java)
            startActivity(intent)
        }

        binding.lnrAttendance.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java)
            intent.putExtra("fromAttendance", true)
            startActivity(intent)
        }

        binding.lnrCommunication.setOnClickListener {
            (activity as ProviderMainActivity).gotoMessages()
        }

        binding.lnrMedical.setOnClickListener {
            startActivity(Intent(context, MedicalReportActivity::class.java))
        }

        binding.llDailyReport.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java)
            startActivity(intent)
        }

        binding.lnrActivity.setOnClickListener {
            startActivity(Intent(context, ParentDailyActivity::class.java))
        }

        binding.lnrRelation.setOnClickListener {
            startActivity(Intent(context, RelationshipsActivity::class.java))
        }

        binding.lnrCenterInfo.setOnClickListener {
            startActivity(Intent(context, CenterInformationActivity::class.java))
        }

        binding.lnrAccountSetting.setOnClickListener {
            val intent = Intent(context, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.lnrPayment.setOnClickListener {
            val intent = Intent(context, ParentPaymentMainActivity::class.java)
            startActivity(intent)
        }
        binding.lnrTransactionHistory.setOnClickListener {
            val intent = Intent(context, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.lnrSummaryReport.setOnClickListener {
            startActivity(Intent(context, SummaryReportActivity::class.java))

        }

        binding.lnrProfile.setOnClickListener {
            var user = mActivity.userManager.user

            when (user?.type ?: "") {
                "provider" -> {
                    mActivity.startActivity<ProviderRegistrationActivity>("item" to user)
                }
                "parent" -> {
                    mActivity.startActivity<ParentSignUpActivity>("item" to user)
                }
                "staff" -> {
                    mActivity.startActivity<StaffRegistrationActivity>("item" to user)
                }
                "authorized_adult" -> {
                    mActivity.startActivity<ParentSignUpActivity>("item" to user)
                }
            }
//            val intent = Intent(context, MyProfileActivity::class.java)
//            intent.putExtra("user", mActivity.userManager.user)
//            startActivity(intent)
        }

        binding.llLogout.setOnClickListener {
            val alert =
                AlertView("Logout?", "Are you sure you want to logout?", AlertStyle.DIALOG)
            alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {

                UserData.setUserLogin(mActivity, false)
                mActivity.startActivity<SplashActivity>()
            })
            alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
            })
            alert.show(mActivity)
        }
    }

    private fun initVar() {

    }
}