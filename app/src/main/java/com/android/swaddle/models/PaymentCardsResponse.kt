package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class PaymentCardsResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: ArrayList<CardData>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}


class CardData {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("user_id")
    @Expose
    var userId: Int? = null

    @SerializedName("card_holder_name")
    @Expose
    var cardHolderName: Any? = null

    @SerializedName("last_four")
    @Expose
    var lastFour: Int? = null

    @SerializedName("card_brand")
    @Expose
    var cardBrand: String? = null

    @SerializedName("pm_id")
    @Expose
    var pmId: String? = null

    @SerializedName("exp_month")
    @Expose
    var expMonth: String? = null

    @SerializedName("exp_year")
    @Expose
    var expYear: String? = null

    @SerializedName("is_default")
    @Expose
    var isDefault: Int? = null

    @SerializedName("address")
    @Expose
    var address: Any? = null

    @SerializedName("country")
    @Expose
    var country: Any? = null

    @SerializedName("state")
    @Expose
    var state: Any? = null

    @SerializedName("city")
    @Expose
    var city: Any? = null

    @SerializedName("zip_code")
    @Expose
    var zipCode: Any? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: Any? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("userId", userId)
            .append("cardHolderName", cardHolderName).append("lastFour", lastFour)
            .append("cardBrand", cardBrand).append("pmId", pmId).append("expMonth", expMonth)
            .append("expYear", expYear).append("isDefault", isDefault).append("address", address)
            .append("country", country).append("state", state).append("city", city)
            .append("zipCode", zipCode).append("deletedAt", deletedAt)
            .append("createdAt", createdAt).append("updatedAt", updatedAt).toString()
    }
}