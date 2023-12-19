package com.android.swaddle.networkManager

import com.android.swaddle.models.LoginResponse
import com.android.swaddle.models.UpdateProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.*
import kotlin.collections.ArrayList

interface WebRequestsKotlin {


    //profile = 0, document = 0
    //profile = 1, document = 0
    //profile = 0, document = 1
    //profile = 1, document = 1

    //document == 0 , profile=0
    @Multipart
    @POST("register_or_update_child")
    fun registerOrUpdateChildWithNoImage(
        @Header("Authorization") headerToken: String?,
        @Part("parent_id") parent_id: RequestBody?,
        @Part("child_id") child_id: Int?,
        @Part("first_name") first_name: RequestBody?,
        @Part("middle_name") middle_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("nick_name") nick_name: RequestBody?,
        @Part("child_call_you") child_call_you: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("primary_address") primary_address: RequestBody?,
        @Part("apt") apt: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("zip_code") zip_code: RequestBody?,
        @Part("secondary_address") secondary_address: RequestBody?,
        @Part("s_apt") s_apt: RequestBody?,
        @Part("s_city") s_city: RequestBody?,
        @Part("s_state") s_state: RequestBody?,
        @Part("s_zip_code") s_zip_code: RequestBody?,
        @Part("custody") custody: RequestBody?,
        @Part("iep") iep: RequestBody?,
        @Part("special_need_considerations") specialNeedConsiderations: RequestBody?,
        @Part("epi_pen_expiration_date") epiPenExpirationDate: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part("iep_diagnosed_by") iep_diagnosed_by: RequestBody?,
        @Part("does_have_any_allergies") does_have_any_allergies: RequestBody?,
        @Part("food_allergies[]") food_allergies: ArrayList<RequestBody>?,
        @Part("environmental_allergies[]") environmental_allergies: ArrayList<RequestBody>?,
        @Part("does_child_require_epi_pin") does_child_require_epi_pin: RequestBody?,
        @Part("custody_consideration") custody_consideration: RequestBody?,
        @Part("custody_description") custody_description: RequestBody?,
        @Part("monday[0]") mondayOpening: RequestBody?,
        @Part("monday[1]") mondayClosing: RequestBody?,
        @Part("tuesday[0]") tuesdayOpening: RequestBody?,
        @Part("tuesday[1]") tuesdayClosing: RequestBody?,
        @Part("wednesday[0]") wednesdayOpening: RequestBody?,
        @Part("wednesday[1]") wednesdayClosing: RequestBody?,
        @Part("thursday[0]") thursdayOpening: RequestBody?,
        @Part("thursday[1]") thursdayClosing: RequestBody?,
        @Part("friday[0]") fridayOpening: RequestBody?,
        @Part("friday[1]") fridayClosing: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("lng") lng: RequestBody?,
        @Part("ethnicity") ethnicity: RequestBody?,
        @Part("ethnicity_others") ethnicityOthers: RequestBody?
    ): Call<LoginResponse>?


    //profile = 1, document = 0
    @Multipart
    @POST("register_or_update_child")
    fun registerOrUpdateChildWithProfileImageNoDocument(
        @Header("Authorization") headerToken: String?,
        @Part("parent_id") parent_id: RequestBody?,
        @Part file: MultipartBody.Part?,
        @Part("child_id") child_id: Int?,
        @Part("first_name") first_name: RequestBody?,
        @Part("middle_name") middle_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("nick_name") nick_name: RequestBody?,
        @Part("child_call_you") child_call_you: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("primary_address") primary_address: RequestBody?,
        @Part("apt") apt: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("zip_code") zip_code: RequestBody?,
        @Part("secondary_address") secondary_address: RequestBody?,
        @Part("s_apt") s_apt: RequestBody?,
        @Part("s_city") s_city: RequestBody?,
        @Part("s_state") s_state: RequestBody?,
        @Part("s_zip_code") s_zip_code: RequestBody?,
        @Part("custody") custody: RequestBody?,
        @Part("iep") iep: RequestBody?,
        @Part("special_need_considerations") specialNeedConsiderations: RequestBody?,
        @Part("epi_pen_expiration_date") epiPenExpirationDate: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part("iep_diagnosed_by") iep_diagnosed_by: RequestBody?,
        @Part("does_have_any_allergies") does_have_any_allergies: RequestBody?,
        @Part("food_allergies[]") food_allergies: ArrayList<RequestBody>?,
        @Part("environmental_allergies[]") environmental_allergies: ArrayList<RequestBody>?,
        @Part("does_child_require_epi_pin") does_child_require_epi_pin: RequestBody?,
        @Part("custody_consideration") custody_consideration: RequestBody?,
        @Part("custody_description") custody_description: RequestBody?,
        @Part("monday[0]") mondayOpening: RequestBody?,
        @Part("monday[1]") mondayClosing: RequestBody?,
        @Part("tuesday[0]") tuesdayOpening: RequestBody?,
        @Part("tuesday[1]") tuesdayClosing: RequestBody?,
        @Part("wednesday[0]") wednesdayOpening: RequestBody?,
        @Part("wednesday[1]") wednesdayClosing: RequestBody?,
        @Part("thursday[0]") thursdayOpening: RequestBody?,
        @Part("thursday[1]") thursdayClosing: RequestBody?,
        @Part("friday[0]") fridayOpening: RequestBody?,
        @Part("friday[1]") fridayClosing: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("lng") lng: RequestBody?,
        @Part("ethnicity") ethnicity: RequestBody?,
        @Part("ethnicity_others") ethnicityOthers: RequestBody?
    ): Call<LoginResponse>?


    //profile = 0, document = 1
    @Multipart
    @POST("register_or_update_child")
    fun registerOrUpdateChildWithDocument(
        @Header("Authorization") headerToken: String?,
        @Part("parent_id") parent_id: RequestBody?,
        @Part document: ArrayList<MultipartBody.Part?>,
        @Part("child_id") child_id: Int?,
        @Part("first_name") first_name: RequestBody?,
        @Part("middle_name") middle_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("nick_name") nick_name: RequestBody?,
        @Part("child_call_you") child_call_you: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("primary_address") primary_address: RequestBody?,
        @Part("apt") apt: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("zip_code") zip_code: RequestBody?,
        @Part("secondary_address") secondary_address: RequestBody?,
        @Part("s_apt") s_apt: RequestBody?,
        @Part("s_city") s_city: RequestBody?,
        @Part("s_state") s_state: RequestBody?,
        @Part("s_zip_code") s_zip_code: RequestBody?,
        @Part("custody") custody: RequestBody?,
        @Part("iep") iep: RequestBody?,
        @Part("special_need_considerations") specialNeedConsiderations: RequestBody?,
        @Part("epi_pen_expiration_date") epiPenExpirationDate: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part("iep_diagnosed_by") iep_diagnosed_by: RequestBody?,
        @Part("does_have_any_allergies") does_have_any_allergies: RequestBody?,
        @Part("food_allergies[]") food_allergies: ArrayList<RequestBody>?,
        @Part("environmental_allergies[]") environmental_allergies: ArrayList<RequestBody>?,
        @Part("does_child_require_epi_pin") does_child_require_epi_pin: RequestBody?,
        @Part("custody_consideration") custody_consideration: RequestBody?,
        @Part("custody_description") custody_description: RequestBody?,
        @Part("monday[0]") mondayOpening: RequestBody?,
        @Part("monday[1]") mondayClosing: RequestBody?,
        @Part("tuesday[0]") tuesdayOpening: RequestBody?,
        @Part("tuesday[1]") tuesdayClosing: RequestBody?,
        @Part("wednesday[0]") wednesdayOpening: RequestBody?,
        @Part("wednesday[1]") wednesdayClosing: RequestBody?,
        @Part("thursday[0]") thursdayOpening: RequestBody?,
        @Part("thursday[1]") thursdayClosing: RequestBody?,
        @Part("friday[0]") fridayOpening: RequestBody?,
        @Part("friday[1]") fridayClosing: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("lng") lng: RequestBody?,
        @Part("ethnicity") ethnicity: RequestBody?,
        @Part("ethnicity_others") ethnicityOthers: RequestBody?
    ): Call<LoginResponse>?


    //profile = 1, document = 1
    @Multipart
    @POST("register_or_update_child")
    fun registerOrUpdateChildWithDocumentAndProfile(
        @Header("Authorization") headerToken: String?,
        @Part("parent_id") parent_id: RequestBody?,
        @Part file: MultipartBody.Part?,
        @Part document: ArrayList<MultipartBody.Part?>,
        @Part("child_id") child_id: Int?,
        @Part("first_name") first_name: RequestBody?,
        @Part("middle_name") middle_name: RequestBody?,
        @Part("last_name") last_name: RequestBody?,
        @Part("nick_name") nick_name: RequestBody?,
        @Part("child_call_you") child_call_you: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("primary_address") primary_address: RequestBody?,
        @Part("apt") apt: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("zip_code") zip_code: RequestBody?,
        @Part("secondary_address") secondary_address: RequestBody?,
        @Part("s_apt") s_apt: RequestBody?,
        @Part("s_city") s_city: RequestBody?,
        @Part("s_state") s_state: RequestBody?,
        @Part("s_zip_code") s_zip_code: RequestBody?,
        @Part("custody") custody: RequestBody?,
        @Part("iep") iep: RequestBody?,
        @Part("special_need_considerations") specialNeedConsiderations: RequestBody?,
        @Part("epi_pen_expiration_date") epiPenExpirationDate: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part("iep_diagnosed_by") iep_diagnosed_by: RequestBody?,
        @Part("does_have_any_allergies") does_have_any_allergies: RequestBody?,
        @Part("food_allergies[]") food_allergies: ArrayList<RequestBody>?,
        @Part("environmental_allergies[]") environmental_allergies: ArrayList<RequestBody>?,
        @Part("does_child_require_epi_pin") does_child_require_epi_pin: RequestBody?,
        @Part("custody_consideration") custody_consideration: RequestBody?,
        @Part("custody_description") custody_description: RequestBody?,
        @Part("monday[0]") mondayOpening: RequestBody?,
        @Part("monday[1]") mondayClosing: RequestBody?,
        @Part("tuesday[0]") tuesdayOpening: RequestBody?,
        @Part("tuesday[1]") tuesdayClosing: RequestBody?,
        @Part("wednesday[0]") wednesdayOpening: RequestBody?,
        @Part("wednesday[1]") wednesdayClosing: RequestBody?,
        @Part("thursday[0]") thursdayOpening: RequestBody?,
        @Part("thursday[1]") thursdayClosing: RequestBody?,
        @Part("friday[0]") fridayOpening: RequestBody?,
        @Part("friday[1]") fridayClosing: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("lng") lng: RequestBody?,
        @Part("ethnicity") ethnicity: RequestBody?,
        @Part("ethnicity_others") ethnicityOthers: RequestBody?
    ): Call<LoginResponse>?

    //updateCustodyDocuments of child
    @Multipart
    @POST("upload_custody_document")
    fun updateCustodyDocuments(
        @Header("Authorization") headerToken: String?,
        @Part("child_id") child_id: RequestBody?,
        @Part document: ArrayList<MultipartBody.Part?>,
    ): Call<LoginResponse>?

}