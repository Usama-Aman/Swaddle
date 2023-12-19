package com.android.swaddle.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun getDayName(date: Date) : String {
    val outFormat = SimpleDateFormat("EEE")
    val day: String = outFormat.format(date)
    return day
}

fun getDay(date: Date) : String {
    val inFormat = SimpleDateFormat("dd")
    val day: String = inFormat.format(date)
    return day
}
