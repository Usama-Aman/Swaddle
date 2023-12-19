package com.android.swaddle.models.uploadFileResponse

import com.android.swaddle.models.uploadFileResponse.SuccessData
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class UploadFileResponse {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("successData")
    @Expose
    var successData: SuccessData? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("successData", successData).toString()
    }
}