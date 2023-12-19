package com.android.swaddle.baseClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.android.swaddle.activities.common.SplashActivity

import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.networkManager.NetworkURLs.Companion.KEY_GOOGLE
import com.android.swaddle.onesignal.OneSignalNotificationOpenedHandler
import com.android.swaddle.onesignal.OneSignalNotificationReceivedHandler
import com.android.swaddle.prefrences.UserData

import com.google.android.libraries.places.api.Places
import com.onesignal.OneSignal
import com.stripe.android.PaymentConfiguration
import org.json.JSONObject
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
open class BaseApplication : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: BaseApplication? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        var isAppRunning: Boolean = false
        var isChatScreenOpen = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Places.initialize(this, KEY_GOOGLE)
//        OneSignal.startInit(this).init();
        OneSignal.startInit(this)
            .filterOtherGCMReceivers(true)
            .disableGmsMissingPrompt(false)
            .autoPromptLocation(false) // default call promptLocation later
            .setNotificationReceivedHandler(OneSignalNotificationReceivedHandler(this@BaseApplication))
            .setNotificationOpenedHandler(OneSignalNotificationOpenedHandler(this@BaseApplication))
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .init()

        context = this
//        sendTags()

        PaymentConfiguration.init(
//            applicationContext,
            NetworkURLs.stripe_publishKey
        )

        isAppRunning = true
    }

    private fun sendTags() {
        val keys = JSONObject()
        keys.put("user_id", UserData.user(this@BaseApplication).user?.id)
        keys.put("device_type", "android")
        OneSignal.sendTags(keys)

        Log.e("SendTag", keys.toString())
    }

    override fun onTerminate() {
        //todo call api to set offline
        super.onTerminate()
    }

    private fun appInitialization() {

        defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler)
    }

    private var defaultUEH: Thread.UncaughtExceptionHandler? = null

    // handler listener
    private val _unCaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, ex ->
        ex.printStackTrace()
        thread.start()

        //        exitProcess(0)
    }
}

//


open class MyExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        val intent = Intent(activity, SplashActivity::class.java)
        intent.putExtra("crash", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            BaseApplication.instance?.baseContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val mgr =
            BaseApplication.instance?.baseContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        activity?.finish()
        exitProcess(2)
    }

    private var activity: Activity? = null

    fun MyExceptionHandler(a: Activity) {
        activity = a
    }

}