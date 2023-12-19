package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class GetStaffResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<User>? = null

    override fun toString(): String{
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class StaffAttendance: Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("staff_id")
    @Expose
    var staffId: Int? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: Any? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("details")
    @Expose
    var details: String? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("staffId", staffId)
            .append("status", status).append("deletedAt", deletedAt).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).append("details", details).toString()
    }
}