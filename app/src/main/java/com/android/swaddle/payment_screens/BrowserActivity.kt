package com.android.swaddle.payment_screens

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassRoomsResponse
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.networkManager.NetworkURLs.Companion.stripe_secretKey
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.CustomToasts


import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException

class BrowserActivity : BaseActivity() {
    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ivAddNewCard: ImageView
    private lateinit var topBar: RelativeLayout
    private lateinit var webView: WebView
    private lateinit var llContent: LinearLayout


    //    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var content: RelativeLayout
    private var url = ""
    private var title = ""

    private lateinit var request: Request
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        supportActionBar?.hide()
//        showLoading()
        url = intent?.getStringExtra("url") ?: ""
        title = intent?.getStringExtra("title") ?: ""
        if (url == "") {
            url =
                "https://connect.stripe.com/express/oauth/authorize?redirect_uri=${NetworkURLs.stripe_redirectURL} +&client_id=${NetworkURLs.stripe_clientId}&scope=read_write&"
            Log.e("url", url)
        }
        initIds()
        setLightStatusBar(content, this@BrowserActivity, R.color.colorWhite)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, mUrl: String?): Boolean {
//                hideLoading()
                Log.e("url", mUrl ?: "")
                if ((mUrl ?: "").contains("/swaddle-web/home")) {

                    val list = (mUrl ?: "").split("=")
                    val stateCode = list.last()
                    val code = list[1].split("&").first()
                    Log.d("status_code", stateCode)
                    Log.d("code", code)
                    callAPItoSendStripe(code)


//                    CustomToasts.successToast(
//                        applicationContext,
//                        "Stripe Connected Successfully!"
//                    )
//                    userManager.user?.isBankAccountVerified = 1
//                    finish()
                }
//                if (mUrl?.contains("collegiateelites.app/stripe_redirect_uri") == true) {
                webView.loadUrl(mUrl ?: "")
//                }
//                    val list = url.split("=")
//                    val stateCode = list.last()
//                    val code = list[1].split("&").first()
//                    Log.d("status_code", stateCode)
//                    Log.d("code", code)
//                    addStripeConnectAccount(code)
//                } else {
//                    tvTitle.text = url
//                    view?.loadUrl(url)
//                }
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                hideLoading()
                Log.e("test", error.toString())
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                hideLoading()
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e("test", errorResponse.toString())
            }
        }
    }

    private fun callAPItoSendStripe(code: String) {
        showLoading()
        val call: retrofit2.Call<LoginResponse> =
            RetrofitClass.getInstance().webRequestsInstance
                .stripeConnect("Bearer " + userManager.accessToken ?: "", code)
        call.enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                if (response.body()?.status == true) {
                    Log.e("response", response.body().toString())
                    showSuccessToast(this@BrowserActivity, response.body()?.message ?: "")
                    userManager.user?.isBankAccountVerified = 1
                    finish()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@BrowserActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@BrowserActivity, response.body()?.message ?: "")
                    }
                }
                hideLoading()
            }

            override fun onFailure(
                call: retrofit2.Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@BrowserActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    fun addStripeConnectAccount(code: String) {
        showLoading()
        client = OkHttpClient()
        val urlBuilder = NetworkURLs.STRIPE_TOKEN.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "authorization_code")
            .addFormDataPart("client_secret", stripe_secretKey).addFormDataPart("code", code)
            .build()
        request = Request.Builder().header("Authorization", "Bearer $stripe_secretKey").url(url)
            .post(requestBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailureEncryptedPasswordFromServer()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onFailureEncryptedPasswordFromServer()
                    throw  IOException("Unexpected code $response")
                } else {
                    onResponseEncryptedPasswordFromServer(response.body!!.string().toString())
                }
            }
        })
    }

    private fun onFailureEncryptedPasswordFromServer() {
        Log.d("respisner", "rooro")
    }

    @SuppressLint("SetTextI18n")
    private fun onResponseEncryptedPasswordFromServer(response: String) {
        Log.d("response", response)
        runOnUiThread {

            val json = JSONObject(response)
            val stripeConnectId = json.getString("stripe_user_id")
            val stripePublishableKey = json.getString("stripe_publishable_key")
            val accessToken = json.getString("access_token")
            val urlPath = "https://api.stripe.com/v1/accounts/$stripeConnectId/login_links"
            val urlBuilder = urlPath.toHttpUrlOrNull()!!.newBuilder()
            val requestBody = FormBody.Builder().build()
            val url = urlBuilder.build().toString()
            request = Request.Builder().header("Authorization", "Bearer $stripe_secretKey").url(url)
                .post(requestBody).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailureEncryptedPasswordFromServer()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onFailureEncryptedPasswordFromServer()
                        throw  IOException("Unexpected code $response")
                    } else {
                        val responseObj = JSONObject(response.body?.string().toString())
                        val stripeUrl = responseObj.getString("url")
                        val query =
                            "update users SET  account_status = '1' , stripe_payout_account_id = '$stripeConnectId' , is_bank_account_verified = '1' ,stripe_express_dashboard_url = '$stripeUrl' ,  stripe_payout_account_public_key = '$stripePublishableKey',stripe_payout_account_secret_key = '$accessToken'  where  id = '${
                                " UserData.user(this@DuetBrowserActivity).id"
                            }'"
                        Log.d(
                            "Stripe_Data",
                            "$stripeConnectId ,$stripePublishableKey ,  $accessToken , $stripeUrl"
                        )
                        Log.d("query", query)

                        CustomToasts.successToast(
                            applicationContext,
                            "Stripe Connected Successfully!"
                        )
                        finish()
                    }
                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initIds() {
        onSetupViewGroup(findViewById<RelativeLayout>(R.id.content))

        ivBack = findViewById<ImageView>(R.id.ivBack)
        tvTitle = findViewById<TextView>(R.id.tvTitle)
        ivAddNewCard = findViewById<ImageView>(R.id.ivAddNewCard)
        topBar = findViewById<RelativeLayout>(R.id.topBar)
        webView = findViewById<WebView>(R.id.webView)
        llContent = findViewById<LinearLayout>(R.id.llContent)
//        swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh)
        content = findViewById<RelativeLayout>(R.id.content)

        initListeners()
//        showLoading("Leading stripe...")
        loadWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        if (title == "") {
            tvTitle.viewGone()
        } else {
            tvTitle.viewVisible()
            tvTitle.text = title
        }
        webView.settings.javaScriptEnabled = true
        @RequiresApi(Build.VERSION_CODES.O)
        webView.settings.safeBrowsingEnabled = true
        webView.settings.setAppCacheEnabled(true)
        Log.e("path", url)

        webView.loadUrl(url)
    }

    private fun initListeners() {
        ivBack.setOnClickListener { finish() }
    }
}