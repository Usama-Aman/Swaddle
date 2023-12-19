package com.android.swaddle.models

import java.util.*

data class CalendarModel(
    var date: Date? = null,
    var isEnabled: Boolean = true,
    var isSelected: Boolean = false,
    var is_present : Int = 0
)