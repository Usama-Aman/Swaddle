package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class ForgetPasswordResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ForgetPasswordData? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class ForgetPasswordData {
    @SerializedName("code")
    @Expose
    var code: Int? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("code", code).toString()
    }
}