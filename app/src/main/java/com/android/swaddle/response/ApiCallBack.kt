package com.codingpixel.a2i.response

interface ApiCallBack {

    fun onResponse(call: Any? = null, response: Any, responseType: String)
    fun onFailed(call: Any? = null, t: Throwable, responseType: String)
    fun onSuccessFalse(call: Any? = null, response: Any, responseType: String)

}