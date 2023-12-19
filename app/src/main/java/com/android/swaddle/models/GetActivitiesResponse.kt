package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class GetActivitiesResponse {

    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<Activities>? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }

}

class Activities {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("classroom_id")
    @Expose
    var classroomId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("activity_type")
    @Expose
    var activityType: String? = null

    @SerializedName("activity_date")
    @Expose
    var activityDate: String? = null

    @SerializedName("activity_files")
    @Expose
    var activityFiles: String? = null

    @SerializedName("comment")
    @Expose
    var comment: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("activity_message")
    @Expose
    var activity_message: String? = null

    @SerializedName("day")
    @Expose
    var day: String? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("classroomId", classroomId)
            .append("submittedBy", submittedBy).append("activityType", activityType)
            .append("activityDate", activityDate).append("activityFiles", activityFiles)
            .append("comment", comment).append("deletedAt", deletedAt)
            .append("createdAt", createdAt).append("updatedAt", updatedAt)
            .append("day", day).toString()
    }
}