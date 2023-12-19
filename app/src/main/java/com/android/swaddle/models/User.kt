package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class User : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    var selected: Boolean? = false

    @SerializedName("linked_to")
    @Expose
    var linkedTo: Int? = null

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @SerializedName("middle_name")
    @Expose
    var middleName: String? = null

    @SerializedName("last_name")
    @Expose
    var lastName: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("email_verified_at")
    @Expose
    var emailVerifiedAt: String? = null

    @SerializedName("session_token")
    @Expose
    var sessionToken: String? = null

    @SerializedName("profile_picture")
    @Expose
    var profilePicture: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("position")
    @Expose
    var position: String? = null

    @SerializedName("experience")
    @Expose
    var experience: String? = null

    @SerializedName("certification")
    @Expose
    var certification: String? = null

    @SerializedName("more_certification")
    @Expose
    var moreCertification: String? = null

    @SerializedName("inspiration")
    @Expose
    var inspiration: String? = null

    @SerializedName("custody_document")
    @Expose
    var custodyDocument: ArrayList<String>? = ArrayList()

    @SerializedName("completion_date")
    @Expose
    var completionDate: String? = null

    @SerializedName("more_completion_date")
    @Expose
    var moreCompletionDate: String? = null

    @SerializedName("about_me")
    @Expose
    var aboutMe: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("relation")
    @Expose
    var relation: String? = null

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("home_address")
    @Expose
    var homeAddress: String? = null

    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String? = null

    @SerializedName("lat")
    @Expose
    var lat: String? = null

    @SerializedName("lng")
    @Expose
    var lng: String? = null

    @SerializedName("workplace")
    @Expose
    var workplace: String? = null

    @SerializedName("workplace_address")
    @Expose
    var workplaceAddress: String? = null

    @SerializedName("workplace_phone_number")
    @Expose
    var workplacePhoneNumber: String? = null

    @SerializedName("custody_consideration")
    @Expose
    var custodyConsideration: String? = null

    @SerializedName("ethnicity")
    @Expose
    var ethnicity: String? = null

    @SerializedName("ethnicity_others")
    @Expose
    var ethnicity_others: String? = null

    @SerializedName("custody_description")
    @Expose
    var custodyDescription: String? = null


    @SerializedName("stripe_id")
    @Expose
    var stripeId: String? = null

    @SerializedName("stripe_payout_account_id")
    @Expose
    var stripePayoutAccountId: String? = null

    @SerializedName("stripe_express_dashboard_url")
    @Expose
    var stripeExpressDashboardUrl: String? = null

    @SerializedName("is_bank_account_verified")
    @Expose
    var isBankAccountVerified: Int? = null

    @SerializedName("bank_name")
    @Expose
    var bankName: String? = null

    @SerializedName("dismissal_date")
    @Expose
    var dismissalDate: String? = null

    @SerializedName("close_account")
    @Expose
    var closeAccount: Int? = null

    @SerializedName("is_active")
    @Expose
    var isActive: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("child_calls_you")
    @Expose
    var childCallsYou: String? = null

    @SerializedName("center_info")
    @Expose
    var centerInfo: CenterInfo? = CenterInfo()

    @SerializedName("child_info")
    @Expose
    var childInfo: ArrayList<ChildInfo>? = ArrayList()

    @SerializedName("attendance_today")
    @Expose
    var attendance: ArrayList<StaffAttendance>? = ArrayList()

    @SerializedName("is_provider_premium")
    @Expose
    var isProviderPremium: Int? = null

    var isUserSelected = false


    @SerializedName("provider_subscription")
    @Expose
    var providerSubscription: ProviderSubscription? = ProviderSubscription()

}