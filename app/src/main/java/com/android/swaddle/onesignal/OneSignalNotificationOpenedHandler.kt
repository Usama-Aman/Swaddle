package com.android.swaddle.onesignal

import android.content.Intent
import android.util.Log
import com.android.swaddle.activities.common.SplashActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.baseClasses.BaseApplication
import com.android.swaddle.prefrences.UserData
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal
import org.json.JSONObject

class OneSignalNotificationOpenedHandler(private var baseApplication: BaseApplication) :
    OneSignal.NotificationOpenedHandler {
    override fun notificationOpened(result: OSNotificationOpenResult) {
//        try {
//
//
//            val actionType = result.action.type
        val data = result.notification.payload.additionalData

        Log.e("NotificationOpened", data.toString())
////        Log.e("actionType", actionType.toString())
        var json = JSONObject(data.toString())
        var typedId = json.optInt("type_id")
//            var model =
//                Gson().fromJson(
//                    data.toString(),
//                    NotificationModel::class.java
//                )

        if (BaseApplication.isAppRunning)
            if (UserData.isUserLogin(baseApplication)) {
                var sintent = Intent(baseApplication, ProviderMainActivity::class.java)
                sintent.putExtra("fromNotification", true)
                if (json.has("chat_id")) {
                    sintent.putExtra("chat_id", json.getString("chat_id"))
//                    sintent.putExtra("sender_name", result.notification.payload.body.split("has")[0])
                    sintent.putExtra("sender_name", json.getString("sender_name"))
                }
                if (json.has("type")) {
                    sintent.putExtra("type", json.getString("type"))
                    if (json.has("classroom_id"))
                        sintent.putExtra("classroom_id", json.getInt("classroom_id"))
                    if (json.has("child_id"))
                        sintent.putExtra("child_id", json.getInt("child_id"))
                }
                sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                baseApplication.startActivity(sintent)

//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            } else {
                var sintent = Intent(baseApplication, SplashActivity::class.java)
                sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                baseApplication.startActivity(sintent)
            }
        else {
            var sintent = Intent(baseApplication, SplashActivity::class.java)
            sintent.putExtra("fromNotification", true)
            if (json.has("chat_id")) {
                sintent.putExtra("chat_id", json.getString("chat_id"))
                //sintent.putExtra("sender_name", result.notification.payload.body.split("has")[0])
                sintent.putExtra("sender_name", json.getString("sender_name"))
            }
            if (json.has("type")) {
                sintent.putExtra("type", json.getString("type"))
                if (json.has("classroom_id"))
                    sintent.putExtra("classroom_id", json.getInt("classroom_id"))
                if (json.has("child_id"))
                    sintent.putExtra("child_id", json.getInt("child_id"))
            }
            sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            baseApplication.startActivity(sintent)
        }


//            Log.e("openedResult", result.notification.toString())
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

    }

}