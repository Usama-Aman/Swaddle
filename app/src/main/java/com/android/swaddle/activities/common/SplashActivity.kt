package com.android.swaddle.activities.common

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.android.swaddle.activities.parent.ParentDailyActivity
import com.android.swaddle.activities.providers.AddActivity
import com.android.swaddle.activities.providers.LoginActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.activities.providers.reports.ChildIncidentReportActivity
import com.android.swaddle.activities.providers.reports.DailyReportActivity
import com.android.swaddle.activities.providers.reports.IncidentReportActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivitySplashBinding
import com.android.swaddle.payment_screens.ParentPaymentMainActivity
import com.android.swaddle.prefrences.UserData
import org.jetbrains.anko.startActivity


class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        splashThread()

        try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                binding.tvVersionCode.text = "Version : ${pInfo.longVersionCode} ($version)"
            } else {
                binding.tvVersionCode.text = "Version : ${pInfo.versionCode} ($version)"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun splashThread() {
        Handler().postDelayed(
            {
                if (UserData.isUserLogin(this@SplashActivity)) {
                    val sintent = Intent(this@SplashActivity, ProviderMainActivity::class.java)
                    if (intent.hasExtra("fromNotification")) {
                        sintent.putExtra(
                            "fromNotification",
                            intent.getBooleanExtra("fromNotification", false)
                        )
                        if (intent.hasExtra("chat_id")) {
                            sintent.putExtra("chat_id", intent.getStringExtra("chat_id"))
                            sintent.putExtra("sender_name", intent.getStringExtra("sender_name"))
                        }
                        if (intent.hasExtra("type")) {
                            sintent.putExtra("type", intent.getStringExtra("type"))
                            sintent.putExtra("classroom_id", intent.getIntExtra("classroom_id", -1))
                            sintent.putExtra("child_id", intent.getIntExtra("child_id", -1))
                        }
                    }
                    startActivity(sintent)

                } else {
                    startActivity<LoginActivity>()
                }
                finishAffinity()
            }, 1200
        )
    }


    private fun init() {
        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }


}