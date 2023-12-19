package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable

class ClassRoomsResponse : Serializable {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<ClassroomDetails>? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class Counts {
    @SerializedName("sign_in_count")
    @Expose
    var signInCount: Int? = null

    @SerializedName("sign_out_count")
    @Expose
    var signOutCount: Int? = null

    @SerializedName("present_count")
    @Expose
    var presentCount: Int? = null

    @SerializedName("absent_count")
    @Expose
    var absentCount: Int? = null
}
