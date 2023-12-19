package com.android.swaddle.response

import android.content.Context
import com.android.swaddle.networkManager.RetrofitClass
import com.codingpixel.a2i.response.ApiCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiController private constructor(var mConext: Context) {

    companion object {

        var apiController: ApiController? = null
        lateinit var context:  Context;

        fun getInstance(mContext: Context): ApiController {

            if (apiController == null) {
                apiController = ApiController(mContext)
            }

            context = mContext

            return apiController as ApiController

        }

    }

    suspend fun getAllTypeResponse(call: Any, apiCallBack: ApiCallBack, responseType: String) {

        withContext(Dispatchers.IO) {

            (call as Call<Any>).enqueue(object : Callback<Any> {

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        apiCallBack.onResponse(call, response, responseType)
                    } else {
                        apiCallBack.onSuccessFalse(call, response, responseType)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    apiCallBack.onFailed(call, t, responseType)
                }

            })

        }

    }

    //Auth


    suspend fun removeClassRoom(
        header: String,
        apiCallback: ApiCallBack,
        responseType: String,
        classroom_id: Int
    ){

        var response = RetrofitClass.getInstance().webRequestsInstance.removeClassRoom(
            header,classroom_id
        )
        getAllTypeResponse(response, apiCallback, responseType)
    }
}