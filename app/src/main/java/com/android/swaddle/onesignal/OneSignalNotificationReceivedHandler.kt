package com.android.swaddle.onesignal

import android.util.Log
import com.android.swaddle.baseClasses.BaseApplication

import com.onesignal.OSNotification
import com.onesignal.OneSignal
import org.jetbrains.anko.runOnUiThread


//This will be called when a notification is received while your app is running.
class OneSignalNotificationReceivedHandler(var baseApplication: BaseApplication) :
    OneSignal.NotificationReceivedHandler {

    override fun notificationReceived(notification: OSNotification) {

        Log.e("NotiData", notification.toString())

        val data = notification.payload.additionalData
        Log.e("NotificationData", data.toString())
        try {
            baseApplication.runOnUiThread {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}