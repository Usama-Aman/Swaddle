package com.android.swaddle.utils.apiController

interface ApiCallBack2 {

    fun onStatusTrue(call: Any? = null, response: Any, responseType: String)
    fun onStatusFalse(call: Any? = null, response: Any, responseType: String)
    fun onResponseFailed(call: Any? = null, t: Throwable, responseType: String)

}