package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class LoginResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: LoginData? = null


    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class NotificationSettingData {
    @SerializedName("allow_notification")
    @Expose
    var allowNotification: String? = null
}

class LoginData {
    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null

    @SerializedName("token_type")
    @Expose
    var tokenType: String? = null

    @SerializedName("expires_at")
    @Expose
    var expiresAt: String? = null

    @SerializedName("user")
    @Expose
    var user: User? = User()


    override fun toString(): String {
        return ToStringBuilder(this).append("accessToken", accessToken)
            .append("tokenType", tokenType).append("expiresAt", expiresAt).append("user", user)
            .toString()
    }
}


class NotificationSettingsResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: NotificationSettingData? = null


    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class ProviderSubscription: Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("user_id")
    @Expose
    var userId: Int? = null

    @SerializedName("package_id")
    @Expose
    var packageId: Int? = null

    @SerializedName("price")
    @Expose
    var price: Int? = null

    @SerializedName("start_date")
    @Expose
    var startDate: String? = null

    @SerializedName("expiry_date")
    @Expose
    var expiryDate: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("duration")
    @Expose
    var duration: String? = null

    @SerializedName("card_id")
    @Expose
    var cardId: Int? = null

}