package com.android.swaddle.activities.providers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ChildProfileListActivity
import com.android.swaddle.activities.parent.MedicalReportActivity
import com.android.swaddle.activities.parent.ParentDailyActivity
import com.android.swaddle.activities.providers.messages.ChatActivity
import com.android.swaddle.activities.providers.reports.ChildIncidentReportActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.activities.providers.reports.IncidentReportActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityProviderMainBinding
import com.android.swaddle.enums.UserType
import com.android.swaddle.fragments.parent.ParentDashBoardFragment
import com.android.swaddle.fragments.parent.ParentMoreFragment
import com.android.swaddle.fragments.providers.DashboardFragment
import com.android.swaddle.fragments.providers.MessagesFragment
import com.android.swaddle.fragments.providers.MoreFragment
import com.android.swaddle.fragments.providers.NotificationFragment
import com.android.swaddle.onesignal.OneSignalNotificationManager
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProviderMainActivity : BaseActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var imgList: ArrayList<ImageView> = ArrayList()

    private lateinit var binding: ActivityProviderMainBinding
    private lateinit var fragment: Fragment
    private lateinit var fragmentTransaction: FragmentTransaction

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        if (binding.viewPager.currentItem != 0) {
            binding.viewDashboard.callOnClick()
            return
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        CustomToasts.basicToast(this@ProviderMainActivity, "Please click BACK again to exit")
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setViewPager()
        listener()

        OneSignalNotificationManager.sendUserTags(this)

        onNewIntent(intent)
    }

    private fun setViewPager() {
        binding.viewPager.adapter = ViewPagerFragmentAdapter(this@ProviderMainActivity)
        binding.viewPager.isUserInputEnabled = false

    }

    private fun listener() {
        binding.viewDashboard.setOnClickListener {
            changeBottomTabColors(binding.imgDashboard)
            binding.viewPager.setCurrentItem(0, false)
        }


        binding.viewMessages.setOnClickListener {
            changeBottomTabColors(binding.imgMsg)
            binding.viewPager.setCurrentItem(1, false)
        }

        binding.viewNotification.setOnClickListener {
            changeBottomTabColors(binding.imgNotification)
            binding.viewPager.setCurrentItem(2, false)

        }

        binding.viewMore.setOnClickListener {
            changeBottomTabColors(binding.imgMore)
            binding.viewPager.setCurrentItem(3, false)
        }
    }


    private fun init() {
        imgList.add(binding.imgDashboard)
        imgList.add(binding.imgMsg)
        imgList.add(binding.imgNotification)
        imgList.add(binding.imgMore)


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                binding.viewPager.currentItem = 0
            }
            R.id.nav_message -> {
                binding.viewPager.currentItem = 1
            }
            R.id.nav_notification -> {
                binding.viewPager.currentItem = 2
            }

            R.id.nav_more -> {
                binding.viewPager.currentItem = 3
            }

        }
        return true
    }

    fun gotoMessages() {
        binding.viewMessages.callOnClick()
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        private val contextt: Context = fragmentActivity
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> {
                    return if (UserData.getUserType(contextt) == UserType.PARENT.type || UserData.getUserType(
                            contextt
                        ) == UserType.AUTHORIZED_ADULT.type
                    ) {
                        ParentDashBoardFragment()

                    } else {
                        DashboardFragment()
                    }
                }
                1 -> return MessagesFragment()
                2 -> return NotificationFragment()
                3 -> {
                    return if (UserData.getUserType(contextt) == UserType.PARENT.type || UserData.getUserType(
                            contextt
                        ) == UserType.AUTHORIZED_ADULT.type
                    ) {
                        ParentMoreFragment()
                    } else {
                        MoreFragment()
                    }
                }
            }
            return DashboardFragment()
        }

        override fun getItemCount(): Int {
            return 4
        }
    }


    private fun changeBottomTabColors(img: ImageView) {
        imgList.forEach {
            if (it.id == img.id) {
                img.setColorFilter(
                    ContextCompat.getColor(this, R.color.colroBlue),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                it.setColorFilter(
                    ContextCompat.getColor(this, R.color.colorDashBoardBlack),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.hasExtra("fromNotification") == true) {
            if (intent.hasExtra("chat_id")) {
                //binding.viewMessages.callOnClick()
                val sintent = Intent(applicationContext, ChatActivity::class.java)
                sintent.putExtra("chat_id", intent.getStringExtra("chat_id"))
                sintent.putExtra("main_name", intent.getStringExtra("sender_name"))
                startActivity(sintent)
            } else {
                goToNotificationTypeScreen(intent.getStringExtra("type"),intent.getIntExtra("child_id",-1),intent.getIntExtra("classroom_id",-1))
            }
        }
    }

    private fun goToNotificationTypeScreen(type: String?,child_id: Int, class_id: Int) {
        when (type) {
            "activity" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, ParentDailyActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, AddActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                }
            }
            "invoice" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, ParentPaymentMainActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, ParentPaymentMainActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "daily_report" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "incident_report" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, ChildIncidentReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, IncidentReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "parent_paid_to_provider" -> {//failed on provider
                val sintent = Intent(this, ParentPaymentMainActivity::class.java)
                sintent.putExtra("classroom_id", class_id)
                sintent.putExtra("child_id", child_id)
                startActivity(sintent)
            }
            "medical_information_updated" -> {//done
                val sintent = Intent(this, MedicalReportActivity::class.java)
                sintent.putExtra("classroom_id", class_id)
                sintent.putExtra("child_id", child_id)
                startActivity(sintent)
            }
            "medical_information_deleted" -> {//done
                val sintent = Intent(this, MedicalReportActivity::class.java)
                sintent.putExtra("classroom_id", class_id)
                sintent.putExtra("child_id", child_id)
                startActivity(sintent)
            }
            "update_child_attendance" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "child_attendance_signin" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "child_attendance_signout" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
            "center_closing_alert" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
                else{
                    val sintent = Intent(this, ProviderChildHelperActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                }
            }
            "child_created" ->{
                val intent = Intent(this, ChildProfileListActivity::class.java)
                startActivity(intent)
            }
            "staff_attendance_signin" -> {//done
                if (userManager.user?.type == Constants.provider
                ) {
                    val sintent = Intent(this, StaffActivity::class.java)
                    startActivity(sintent)
                }
            }
            "todays_activity_missing" -> {//done
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, ParentDailyActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, AddActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                }
            }
            "child_signed_out_before_dailyReport" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("fromAttendance", true)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
                else{
                    val sintent = Intent(this, ProviderChildHelperActivity::class.java)
                    sintent.putExtra("classroom_id", class_id)
                    startActivity(sintent)
                }
            }
            "provide_missing_daiylyReport_items" -> {
                if (userManager.user?.type == Constants.parent
                    || userManager.user?.type == Constants.authorized_adult
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.parent)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                } else if (userManager.user?.type == Constants.provider
                    || userManager.user?.type == Constants.staff
                ) {
                    val sintent = Intent(this, DailyReportActivity::class.java)
                    sintent.putExtra("type", Constants.provider)
                    sintent.putExtra("classroom_id", class_id)
                    sintent.putExtra("child_id", child_id)
                    startActivity(sintent)
                }
            }
        }
    }

}