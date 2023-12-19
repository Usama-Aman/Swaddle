package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class TemporaryChatResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: TemporaryChat? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class TemporaryChat : Serializable {

    @SerializedName("chat_type")
    @Expose
    var chatType: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("admin_id")
    @Expose
    var adminId: Int? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("other_user_id")
    @Expose
    var otherUserId: Int? = null

    @SerializedName("group_photo")
    @Expose
    var groupPhoto: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("unread_count")
    @Expose
    var unreadCount: Int? = null

    @SerializedName("last_message")
    @Expose
    var lastMessage: Message? = null

    @SerializedName("messages")
    @Expose
    var messages: Message? = null

    @SerializedName("members")
    @Expose
    var members: ArrayList<Member>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("adminId", adminId)
            .append("title", title).append("type", type).append("groupPhoto", groupPhoto)
            .append("createdAt", createdAt).append("updatedAt", updatedAt)
            .append("unreadCount", unreadCount).append("lastMessage", lastMessage)
            .append("messages", messages).append("members", members).toString()
    }

}



