package com.android.swaddle.models

data class WeeklyChildAttendanceModel(
    val `data`: List<WeeklyChildAttendanceData>,
    val message: String,
    val status: Boolean
)

data class WeeklyChildAttendanceData(
    val date: String,
    val day: String,
    val is_present: Int,
    val week_day: String,
    var isSelected: Boolean = false,
    var isEnabled: Boolean = false,
)