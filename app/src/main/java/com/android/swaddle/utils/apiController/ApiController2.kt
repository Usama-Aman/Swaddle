package com.android.swaddle.utils.apiController

import android.content.Context
import androidx.lifecycle.ViewModel
import com.android.swaddle.networkManager.RetrofitClass
import com.codingpixel.a2i.response.ApiCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiController2 private constructor(var mConext: Context) : ViewModel() {

    companion object {

        var apiController2: ApiController2? = null
        lateinit var context: Context;

        fun getInstance(mContext: Context): ApiController2 {

            if (apiController2 == null) {
                apiController2 = ApiController2(mContext)
            }

            context = mContext

            return apiController2 as ApiController2

        }

    }

    suspend fun getAllTypeResponse(call: Any, apiCallBack: ApiCallBack2, responseType: String) {

        withContext(Dispatchers.IO) {

            (call as Call<Any>).enqueue(object : Callback<Any> {

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        apiCallBack.onStatusTrue(call, response, responseType)
                    } else {
                        apiCallBack.onStatusFalse(call, response, responseType)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    apiCallBack.onResponseFailed(call, t, responseType)
                }

            })

        }

    }

    suspend fun subDailyReportNew(
        token: String,
        apiCallback: ApiCallBack2,
        responseType: String,
        requestBody: RequestBody
    ){


        var response =
            RetrofitClass.getInstance().webRequestsInstance
                .subDailyReportNew("Bearer $token",requestBody)

        getAllTypeResponse(response, apiCallback, responseType)

    }



}
