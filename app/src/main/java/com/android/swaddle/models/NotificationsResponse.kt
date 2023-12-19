package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable

class NotificationsResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<NotificationDetail>? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class NotificationDetail : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("sender_id")
    @Expose
    var senderId: Int? = null

    @SerializedName("reciever_id")
    @Expose
    var recieverId: Int? = null

    @SerializedName("notification")
    @Expose
    var notification: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("user_details")
    @Expose
    var userDetails: User? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("type_id")
    @Expose
    var type_id: String? = null

    @SerializedName("classroom_id")
    @Expose
    var classroom_id: String? = null

    @SerializedName("child_id")
    @Expose
    var child_id: String? = null

}