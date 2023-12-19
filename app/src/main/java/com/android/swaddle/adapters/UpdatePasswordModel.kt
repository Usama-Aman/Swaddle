package com.android.swaddle.adapters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

public class UpdatePasswordModel {
    @SerializedName("status")
    @Expose
    private var status: Boolean? = null

    @SerializedName("message")
    @Expose
    private var message: String? = null

    @SerializedName("data")
    @Expose
    private var data: Any? = null

    fun getStatus(): Boolean? {
        return status
    }

    fun setStatus(status: Boolean?) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getData(): Any? {
        return data
    }

    fun setData(data: Any?) {
        this.data = data
    }
}