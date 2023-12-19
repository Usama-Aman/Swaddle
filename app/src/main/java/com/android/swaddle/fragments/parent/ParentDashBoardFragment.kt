package com.android.swaddle.fragments.parent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.activities.parent.ParentDailyActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.baseClasses.BaseFragment
import com.android.swaddle.databinding.FragmentParentDashboardBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.signin_signout.SignInSignOutBottomSheet
import com.android.swaddle.utils.ProgressDialog

class ParentDashBoardFragment : BaseFragment() {

    private lateinit var binding: FragmentParentDashboardBinding
    private val pd = ProgressDialog.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVar()
        listener()
    }

    private fun listener() {
        binding.ivCross.setOnClickListener {
            binding.rlWarnings.viewGone()
        }
        binding.cvPayments.setOnClickListener {
            val intent = Intent(context, ParentPaymentMainActivity::class.java)
            startActivity(intent)
        }
        binding.cvAttandance.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java)
            intent.putExtra("fromAttendance", true)
            startActivity(intent)
        }

        binding.cvActivity.setOnClickListener {
            startActivity(Intent(context, ParentDailyActivity::class.java))
        }

        binding.cvSignInSignOut.setOnClickListener {
            val sheet = SignInSignOutBottomSheet(mActivity)
            sheet.show(activity?.supportFragmentManager!!, "SignInSignOutBottomSheet")
        }
    }

    private fun initVar() {
        if (mActivity.userManager.user?.isProviderPremium == 1) {
            when (mActivity.userManager.user?.providerSubscription?.status) {
                "trial" -> {
                    binding.rlWarnings.viewVisible()
                    binding.tvWarning.text = "Day care center is on trial period of 7 days"
                }
                "expire_soon" -> {//14Days
                    binding.rlWarnings.viewVisible()
                    binding.tvWarning.text =
                        "Payments pending! We will limit the functionality after 14 day."
                }
                else -> {
                    binding.rlWarnings.viewGone()
                }
            }
        } else {
            binding.rlWarnings.viewVisible()
            binding.tvWarning.text = "Day care services are limited due to pending payments"
        }
    }
}
