package com.android.swaddle.enums

import java.io.Serializable

    enum class UserType(val type: String) :Serializable{

    PROVIDER("provider"),
    PARENT("parent"),
    STAFF("staff"),
    AUTHORIZED_ADULT("authorized_adult")
}