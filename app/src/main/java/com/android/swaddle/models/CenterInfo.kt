package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class CenterInfo : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("provider_id")
    @Expose
    var providerId: Int? = null

    @SerializedName("center_name")
    @Expose
    var centerName: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("location")
    @Expose
    var location: String? = null

    @SerializedName("lat")
    @Expose
    var lat: String? = null

    @SerializedName("lng")
    @Expose
    var lng: String? = null

    @SerializedName("website")
    @Expose
    var website: String? = null

    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String? = null

    @SerializedName("center_logo")
    @Expose
    var centerLogo: String? = null

    @SerializedName("monday")
    @Expose
    var monday: String? = null

    @SerializedName("tuesday")
    @Expose
    var tuesday: String? = null

    @SerializedName("wednesday")
    @Expose
    var wednesday: String? = null

    @SerializedName("thursday")
    @Expose
    var thursday: String? = null

    @SerializedName("friday")
    @Expose
    var friday: String? = null

    @SerializedName("saturday")
    @Expose
    var saturday: String? = null

    @SerializedName("sunday")
    @Expose
    var sunday: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("provider")
    @Expose
    var provider: User? = null

    @SerializedName("staff")
    @Expose
    var staff: ArrayList<User>? = ArrayList()

    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("providerId", providerId)
            .append("centerName", centerName).append("email", email).append("location", location)
            .append("website", website).append("phoneNumber", phoneNumber)
            .append("centerLogo", centerLogo).append("monday", monday).append("tuesday", tuesday)
            .append("wednesday", wednesday).append("thursday", thursday).append("friday", friday)
            .append("saturday", saturday).append("sunday", sunday).append("deletedAt", deletedAt)
            .append("createdAt", createdAt).append("updatedAt", updatedAt)
            .append("provider", provider).append("staff", staff).toString()
    }
}