package com.android.swaddle.models.uploadFileResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class SuccessData {
    @SerializedName("full_path")
    @Expose
    var fullPath: String? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("fullPath", fullPath).toString()
    }
}