package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable


class ChildesResponse : Serializable {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("counts")
    @Expose
    var counts: Counts? = null


    @SerializedName("data")
    @Expose
    var data: ArrayList<ChildInfo>? = null
    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", data).toString()
    }
}

class ChildDetailsResponse : Serializable {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var childInfo: ChildInfo? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("status", status).append("message", message)
            .append("data", childInfo).toString()
    }
}

class ChildInfo : Serializable {
    @SerializedName("child_latest_invoice")
    @Expose
    var childLatestInvoice: ArrayList<ChildLatestInvoice>? = ArrayList()

    @SerializedName("classroom_id")
    @Expose
    var classroomId: Int? = null


    @SerializedName("primary_address")
    @Expose
    var primaryAddress: String? = null

    @SerializedName("apt")
    @Expose
    var apt: String? = null

    @SerializedName("city")
    @Expose
    var city: String? = null

    @SerializedName("state")
    @Expose
    var state: String? = null

    @SerializedName("zip_code")
    @Expose
    var zipCode: String? = null

    @SerializedName("attendance")
    @Expose
    var attendance: ChildAttendance? = ChildAttendance()

    @SerializedName("child_attendance")
    @Expose
    var childAttendance: ChildAttendance? = ChildAttendance()

    @SerializedName("secondary_address")
    @Expose
    var secondaryAddress: String? = null

    @SerializedName("s_apt")
    @Expose
    var sApt: String? = null

    @SerializedName("s_city")
    @Expose
    var sCity: String? = null

    @SerializedName("s_state")
    @Expose
    var sState: String? = null

    @SerializedName("s_zip_code")
    @Expose
    var sZipCode: String? = null

    @SerializedName("ethnicity")
    @Expose
    var ethnicity: String? = null

    @SerializedName("ethnicity_others")
    @Expose
    var ethnicityOthers: String? = null

    @SerializedName("custody_consideration")
    @Expose
    var custodyConsideration: String? = null

    @SerializedName("custody_description")
    @Expose
    var custodyDescription: String? = null

    @SerializedName("custody_document")
    @Expose
    var custodyDocument: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("registrar_id")
    @Expose
    var registrarId: Int? = null

    @SerializedName("authorized_adults")
    @Expose
    var authorizedAdults: String? = null

    @SerializedName("classroom_details")
    @Expose
    var classroomDetails: ClassroomDetails? = ClassroomDetails()

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @SerializedName("middle_name")
    @Expose
    var middleName: String? = null

    @SerializedName("last_name")
    @Expose
    var lastName: String? = null

    @SerializedName("nick_name")
    @Expose
    var nickName: String? = null

    @SerializedName("child_call_you")
    @Expose
    var childCallYou: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("special_need_considerations")
    @Expose
    var specialNeedConsiderations: String? = null

    @SerializedName("authorized_adult_child_call")
    @Expose
    var authorizedAdultChildCall: String? = null

    @SerializedName("custody")
    @Expose
    var custody: String? = null

    @SerializedName("iep")
    @Expose
    var iep: String? = null

    @SerializedName("comment")
    @Expose
    var comment: String? = null

    @SerializedName("iep_diagnosed_by")
    @Expose
    var iepDiagnosedBy: String? = null

    @SerializedName("does_have_any_allergies")
    @Expose
    var doesHaveAnyAllergies: String? = null

    @SerializedName("food_allergies")
    @Expose
    var foodAllergies: String? = null

    @SerializedName("environmental_allergies")
    @Expose
    var environmentalAllergies: String? = null

    @SerializedName("does_child_require_epi_pin")
    @Expose
    var doesChildRequireEpiPin: String? = null

    @SerializedName("will_you_provide_epi_pin")
    @Expose
    var willYouProvideEpiPin: String? = null

    @SerializedName("epi_pen_received_in_day_care")
    @Expose
    var epiPenReceivedInDayCare: String? = null

    @SerializedName("epi_pen_expiration_date")
    @Expose
    var epiPenExpirationDate: String? = null

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

    @SerializedName("profile_picture")
    @Expose
    var profilePicture: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null
    var isSelected: Boolean? = false

    @SerializedName("relations")
    @Expose
    var relations: ArrayList<User>? = ArrayList()

    @SerializedName("incident_reports")
    @Expose
    var incidentReports: IncidentReport? = null
    var callsYou: String? = null

    @SerializedName("absent_notes")
    @Expose
    val absent_notes: AbsentNote? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("registrarId", registrarId)
            .append("authorizedAdults", authorizedAdults)
            .append("classroomDetails", classroomDetails)
            .append("firstName", firstName).append("middleName", middleName)
            .append("lastName", lastName).append("nickName", nickName)
            .append("childCallYou", childCallYou).append("dob", dob).append("gender", gender)
            .append("primaryAddress", primaryAddress)
            .append("authorizedAdultChildCall", authorizedAdultChildCall)
            .append("secondaryAddress", secondaryAddress).append("custody", custody)
            .append("iep", iep).append("comment", comment).append("iepDiagnosedBy", iepDiagnosedBy)
            .append("doesHaveAnyAllergies", doesHaveAnyAllergies)
            .append("foodAllergies", foodAllergies)
            .append("environmentalAllergies", environmentalAllergies)
            .append("doesChildRequireEpiPin", doesChildRequireEpiPin)
            .append("willYouProvideEpiPin", willYouProvideEpiPin)
            .append("epiPenReceivedInDayCare", epiPenReceivedInDayCare)
            .append("epiPenExpirationDate", epiPenExpirationDate).append("monday", monday)
            .append("tuesday", tuesday).append("wednesday", wednesday).append("thursday", thursday)
            .append("friday", friday).append("profilePicture", profilePicture)
            .append("deletedAt", deletedAt).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).toString()
    }
}

class ClassroomDetails : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("provider_id")
    @Expose
    var providerId: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: Any? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("is_default")
    @Expose
    var isDefault: Int? = null

    @SerializedName("sign_in_count")
    @Expose
    var signInCount: Int? = null

    @SerializedName("sign_out_count")
    @Expose
    var signOutCount: Int? = null

    @SerializedName("present_count")
    @Expose
    var presentCount: Int? = null

    @SerializedName("absent_count")
    @Expose
    var absentCount: Int? = null

    @SerializedName("childs")
    @Expose
    var childs: List<ChildInfo>? = null

    @SerializedName("childs_count")
    @Expose
    var childesCount: Int? = null


    var isSelected = false


    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("providerId", providerId)
            .append("name", name).append("description", description).append("deletedAt", deletedAt)
            .append("createdAt", createdAt).append("updatedAt", updatedAt).toString()
    }
}

class IncidentReport : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("provider_id")
    @Expose
    var providerId: Int? = null

    @SerializedName("what_happened")
    @Expose
    var whatHappened: String? = null

    @SerializedName("teacher_comment")
    @Expose
    var teacherComment: String? = null

    @SerializedName("treatment_given")
    @Expose
    var treatmentGiven: String? = null

    @SerializedName("media")
    @Expose
    var media: String? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null
    var isSelected: Boolean? = false
    override fun toString(): String {
        return ToStringBuilder(this).append("id", id).append("childId", childId)
            .append("submittedBy", submittedBy).append("whatHappened", whatHappened)
            .append("teacherComment", teacherComment).append("media", media)
            .append("deletedAt", deletedAt).append("createdAt", createdAt)
            .append("updatedAt", updatedAt).toString()
    }
}

class ChildAttendance : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("attendance_date")
    @Expose
    var attendanceDate: String? = null

    @SerializedName("arrival_time")
    @Expose
    var arrivalTime: String? = null

    @SerializedName("departure_time")
    @Expose
    var departureTime: String? = null

    @SerializedName("dropped_off_by")
    @Expose
    var droppedOffBy: Int? = null

    @SerializedName("picked_up_by")
    @Expose
    var pickedUpBy: Int? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("attendence_day")
    @Expose
    var attendanceDay: String? = null


    @SerializedName("child_attendance_multiple")
    @Expose
    var childAttendanceMultiple: ArrayList<ChildAttendanceMultiple>? = null
}

class AbsentNote : Serializable {
    @SerializedName("child_id")
    @Expose
    val child_id: Int = -1

    @SerializedName("created_at")
    @Expose
    val created_at: String = ""

    @SerializedName("id")
    @Expose
    val id: Int = -1

    @SerializedName("is_absent")
    @Expose
    val is_absent: Int = -1

    @SerializedName("notes")
    @Expose
    val notes: String = ""

    @SerializedName("updated_at")
    @Expose
    val updated_at: String = ""
}

class ChildAttendanceMultiple : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_attendance_id")
    @Expose
    var childAttendanceId: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("submitted_by")
    @Expose
    var submittedBy: Int? = null

    @SerializedName("attendance_date")
    @Expose
    var attendanceDate: String? = null

    @SerializedName("arrival_time")
    @Expose
    var arrivalTime: String? = null

    @SerializedName("departure_time")
    @Expose
    var departureTime: String? = null

    @SerializedName("dropped_off_by")
    @Expose
    var droppedOffById: Int? = null

    @SerializedName("picked_up_by")
    @Expose
    var pickedUpById: Int? = null


    @SerializedName("dropped_by")
    @Expose
    var droppedOffBy: User? = null

    @SerializedName("picked_by")
    @Expose
    var pickedUpBy: User? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("attendence_day")
    @Expose
    var attendanceDay: String? = null
}



