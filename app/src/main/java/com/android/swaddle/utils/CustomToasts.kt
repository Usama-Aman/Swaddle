package com.android.swaddle.utils

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.android.swaddle.R
import com.muddzdev.styleabletoast.StyleableToast
import org.jetbrains.anko.runOnUiThread

object CustomToasts {

    fun internetNotAvailableToast(context: Context) {
        context.runOnUiThread {
            StyleableToast.Builder(context)
                .text("Could not connect to the Internet, Please check your Internet Connection")
                .textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10)
                .show()
        }

    }

    fun internetNotAvailableToastCenterTop(context: Context) {
        context.runOnUiThread {
            StyleableToast.Builder(context).gravity(Gravity.CENTER_HORIZONTAL).gravity(Gravity.TOP)
                .text("Could not connect to the Internet, Please check your Internet Connection")
                .textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10)
                .show()
        }
    }


    fun noAffectedRowsToast(context: Context) {
        successToast(context, "No Affected Rows!")
    }

    fun basicToast(context: Context, text: String) {
        context.runOnUiThread {
            StyleableToast.Builder(context).text(text).textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).cornerRadius(10).show()
        }
    }

    fun failureToast(context: Context, text: String) {
        context.runOnUiThread {
            StyleableToast.Builder(context).text(text).textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10)
                .show()
        }
    }

    fun failureToastCenterTop(context: Context, text: String) {
        context.runOnUiThread {
            StyleableToast.Builder(context).gravity(Gravity.CENTER_HORIZONTAL).gravity(Gravity.TOP)
                .text(text).textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10)
                .show()
        }
    }

    fun successToast(context: Context, text: String) {
        context.runOnUiThread {
            StyleableToast.Builder(context).text(text).textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_done_green).cornerRadius(10)
                .show()
        }
    }

    fun successToastCenterTop(context: Context, text: String) {
        context.runOnUiThread {
            StyleableToast.Builder(context).gravity(Gravity.CENTER_HORIZONTAL).gravity(Gravity.TOP)
                .text(text).textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_done_green).cornerRadius(10)
                .show()
        }
    }

    fun executionErrorToast(context: Context, e: Exception) {
        context.runOnUiThread {
            failureToast(context, "Query Error : ${e.message.toString()}")
        }
    }

    fun executionErrorToastCenterTop(context: Context, e: Exception) {
        context.runOnUiThread {
            StyleableToast.Builder(context).gravity(Gravity.CENTER_HORIZONTAL).gravity(Gravity.TOP)
                .text("Query Error : ${e.message.toString()}").textColor(Color.WHITE)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
                .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10)
                .show()
        }
    }

    fun dbErrorToast(context: Context, e: Exception) {
        context.runOnUiThread {
            failureToast(context, "DB Connection Error : ${e.message.toString()}")
        }
    }

    fun dbErrorToastCenterTop(context: Context, e: Exception) {
        StyleableToast.Builder(context).gravity(Gravity.CENTER_HORIZONTAL).gravity(Gravity.TOP)
            .text("DB Connection Error : ${e.message.toString()}").textColor(Color.WHITE)
            .backgroundColor(ContextCompat.getColor(context, R.color.colorBlack80))
            .font(R.font.font_semi_bold).iconStart(R.drawable.ic_close_red).cornerRadius(10).show()
    }

    fun exceptionToast(context: Context, e: Exception) {
        failureToast(context, e.message.toString())
    }

}


