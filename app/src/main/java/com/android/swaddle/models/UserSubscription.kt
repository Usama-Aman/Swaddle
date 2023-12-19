package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder


class UserSubscription {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("user_id")
    @Expose
    var userId: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("stripe_id")
    @Expose
    var stripeId: String? = null

    @SerializedName("stripe_status")
    @Expose
    var stripeStatus: String? = null

    @SerializedName("stripe_plan")
    @Expose
    var stripePlan: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: Int? = null

    @SerializedName("trial_ends_at")
    @Expose
    var trialEndsAt: Any? = null

    @SerializedName("ends_at")
    @Expose
    var endsAt: Any? = null

    @SerializedName("apply_rounds_count")
    @Expose
    var applyRoundsCount: Int? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("items")
    @Expose
    var items: ArrayList<Any>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("userId", userId).append("name", name)
            .append("stripeId", stripeId).append("stripeStatus", stripeStatus)
            .append("stripePlan", stripePlan).append("quantity", quantity)
            .append("trialEndsAt", trialEndsAt).append("endsAt", endsAt)
            .append("applyRoundsCount", applyRoundsCount).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).append("items", items).toString()
    }

}