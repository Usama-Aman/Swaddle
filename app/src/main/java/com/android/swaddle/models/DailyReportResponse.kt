package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class DailyReportResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: DailyReport? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }

}


class MedicalReportsResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<DailyReport>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }

}

class DailyReport : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("breakfast_portion")
    @Expose
    var breakfastPortion: String? = null

    @SerializedName("breakfast_description")
    @Expose
    var breakfastDescription: String? = null

    @SerializedName("lunch_portion")
    @Expose
    var lunchPortion: String? = null

    @SerializedName("lunch_description")
    @Expose
    var lunchDescription: String? = null

    @SerializedName("snack_portion")
    @Expose
    var snackPortion: String? = null

    @SerializedName("snack_description")
    @Expose
    var snackDescription: String? = null

    @SerializedName("bottles_drank_today")
    @Expose
    var bottlesDrankToday: String? = null

    @SerializedName("bowel_movements_count")
    @Expose
    var bowelMovementsCount: String? = null

    @SerializedName("bowel_movements_condition")
    @Expose
    var bowelMovementsCondition: String? = null

    @SerializedName("nap_times")
    @Expose
    var napTimes: String? = null

    @SerializedName("things_to_bring_tomorrow")
    @Expose
    var thingsToBringTomorrow: String? = null

    @SerializedName("comment")
    @Expose
    var comment: String? = null

    @SerializedName("file_path")
    @Expose
    var filePath: String? = null

    @SerializedName("draft")
    @Expose
    var draft: Int? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null
}