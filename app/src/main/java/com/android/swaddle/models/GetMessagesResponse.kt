package com.android.swaddle.models

import com.google.android.exoplayer2.PlayerMessage.Sender
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class GetMessagesResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("successData")
    @Expose
    var messages: ArrayList<Message>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("successData", messages).toString()
    }
}

class Message {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("chat_id")
    @Expose
    var chatId: Int? = null

    @SerializedName("sender_id")
    @Expose
    var senderId: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("file_type")
    @Expose
    var fileType: String? = null

    @SerializedName("file_path")
    @Expose
    var filePath: String? = null

    @SerializedName("file_ratio")
    @Expose
    var fileRatio: String? = null

    @SerializedName("is_deleted")
    @Expose
    var isDeleted: Int? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("sender")
    @Expose
    var sender: User? = null

    @SerializedName("receiver")
    @Expose
    var receiver: User? = null

    override fun toString(): String {
        return ToStringBuilder(this)
            .append("id", id).append("chatId", chatId)
            .append("senderId", senderId).append("message", message).append("fileType", fileType)
            .append("filePath", filePath).append("fileRatio", fileRatio)
            .append("isDeleted", isDeleted).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).append("sender", sender).toString()
    }


}