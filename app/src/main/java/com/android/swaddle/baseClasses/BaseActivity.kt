package com.android.swaddle.baseClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.swaddle.BuildConfig
import com.android.swaddle.R
import com.android.swaddle.activities.common.SplashActivity
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.uploadFileResponse.UploadFileResponse
import com.android.swaddle.networkManager.IOSocketManager
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.onesignal.OneSignalNotificationManager
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.*
import com.android.swaddle.utils.slider.CacheDataSourceFactory
import com.android.swaddle.utils.slider.MediaSliderActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.onesignal.OneSignal
import com.onesignal.shortcutbadger.ShortcutBadger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


open class BaseActivity : AppCompatActivity() {
    lateinit var mViewGroup: ViewGroup
    lateinit var tinyDB: TinyDB
    var fontBold: Typeface? = null
    var fontSemiBold: Typeface? = null
    var fontRegular: Typeface? = null
    var fontLight: Typeface? = null
    public var sharedSocket = IOSocketManager()
    val gson = Gson()

    //    var firebaseAnalytics: FirebaseAnalytics? = null
    val userManager: LoginData
        get() {
            return UserData.user(this@BaseActivity)
        }
    private val pd = ProgressDialog.newInstance()
    open fun onSetupViewGroup(mVG: ViewGroup) {
        mViewGroup = mVG
        HideUtil.init(this, mViewGroup)
    }

    fun setFocus(view: View) {
        if (view.isFocused) {
            view.clearFocus()
        }
        view.requestFocus()
    }

    fun isToday(date: Date?): Boolean {
        val today = Calendar.getInstance()
        val specifiedDate = Calendar.getInstance()
        specifiedDate.time = date
        return today[Calendar.DAY_OF_MONTH] == specifiedDate[Calendar.DAY_OF_MONTH]
                && today[Calendar.MONTH] == specifiedDate[Calendar.MONTH]
                && today[Calendar.YEAR] == specifiedDate[Calendar.YEAR]
    }

     fun openFileInBrowser(downloadedPosition: Int, url: String) {

        Log.e(
            "filePath",
            url
        )
        //val i = Intent(Intent.ACTION_VIEW)
        //i.data = Uri.parse(url)
        //startActivity(i)
         // val webIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://docs.google.com/gview?embedded=true&url="+url))
         if(url.contains("pdf") || url.contains("doc") || url.contains("docx")){
             val webIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://docs.google.com/gview?embedded=true&url="+url))
             startActivity(webIntent)
         }
         else {
             val webIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
             startActivity(webIntent)
         }
    }
//    fun signUpEvent() {
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.METHOD, "sign_up")
//        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
//    }
//
//    fun loginEvent() {
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.METHOD, "login")
//        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
//    }
//
//    fun searchEvent() {
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.METHOD, "search")
//        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
//    }

    fun loadMediaSliderView(
        mediaUrlList: ArrayList<MediaSliderActivity.MediaModel>,
        startPosition: Int,
        title: String = "Gallery",
        isTitleVisible: Boolean = false,
        isMediaCountVisible: Boolean = false,
        isNavigationVisible: Boolean = true,
        titleBackgroundColor: String? = "",
        titleTextColor: String = "",
        HIDE_DOTS: Boolean = false
    ) {
        val intent = Intent(this@BaseActivity, MediaSliderActivity::class.java)
        intent.putExtra("MEDIA_LIST", mediaUrlList)
        intent.putExtra("TITLE_VISIBLE", isTitleVisible)
        intent.putExtra("MEDIA_COUNT_VISIBLE", isMediaCountVisible)
        intent.putExtra("NAVIGATION_VISIBLE", isNavigationVisible)
        intent.putExtra("TITLE", title)
        intent.putExtra("TITLE_BG", titleBackgroundColor)
        intent.putExtra("TITLE_CLR", titleTextColor)
        intent.putExtra("POSITION", startPosition)
        intent.putExtra("HIDE_DOTS", HIDE_DOTS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    fun setFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun setNoLayoutLimits() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showLoading(text: String = "Please wait...", cancelable: Boolean = false) {

        runOnUiThread {

            if (!pd.isAdded) {
                try {
                    if (!pd.isVisible && !pd.isAdded) {
                        pd.show(supportFragmentManager, "pd")
                    }
                    Handler().postDelayed({
                        pd.txtProgress.text = text
                        pd.isCancelable = cancelable
                    }, 200)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun updatedLoadingText(txtProgress: String) {
        runOnUiThread {
            try {
                Handler().postDelayed({
                    if (txtProgress.isNotEmpty()) {
                        pd.txtProgress.text = txtProgress
                    } else {
                        pd.txtProgress.text = "Please wait..."
                    }
                }, 200)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun hideLoading() {
        runOnUiThread {
            try {
                if (pd.isAdded) pd.dismiss()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun createProgressDialog(context: Context, message: String): android.app.ProgressDialog {
        val text: TextView
        val dialog = android.app.ProgressDialog(context)
        try {
            dialog.show()
        } catch (e: WindowManager.BadTokenException) {

        }
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.progress)
        text = dialog.findViewById(R.id.text)
        text.text = message

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tinyDB = TinyDB(this@BaseActivity)
//        firebaseAnalytics = FirebaseAnalytics.getInstance(this@BaseActivity)
        fontBold = Typeface.createFromAsset(assets, "font_bold.otf")
        fontSemiBold = Typeface.createFromAsset(assets, "font_semi_bold.otf")
        fontRegular =
            Typeface.createFromAsset(assets, "font_regular.ttf")
        fontLight =
            Typeface.createFromAsset(assets, "fon_light.otf")
        tinyDB = TinyDB(this@BaseActivity)

        // Adding an Network Interceptor for Debugging purpose :
        val loggingInterceptor = HttpLoggingInterceptor()

        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .addNetworkInterceptor(loggingInterceptor)
            .build()


        AndroidNetworking.initialize(applicationContext, okHttpClient)

        // Then set the JacksonParserFactory like below

        val gson = GsonBuilder()
            .setLenient()
            .create()
        AndroidNetworking.setParserFactory(GsonParserFactory(gson))

    }

    open class PlayerVUtil {
        companion object {

            var cacheFile: CacheDataSourceFactory? = null
            var isMuteVideo = true

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColor(color: Int) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = resources.getColor(color)

//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
//        window.statusBarColor = ContextCompat.getColor(BookReaderActivity.this,R.color.white);// set status background white
//
    }

    open fun setLightStatusBar(view: View, activity: BaseActivity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
            activity.window.statusBarColor = resources.getColor(color)
        }
    }

    open fun clearLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.statusBarColor = ContextCompat
                .getColor(activity, R.color.colorPrimaryDark)
        }
    }

    fun setStatusBarIconsView(myView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun isNetworkConnected(): Boolean {
        val cm: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }


    fun uploadFileToServer(
        filePath: String, callback: (String) -> Unit
    ) {
        val image = File(filePath)
        val file = MultipartBody.Part.createFormData(
            "file",
            image.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), image)
        )
//        val userId: RequestBody = RequestBody.create(
//            "multipart/form-data".toMediaTypeOrNull(),
//            ""
//        )

        val call: Call<UploadFileResponse>
        call = RetrofitClass.getInstance().webRequestsInstance.hitUploadFile(file)
        call.enqueue(object : Callback<UploadFileResponse> {
            override fun onResponse(
                call: Call<UploadFileResponse>,
                response: Response<UploadFileResponse>
            ) {
                runOnUiThread {
                    hideLoading()
                }
                Log.e("response", response.body()?.successData?.fullPath ?: "")

                if (response.body()?.status == 200) {
                    callback(response.body()?.successData?.fullPath ?: "")
                    showSuccessToast(
                        this@BaseActivity,
                        response.body()?.message.toString()
                    )
                } else {
                    showErrorToast(this@BaseActivity, response.message().toString())
                }
            }

            override fun onFailure(
                call: Call<UploadFileResponse>,
                t: Throwable
            ) {
                hideLoading()
                t.printStackTrace()
                Toast.makeText(this@BaseActivity, "Can't connect to server", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


//    fun vibrate() {
//        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= 26) {
//            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
//        } else {
//            vibrator.vibrate(5)
//        }
//    }

    fun playClickSound() {
        try {
            val mediaPlayer: MediaPlayer =
                MediaPlayer.create(applicationContext, R.raw.click_sound_low)
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.reset()
                mediaPlayer.release()
            }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun animateButton(view: View) {
        // Load the animation
        val myAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.bounce)
        val animationDuration = 200
        myAnim.duration = animationDuration.toLong()

        // Use custom animation interpolator to achieve the bounce effect
        val interpolator = CustomBounceInterpolator(1.0, 1.0)

        myAnim.interpolator = interpolator

        // Animate the button

        view.startAnimation(myAnim)
        //        playSound()

        // Run button animation again after it finished
        myAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}

            override fun onAnimationRepeat(arg0: Animation) {}

            override fun onAnimationEnd(arg0: Animation) {
                //                animateButton()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createThumbnailFromVideoPathAndroidQ(file: File, size: android.util.Size): Bitmap {
        return ThumbnailUtils.createVideoThumbnail(file, size, null)
    }

    fun createThumbnailFromVideoPath(filePath: String, type: Int): Bitmap {
        return ThumbnailUtils.createVideoThumbnail(filePath, type)!!
    }


    private fun logoutUser() {
        showLoading("Session Expired. Logging out...")
        val keysDel = ArrayList<String>()
        keysDel.add("user_id")
        keysDel.add("device_type")
        OneSignal.deleteTags(keysDel)
        OneSignal.deleteTags(keysDel, object : OneSignal.ChangeTagsUpdateHandler {
            override fun onSuccess(tags: JSONObject?) {
                hideLoading()
                OneSignalNotificationManager.removeUserTags()
                UserData.setUserLogin(this@BaseActivity, false)
                UserData.clearUserData(this@BaseActivity)
                OneSignal.clearOneSignalNotifications()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancelAll()
                ShortcutBadger.removeCount(this@BaseActivity)
                startActivity(intentFor<SplashActivity>().clearTop().clearTask().newTask())
                finishAffinity()
            }

            override fun onFailure(error: OneSignal.SendTagsError?) {
                hideLoading()
                OneSignalNotificationManager.removeUserTags()
                UserData.setUserLogin(this@BaseActivity, false)
                UserData.clearUserData(this@BaseActivity)
                OneSignal.clearOneSignalNotifications()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancelAll()
                ShortcutBadger.removeCount(this@BaseActivity)
                startActivity(intentFor<SplashActivity>().clearTop().clearTask().newTask())
                finishAffinity()
            }
        })
    }


    fun generateHoursList(): ArrayList<String> {
        val temp = ArrayList<String>()
        for (i in 1..12 step 1) {
            if (i < 10) {
                temp.add("0$i")
            } else {
                temp.add("$i")
            }
        }
        return temp
    }

    fun generateYearsList(): ArrayList<String> {
        val temp = ArrayList<String>()
        for (i in 2001..2099 step 1) {
            temp.add("$i")
        }
        return temp
    }


    fun generateDaysList(month: String = "JAN", year: String = "2001"): ArrayList<String> {
        val temp = ArrayList<String>()
        var isLeap = (year.toInt()) % 4 == 0
        when (month) {
            "JAN" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "FEB" -> {
                if (isLeap) {
                    for (i in 1..28 step 1) {
                        if (i < 10) {
                            temp.add("0$i")
                        } else {
                            temp.add("$i")
                        }
                    }
                } else {
                    for (i in 1..29 step 1) {
                        if (i < 10) {
                            temp.add("0$i")
                        } else {
                            temp.add("$i")
                        }
                    }
                }
            }
            "MAR" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "APR" -> {
                for (i in 1..30 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "MAY" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "JUN" -> {
                for (i in 1..30 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "JUL" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "AUG" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "SEP" -> {
                for (i in 1..30 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "OCT" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "NOV" -> {
                for (i in 1..30 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
            "DES" -> {
                for (i in 1..31 step 1) {
                    if (i < 10) {
                        temp.add("0$i")
                    } else {
                        temp.add("$i")
                    }
                }
            }
        }

        return temp
    }

    fun generateMonthList(): ArrayList<String> {
        val temp = ArrayList<String>()

        temp.add("JAN")
        temp.add("FEB")
        temp.add("MAR")
        temp.add("APR")
        temp.add("MAY")
        temp.add("JUN")
        temp.add("JUL")
        temp.add("AUG")
        temp.add("SEP")
        temp.add("OCT")
        temp.add("NOV")
        temp.add("DES")

        return temp
    }


    fun generateMinutesList(): ArrayList<String> {
        val temp = ArrayList<String>()
//        for (i in 0..59 step 1) {
//            if (i < 10) {
//                temp.add("0$i")
//            } else {
//                temp.add("$i")
//            }
//        }
        temp.add("00")
        temp.add("30")
        return temp
    }

    fun generateAmPmList(): ArrayList<String> {
        val temp = ArrayList<String>()
        temp.add("AM")
        temp.add("PM")
        return temp
    }


    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            val mime = MimeTypeMap.getSingleton()
            type = mime.getMimeTypeFromExtension(extension)
        }
        return type
    }

}

fun String.capitalized(): String {
    return this.substring(0, 1).toUpperCase(Locale.getDefault()) + this.substring(1)
}

fun <T> generateList(response: String, type: Class<Array<T>>): ArrayList<T> {
    val arrayList = ArrayList<T>()
    if (response.isEmpty() || response.equals("null") || response.equals("\"[]\"")) {
        return arrayList
    }
    arrayList.addAll(listOf(*Gson().fromJson<Array<T>>(response, type)))
    return arrayList
}

fun Any.toNotNullString(): String {
    //    Log.d("toString",this.toString())
    return if (this == "null") {
        this.toString().replace("null", "")
    } else {
        this.toString()
    }
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

fun roundOff(d: Float, decimalPlace: Int): Float {
    var bd = BigDecimal(java.lang.Float.toString(d))
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
    return bd.toFloat()
}


fun String.doubleToStringNoDecimal(d: Double): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
    formatter.applyPattern("#,###")
    return formatter.format(d)
}

fun String.getStringWhole(): String {
    return this.trim()
}

fun Editable.getStringWhole(): String {
    return this.toString().trim()
}

fun String.isValidEmail(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhone(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.PHONE.matcher(this).matches()
}

fun getAbbreviatedFromDateTime(dateTime: String, format: String = "yyyy-MM-dd"): Date? {
    val input = SimpleDateFormat(format)
    return try {
        input.parse(dateTime)    // parse input
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


