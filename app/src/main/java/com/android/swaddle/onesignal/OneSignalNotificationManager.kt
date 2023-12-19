package com.android.swaddle.onesignal

import android.content.Context
import android.util.Log
import com.android.swaddle.prefrences.UserData
import com.onesignal.OneSignal
import org.json.JSONObject

class OneSignalNotificationManager {

    companion object {
        var shared = OneSignalNotificationManager()

        const val USER_ID = "user_id"
        const val DEVICE_TYPE = "device_type"
        const val USER_TYPE = "user_type"

        fun sendUserTags(mContext: Context) {
            try {
                val userManager = UserData.user(mContext)
                val user = userManager.user
                if (user != null) {
                    OneSignal.sendTags(
                        JSONObject().put(USER_ID, user.id)
                            .put(DEVICE_TYPE, "android")
                            .put(USER_TYPE, "user"),
                        object : OneSignal.ChangeTagsUpdateHandler {
                            override fun onSuccess(tags: JSONObject?) {
                                Log.i("SendTagsSuccess", tags.toString())
                            }

                            override fun onFailure(error: OneSignal.SendTagsError?) {
                                Log.i("SendTagsError", error.toString())
                            }
                        })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun removeUserTags() {
            try {
                OneSignal.deleteTags(
                    arrayListOf(
                        USER_ID,
                        DEVICE_TYPE,
                        USER_TYPE
                    ),
                    object : OneSignal.ChangeTagsUpdateHandler {
                        override fun onSuccess(tags: JSONObject?) {
                            Log.i("RemoveTagsSuccess", tags.toString())
                        }

                        override fun onFailure(error: OneSignal.SendTagsError?) {
                            Log.i("RemoveTagsError", error?.toString()!!)
                        }

                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}