package com.android.swaddle.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class GetInvoicesResponse {
    @SerializedName("status")
    @Expose
    var status: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null
}

class ChildLatestInvoice :Serializable{
    var expanded: Boolean = false

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("child_id")
    @Expose
    var childId: Int? = null

    @SerializedName("created_by")
    @Expose
    var createdBy: Int? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("amount")
    @Expose
    var amount: String? = null

    @SerializedName("balance")
    @Expose
    var balance: Int? = null

    @SerializedName("due_date")
    @Expose
    var dueDate: String? = null

    @SerializedName("file_path")
    @Expose
    var filePath: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("bill_type")
    @Expose
    var billType: String? = null

    @SerializedName("is_recursive")
    @Expose
    var isRecursive: Int? = null

    @SerializedName("deleted_at")
    @Expose
    var deletedAt: Any? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("transaction_id")
    @Expose
    var transactionId: String? = null

    @SerializedName("paid_by")
    @Expose
    var paidBy: Any? = null

    @SerializedName("start_date")
    @Expose
    var startDate: String? = null

    @SerializedName("end_date")
    @Expose
    var endDate: String? = null

    @SerializedName("interval")
    @Expose
    var interval: String? = null

    @SerializedName("total_balance")
    @Expose
    var totalBalance: Int? = null

    @SerializedName("total_due")
    @Expose
    var total_due: Int? = null

    @SerializedName("total_paid")
    @Expose
    var total_paid: Int? = null
}


class Data {
    @SerializedName("cal_date_from")
    @Expose
    var calDateFrom: String? = null

    @SerializedName("cal_date_to")
    @Expose
    var calDateTo: String? = null

    @SerializedName("childs")
    @Expose
    var childs: ArrayList<ChildInfo>? = null

    @SerializedName("childs_data")
    @Expose
    var childesData: ArrayList<ChildInfo>? = null

    @SerializedName("classrooms")
    @Expose
    var classrooms: ArrayList<ClassroomDetails>? = null

    @SerializedName("child_latest_invoice")
    @Expose
    var childLatestInvoice: ArrayList<ChildLatestInvoice>? = null
}