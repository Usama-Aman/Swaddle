package com.android.swaddle.prefrences

import android.content.Context
import androidx.core.content.edit
import com.android.swaddle.models.LoginData
import com.google.gson.Gson

object UserData {
    fun saveUserType(userType: String, context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putString("USER-TYPE", userType)
        }
    }


    fun getUserType(context: Context): String? {
        return context.getSharedPreferences("USER", 0).getString("USER-TYPE", "")
    }

    fun saveUserData(user: LoginData, context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putString("USER-DATA", Gson().toJson(user))
        }
    }

    fun saveChildId(selected_parent_athlete: String, context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putString("selected_parent_athlete", selected_parent_athlete)
        }
    }

    fun getSavedChildId(context: Context): String {
        return context.getSharedPreferences("USER", 0).getString("selected_parent_athlete", "")
            ?: ""
    }


    fun saveChildProfileImage(selected_parent_athlete_image: String, context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putString("selected_parent_athlete_image", selected_parent_athlete_image)
        }
    }

    fun getSavedChildProfileImage(context: Context): String {
        return context.getSharedPreferences("USER", 0)
            .getString("selected_parent_athlete_image", "")
            ?: ""
    }


    fun saveChildName(selected_child_name: String, context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putString("selected_child_name", selected_child_name)
        }
    }

    fun getSavedChildName(context: Context): String {
        return context.getSharedPreferences("USER", 0).getString("selected_child_name", "")
            ?: ""
    }


    fun clearUserData(context: Context) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            clear()
        }
    }

    fun user(context: Context): LoginData {
        return Gson().fromJson(
            context.getSharedPreferences("USER", 0).getString("USER-DATA", ""),
            LoginData::class.java
        ) ?: LoginData()
    }

    fun isUserLogin(context: Context): Boolean {
        return context.getSharedPreferences("USER", 0).getBoolean("USER-LOGIN", false)
    }

    fun setUserLogin(context: Context, isUserLogin: Boolean) {
        context.getSharedPreferences("USER", 0).edit(commit = true) {
            putBoolean("USER-LOGIN", isUserLogin)
        }
    }

}