package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class GetSummaryReportResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<SummaryReport>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class SummaryReport : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("attendance_date")
    @Expose
    var attendanceDate: String? = null

    @SerializedName("arrival_time")
    @Expose
    var arrivalTime: String? = null

    @SerializedName("departure_time")
    @Expose
    var departureTime: String? = null

    @SerializedName("dropped_off_by")
    @Expose
    var droppedOffBy: User? = null

    @SerializedName("picked_up_by")
    @Expose
    var pickedUpBy: User? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: Any? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("attendence_day")
    @Expose
    var attendenceDay: String? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("childId", childId)
            .append("submittedBy", submittedBy).append("attendanceDate", attendanceDate)
            .append("arrivalTime", arrivalTime).append("departureTime", departureTime)
            .append("droppedOffBy", droppedOffBy).append("pickedUpBy", pickedUpBy)
            .append("deletedAt", deletedAt).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).append("attendenceDay", attendenceDay).toString()
    }
}