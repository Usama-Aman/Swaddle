package com.android.swaddle

import com.android.swaddle.models.UpdateProfileResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.ArrayList

class JUpdateProfileModelKotlin {

    @SerializedName("profile_picture")
    var profile_picture: File? = null

    @SerializedName("child_id")
    var child_id: Int? = null

    @SerializedName("first_name")
    var first_name: String? = null

    @SerializedName("middle_name")
    var middle_name: String? = null

    @SerializedName("last_name")
    var last_name: String? = null

    @SerializedName("nick_name")
    var nick_name: String? = null

    @SerializedName("child_call_you")
    var child_call_you: String? = null

    @SerializedName("gender")
    var gender: String? = null

    @SerializedName("dob")
    var dob: String? = null

    @SerializedName("primary_address")
    var primary_address: String? = null

    @SerializedName("secondary_address")
    var secondary_address: String? = null

    @SerializedName("custody")
    var custody: String? = null

    @SerializedName("iep")
    var iep: String? = null

    @SerializedName("comment")
    var comment: String? = null

    @SerializedName("iep_diagnosed_by")
    var iep_diagnosed_by: String? = null

    @SerializedName("does_have_any_allergies")
    var does_have_any_allergies: String? = null

    @SerializedName("food_allergies")
    var food_allergies: ArrayList<String>? = null

    @SerializedName("environmental_allergies")
    var environmental_allergies: ArrayList<String>? = null

    @SerializedName("does_child_require_epi_pin")
    var does_child_require_epi_pin: String? = null

    @SerializedName("will_you_provide_epi_pin")
    var will_you_provide_epi_pin: String? = null

    @SerializedName("custody_consideration")
    var custody_consideration: String? = null

    @SerializedName("custody_description")
    var custody_description: String? = null

    @SerializedName("monday")
    var monday: ArrayList<String>? = null

    @SerializedName("tuesday")
    var tuesday: ArrayList<String>? = null

    @SerializedName("wednesday")
    var wednesday: ArrayList<String>? = null

    @SerializedName("thursday")
    var thursday: ArrayList<String>? = null

    @SerializedName("friday")
    var friday: ArrayList<String>? = null

    @SerializedName("lat")
    var lat: String? = null

    @SerializedName("lng")
    var lng: String? = null

    constructor(
        profile_picture: File?,
        child_id: Int?,
        first_name: String?,
        middle_name: String?,
        last_name: String?,
        nick_name: String?,
        child_call_you: String?,
        gender: String?,
        dob: String?,
        primary_address: String?,
        secondary_address: String?,
        custody: String?,
        iep: String?,
        comment: String?,
        iep_diagnosed_by: String?,
        does_have_any_allergies: String?,
        food_allergies: ArrayList<String>?,
        environmental_allergies: ArrayList<String>?,
        does_child_require_epi_pin: String?,
        will_you_provide_epi_pin: String?,
        custody_consideration: String?,
        custody_description: String?,
        monday: ArrayList<String>?,
        tuesday: ArrayList<String>?,
        wednesday: ArrayList<String>?,
        thursday: ArrayList<String>?,
        friday: ArrayList<String>?,
        lat: String?,
        lng: String?
    ) {
        this.profile_picture = profile_picture
        this.child_id = child_id
        this.first_name = first_name
        this.middle_name = middle_name
        this.last_name = last_name
        this.nick_name = nick_name
        this.child_call_you = child_call_you
        this.gender = gender
        this.dob = dob
        this.primary_address = primary_address
        this.secondary_address = secondary_address
        this.custody = custody
        this.iep = iep
        this.comment = comment
        this.iep_diagnosed_by = iep_diagnosed_by
        this.does_have_any_allergies = does_have_any_allergies
        this.food_allergies = food_allergies
        this.environmental_allergies = environmental_allergies
        this.does_child_require_epi_pin = does_child_require_epi_pin
        this.will_you_provide_epi_pin = will_you_provide_epi_pin
        this.custody_consideration = custody_consideration
        this.custody_description = custody_description
        this.monday = monday
        this.tuesday = tuesday
        this.wednesday = wednesday
        this.thursday = thursday
        this.friday = friday
        this.lat = lat
        this.lng = lng
    }


    //    @Multipart
//    @POST("register_or_update_child")
//    fun registerOrUpdateChildWithOutImage(
//        @Part custodyDocument: MultipartBody.Part?,
//    ): Call<UpdateProfileResponse>?


}