package com.android.swaddle.activities.parent

import android.Manifest
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.activities.providers.messages.NewMessageActivity
import com.android.swaddle.adapters.payment_adapters.FilesAdapter
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityParentDailyBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.slider.MediaSliderActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.codekidlabs.storagechooser.StorageChooser
import kotlinx.android.synthetic.main.activity_add.*
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ParentDailyActivity : BaseActivity(), DateAdapter.ClickListener {
    private lateinit var binding: ActivityParentDailyBinding

    val days = ArrayList<CalendarModel>()
    var selectedTheme: Activities? = null
    var selectedLiteracy: Activities? = null
    var selectedScience: Activities? = null
    var daysAdapter: DateAdapter? = null
    var date = ""
    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1
    var downloadedPosition = 0
    var downloadedPath = ""

    var selectedChild: ChildInfo? = null
    var childesList = ArrayList<ChildInfo>()
    private var childesAdapter: SelectChildSpinnerAdapter? = null

    private var builder = StorageChooser.Builder()

    private var documentThemeList: ArrayList<String> = ArrayList()
    private var themeAdapter: FilesAdapter? = null

    private var documentLiteracy: ArrayList<String> = ArrayList()
    private var documentLiteracyAdapter: FilesAdapter? = null

    private var documentMatScience: ArrayList<String> = ArrayList()
    private var adapterMathScience: FilesAdapter? = null

    private var chooser: StorageChooser? = null
    var formats = ArrayList<String>()
    private var chooserType = ""
    var classroom_id: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        allDaysOfWeek()
        initChooser()
        setThemeRecView()
        setReadingRec()
        setMathRec()
        callAPItoGetChildes()
        listeners()
        showAlertMessage()
    }
    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
    }
    private fun showAlertMessage() {
//        if (userManager.user?.type == "parent") {
//            callApiToGetCenterInformation()
//        }
    }

    private fun callApiToGetCenterInformation() {
        val call: Call<CenterInfoResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getCenterInformation("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<CenterInfoResponse> {
            override fun onResponse(
                call: Call<CenterInfoResponse>,
                response: Response<CenterInfoResponse>
            ) {
                if (response.isSuccessful) {
                    val baseModel = response.body()
                    if (baseModel?.status == true) {
                        if (baseModel.data?.provider?.message.toString().isNotEmpty()) {
                            val message = baseModel.data?.provider?.message.toString()
                            if (message.isNotEmpty()) {
                                binding.tvAlertMessage.text = message
                                binding.lnrMessage.viewVisible()
                            } else {
                                binding.lnrMessage.viewGone()
                            }
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<CenterInfoResponse>,
                t: Throwable
            ) {

            }
        })
    }


    private fun setMathRec() {
        adapterMathScience = FilesAdapter(this, documentMatScience, true)
        binding.recMathScience.adapter = adapterMathScience
        adapterMathScience?.setListener(object : FilesAdapter.CLickListener {
            override fun onItemClick(pos: Int) {

                val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()
                documentMatScience.forEachIndexed { index, model ->
                    mMediaArray.add(
                        MediaSliderActivity.MediaModel(
                            Constants.IMG_BASE_PATH + model ?: "",
                            "image",
                            Constants.IMG_BASE_PATH + model ?: ""
                        )
                    )

                }

                loadMediaSliderView(
                    mMediaArray,
                    pos,
                    "Activity",
                    HIDE_DOTS = true
                )
            }

            override fun onRemoveClick(pos: Int) {
                if (documentMatScience[pos].contains("storage/childs/activity_files")) {
                    if (selectedScience != null)
                        callAPItoRemoveMedia(documentMatScience[pos], "${selectedScience?.id}")
                } else {
                    documentMatScience.removeAt(pos)
                    adapterMathScience?.setData(documentMatScience)
                }
            }

            override fun onDownloadClick(position: Int) {
//                permissionAccess(
//                    position,
//                    Constants.IMG_BASE_PATH + (documentMatScience[position] ?: "")
//                )
                downloadedPosition = position
                downloadedPath = Constants.IMG_BASE_PATH + (documentMatScience[position] ?: "")
                openFileInBrowser(
                    downloadedPosition,
                    Constants.IMG_BASE_PATH + documentMatScience[position] ?: ""
                )
            }
        })
    }


    private fun setThemeRecView() {
        themeAdapter = FilesAdapter(this, documentThemeList, true)
        binding.recThemes.adapter = themeAdapter

        themeAdapter?.setListener(object : FilesAdapter.CLickListener {
            override fun onItemClick(pos: Int) {
                val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()
                documentThemeList.forEachIndexed { index, model ->
                    mMediaArray.add(
                        MediaSliderActivity.MediaModel(
                            Constants.IMG_BASE_PATH + model ?: "",
                            "image",
                            Constants.IMG_BASE_PATH + model ?: ""
                        )
                    )

                }

                loadMediaSliderView(
                    mMediaArray,
                    pos,
                    "Activity",
                    HIDE_DOTS = true
                )
            }

            override fun onRemoveClick(pos: Int) {
                if (documentThemeList[pos].contains("storage/childs/activity_files")) {
                    if (selectedTheme != null)
                        callAPItoRemoveMedia(documentThemeList[pos], "${selectedTheme?.id}")
                } else {
                    documentThemeList.removeAt(pos)
                    themeAdapter?.setData(documentThemeList)
                }
            }

            override fun onDownloadClick(position: Int) {
//                permissionAccess(
//                    position,
//                    Constants.IMG_BASE_PATH + (documentThemeList[position] ?: "")
//                )
                Log.e("position", position.toString())
                downloadedPosition = position
                downloadedPath = Constants.IMG_BASE_PATH + (documentThemeList[position] ?: "")

                openFileInBrowser(
                    downloadedPosition,
                    Constants.IMG_BASE_PATH + documentThemeList[position] ?: ""
                )

            }

        })
    }

    private fun setReadingRec() {
        documentLiteracyAdapter = FilesAdapter(this, documentLiteracy, true)
        binding.recLitracy.adapter = documentLiteracyAdapter
        documentLiteracyAdapter?.setListener(object : FilesAdapter.CLickListener {
            override fun onItemClick(pos: Int) {
                val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()
                documentLiteracy.forEachIndexed { index, model ->
                    mMediaArray.add(
                        MediaSliderActivity.MediaModel(
                            Constants.IMG_BASE_PATH + model ?: "",
                            "image",
                            Constants.IMG_BASE_PATH + model ?: ""
                        )
                    )

                }

                loadMediaSliderView(
                    mMediaArray,
                    pos,
                    "Activity",
                    HIDE_DOTS = true
                )
            }

            override fun onRemoveClick(pos: Int) {
                if (documentLiteracy[pos].contains("storage/childs/activity_files")) {
                    if (selectedLiteracy != null) {
//                        val alert =
//                            AlertView(
//                                "Remove?",
//                                "Are you sure you want to Remove file?",
//                                AlertStyle.DIALOG
//                            )
//                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
//                            //=call api to remove from Classroom...
//                            callAPItoRemoveMedia(documentLiteracy[pos], "${selectedLiteracy?.id}")
//                        })
//                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
//                        })
//                        alert.show(this@ParentDailyActivity)

                    }

                } else {
                    documentLiteracy.removeAt(pos)
                    documentLiteracyAdapter?.setData(documentLiteracy)
                }
            }

            override fun onDownloadClick(position: Int) {
//                permissionAccess(
//                    position,
//                    Constants.IMG_BASE_PATH + (documentLiteracy[position] ?: "")
//                )
                downloadedPosition = position
                downloadedPath = Constants.IMG_BASE_PATH + (documentLiteracy[position] ?: "")

                openFileInBrowser(
                    downloadedPosition,
                    Constants.IMG_BASE_PATH + documentLiteracy[position] ?: ""
                )
            }
        })
    }

    private fun initChooser() {
        formats.add("txt")
        formats.add("jpg")
        formats.add("jpeg")
        formats.add("xlx")
        formats.add("xlxs")
        formats.add("doc")
        formats.add("docx")
        formats.add("png")

        builder.customFilter(formats)

        builder.withActivity(this)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .customFilter(formats)
            .build()

        chooser = builder.build()
        chooser?.setOnMultipleSelectListener { paths ->
            Log.e("SELECTED_PATH", paths.size.toString())
            if (chooserType == "theme") {
                documentThemeList.addAll(paths)
                themeAdapter?.setData(documentThemeList)
            } else if (chooserType == "read") {
                documentLiteracy.addAll(paths)
                documentLiteracyAdapter?.setData(documentLiteracy)
            } else {
                documentMatScience.addAll(paths)
                adapterMathScience?.setData(documentMatScience)
            }
        }
        chooser?.setOnSelectListener { path ->
            Log.e("SELECTED_PATH", path)

            if (chooserType == "theme") {
                documentThemeList.addAll(listOf(path))
                themeAdapter?.setData(documentThemeList)
            } else if (chooserType == "read") {
                documentLiteracy.addAll(listOf(path))
                documentLiteracyAdapter?.setData(documentLiteracy)
            } else {
                documentMatScience.addAll(listOf(path))
                adapterMathScience?.setData(documentMatScience)
            }
        }
    }

    private fun callAPItoActivities(
    ) {
        binding.tvAlertMessage.text = ""
        binding.lnrMessage.viewGone()
        val call: Call<GetActivitiesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getActivities(
                "Bearer " + userManager.accessToken ?: "",
                "${selectedChild?.id}", "", date
            )
        call.enqueue(object : Callback<GetActivitiesResponse> {
            override fun onResponse(
                call: Call<GetActivitiesResponse>,
                response: Response<GetActivitiesResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()

                documentThemeList.clear()
                themeAdapter?.setData(documentThemeList)
                themeAdapter?.notifyDataSetChanged()
                binding.etThemeDesc.setText("")

                documentMatScience.clear()
                adapterMathScience?.setData(documentMatScience)
                adapterMathScience?.notifyDataSetChanged()
                binding.etScienceDesc.setText("")

                documentLiteracy.clear()
                documentLiteracyAdapter?.setData(documentLiteracy)
                documentLiteracyAdapter?.notifyDataSetChanged()
                binding.etReadingDesc.setText("")


                if (response.body()?.status == true) {
                    if (response.body()?.data.isNullOrEmpty()) {
                        binding.tvThemeHead.viewGone()
                        binding.cvTheme.viewGone()
                        binding.tvReading.viewGone()
                        binding.cvreading.viewGone()
                        binding.tvMath.viewGone()
                        binding.cvMath.viewGone()
                        binding.lnrMessage.viewGone()
                        binding.noData.viewVisible()
                    } else {
                        binding.tvThemeHead.viewVisible()
                        binding.cvTheme.viewVisible()
                        binding.tvReading.viewVisible()
                        binding.cvreading.viewVisible()
                        binding.tvMath.viewVisible()
                        binding.cvMath.viewVisible()
                        binding.lnrMessage.viewVisible()
                        binding.noData.viewGone()

                        setActivitiesData(response.body()?.data)
                    }
                } else {
                    binding.tvThemeHead.viewGone()
                    binding.cvTheme.viewGone()
                    binding.tvReading.viewGone()
                    binding.cvreading.viewGone()
                    binding.tvMath.viewGone()
                    binding.cvMath.viewGone()
                    binding.lnrMessage.viewGone()
                    binding.cvMath.viewGone()
                    binding.noData.viewVisible()
                }

            }

            override fun onFailure(
                call: Call<GetActivitiesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentDailyActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setActivitiesData(data: ArrayList<Activities>?) {
        data?.forEachIndexed { index, activities ->
            when (activities.activityType) {
                "activity_message" -> {
                    if (!activities.activity_message.isNullOrBlank()) {
                        binding.tvAlertMessage.text = activities.activity_message
                        binding.lnrMessage.viewVisible()
                    } else {
                        binding.lnrMessage.viewGone()
                        binding.tvAlertMessage.text = ""
                    }
                }
                "Theme" -> {
                    if (activities.activityFiles != null) {
                        documentThemeList.clear()
                        var files = JSONArray(activities.activityFiles ?: "")
                        for (i in 0 until files.length()) {
                            documentThemeList.add(files[i].toString())
                        }
                        themeAdapter?.setData(documentThemeList)
                        themeAdapter?.notifyDataSetChanged()
                        if (documentThemeList.size > 0) {
                            binding.tvNoThemesData.viewGone()
                        } else {
                            binding.tvNoThemesData.viewVisible()
                        }
                    }
                    if (activities.comment != null) {
                        binding.etThemeDesc.text = activities.comment ?: ""
                        if (activities.comment!!.isBlank()) {
                            binding.etThemeDesc.viewGone()
                            binding.tvThemeAdditionalComment.viewGone()
                        }

                    } else {
                        binding.etThemeDesc.viewGone()
                        binding.tvThemeAdditionalComment.viewGone()
                    }
                    selectedTheme = activities
                }
                "Reading/Literacy" -> {
                    if (activities.activityFiles != null) {
                        documentLiteracy.clear()
                        var files = JSONArray(activities.activityFiles ?: "")
                        for (i in 0 until files.length()) {
                            documentLiteracy.add(files[i].toString())
                        }
                        documentLiteracyAdapter?.setData(documentLiteracy)
                        documentLiteracyAdapter?.notifyDataSetChanged()
                    }
                    if (documentLiteracy.size > 0) {
                        binding.tvNoLitracyData.viewGone()
                    } else {
                        binding.tvNoLitracyData.viewVisible()
                    }
                    if (activities.comment != null) {
                        binding.etReadingDesc.setText(activities.comment ?: "")
                        if (activities.comment!!.isBlank()) {
                            binding.etReadingDesc.viewGone()
                            binding.tvReadingAdditionalComment.viewGone()
                        }

                    } else {
                        binding.etReadingDesc.viewGone()
                        binding.tvReadingAdditionalComment.viewGone()
                    }
                    selectedLiteracy = activities
                }
                else -> {
                    if (activities.activityFiles != null) {
                        documentMatScience.clear()
                        var files = JSONArray(activities.activityFiles ?: "")
                        for (i in 0 until files.length()) {
                            documentMatScience.add(files[i].toString())
                        }
                        adapterMathScience?.setData(documentMatScience)
                        adapterMathScience?.notifyDataSetChanged()
                    }
                    if (activities.comment != null) {
                        binding.etScienceDesc.setText(activities.comment ?: "")
                    }
                    selectedScience = activities

//                    if (activities.activityFiles != null) {
//                        documentMatScience.clear()
//                        var files = JSONArray(activities.activityFiles ?: "")
//                        for (i in 0 until files.length()) {
//                            documentMatScience.add(files[i].toString())
//                        }
//                        adapterMathScience?.setData(documentMatScience)
//                        adapterMathScience?.notifyDataSetChanged()
//                    }
//                    if (documentMatScience.size > 0) {
//                        binding.tvNoMathScienceData.viewGone()
//                    } else {
//                        binding.tvNoMathScienceData.viewVisible()
//                    }
//                    if (activities.comment != null) {
//                        binding.etScienceDesc.setText(activities.comment ?: "")
//
//                        if (activities.comment!!.isBlank()) {
//                            binding.etScienceDesc.viewGone()
//                            binding.tvMathAdditionalComment.viewGone()
//                        }
//                    } else {
//                        binding.etScienceDesc.viewGone()
//                        binding.tvMathAdditionalComment.viewGone()
//                    }
//                    selectedScience = activities
                }
            }
        }
    }

    private fun callAPItoGetChildes() {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesList("Bearer " + userManager.accessToken ?: "", "0")
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ParentDailyActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@ParentDailyActivity, childesList)
            binding.spChild.adapter = childesAdapter
        } else {
            childesAdapter?.setItems(childesList)
            childesAdapter?.notifyDataSetChanged()
        }

        binding.spChild.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (childesList.size > 0) {
                    selectedChild = childesList[position]
                    showLoading()
                    callAPItoActivities()
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            if(selectedChild?.id == -1)
                selectedChild = childesList.first()
            if(classroom_id != -1) {
                childesList.forEachIndexed { index, item ->
                    if (item.classroomId == classroom_id) {
                        binding.spChild.setSelection(index)
                        selectedChild = item
                    }
                }
                classroom_id = -1
            }
            //callAPItoActivities()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            selectedChild = null
        }
    }


    private fun listeners() {
        binding.llSendMessge.setOnClickListener {
            startActivity<NewMessageActivity>()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnChooseThemeFile.setOnClickListener {
            chooserType = "theme"
            chooser?.show()
        }

        binding.btnLiteracy.setOnClickListener {
            chooserType = "read"
            chooser?.show()
        }

        binding.btnMath.setOnClickListener {
            chooserType = "math"
            chooser?.show()
        }

        binding.tvSubmitTheme.setOnClickListener {
            if (documentThemeList.size > 0) {
                callAPiToUpdate(
                    documentThemeList,
                    binding.etThemeDesc.text.toString().trim(),
                    binding.tvSubmitTheme,
                    binding.progressTheme,
                    "Theme"
                )
            } else {
                showErrorToast(
                    this@ParentDailyActivity,
                    "Please Choose at least one file for Theme!"
                )
            }
        }

        binding.tvSubmitReading.setOnClickListener {
            if (documentLiteracy.size > 0) {
                callAPiToUpdate(
                    documentLiteracy,
                    binding.etReadingDesc.text.toString().trim(),
                    binding.tvSubmitReading,
                    binding.progressReading,
                    "Reading/Literacy"
                )
            } else {
                showErrorToast(
                    this@ParentDailyActivity,
                    "Please Choose at least one file for Reading/Literacy!"
                )
            }
        }

        binding.tvSubmitScience.setOnClickListener {
            if (documentMatScience.size > 0) {
                callAPiToUpdate(
                    documentMatScience,
                    binding.etScienceDesc.text.toString().trim(),
                    binding.tvSubmitScience,
                    binding.progressScience,
                    "Math/Science"
                )
            } else {
                showErrorToast(
                    this@ParentDailyActivity,
                    "Please Choose at least one file for Math/Science!"
                )
            }
        }
    }

//    private fun allDaysOfWeek() {
//        val calendar = Calendar.getInstance()
//        val today = Calendar.getInstance()
//        calendar.firstDayOfWeek = Calendar.MONDAY
//        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
//        var toDayAdded = false
//        //  val days = arrayOfNulls<String>(7)
//        for (i in 0..6) {
//            //days[i] = format.format(calendar.time)
//            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
//                toDayAdded = true
//                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))
//
//                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
//                val day = DateFormat.format("dd", days[i].date) as String // 20
//                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
//                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
//                val year = DateFormat.format("yyyy", days[i].date) as String // 2013
//
//                date = "$day-$monthNumber-$year"
//                Log.e("date", date)
//            } else {
//                if (toDayAdded)
//                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
//                else
//                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
//            }
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1)
//            Log.i("adsf", "" + days[i])
//        }
//
//        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        binding.recDate.layoutManager = manager
//        daysAdapter = DateAdapter(this, days, this)
//        binding.recDate.adapter = daysAdapter
//    }


    private fun allDaysOfWeek() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        var toDayAdded = false
        //  val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            //days[i] = format.format(calendar.time)
            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                toDayAdded = true
                days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = true))

                val dayOfTheWeek = DateFormat.format("EEEE", days[i].date) as String // Thursday
                val day = DateFormat.format("dd", days[i].date) as String // 20
                val monthString = DateFormat.format("MMM", days[i].date) as String // Jun
                val monthNumber = DateFormat.format("MM", days[i].date) as String // 06
                val year = DateFormat.format("yyyy", days[i].date) as String // 2013

                date = "$day-$monthNumber-$year"
                Log.e("date", date)
            } else {
                if (toDayAdded)
                    days.add(CalendarModel(calendar.time, isEnabled = false, isSelected = false))
                else
                    days.add(CalendarModel(calendar.time, isEnabled = true, isSelected = false))
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Log.i("adsf", "" + days[i])
        }

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recDate.layoutManager = manager
        daysAdapter = DateAdapter(this, days, this)
        binding.recDate.adapter = daysAdapter
    }

    private fun callAPItoRemoveMedia(media: String, id: String) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteActivityMedia("Bearer " + userManager.accessToken ?: "", id, media)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {

                    showSuccessToast(
                        this@ParentDailyActivity,
                        response.body()?.message ?: ""
                    )
                    callAPItoActivities()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@ParentDailyActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@ParentDailyActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@ParentDailyActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPiToUpdate(
        docs: ArrayList<String>,
        desc: String,
        tv: TextView,
        pro: ProgressBar, type: String
    ) {
        showProgressBar(tv, pro)
        var api =
            AndroidNetworking.upload(com.android.swaddle.utils.Constants.SERVER_ADDRESS_NEW + "add_or_update_child_activity")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")

        var files = ArrayList<File>()
        if (docs.size > 0) {
            docs.forEachIndexed { index, s ->
                val fileURI = Uri.parse(s)
                val file = File(fileURI.path ?: "")
                files.add(file)
            }
            api.addMultipartFileList("files[]", files)
        }
        api.addMultipartParameter("child_id", (selectedChild?.id ?: 0).toString())
        api.addMultipartParameter("activity_type", type)
        api.addMultipartParameter("activity_date", date)
        api.addMultipartParameter("comment", desc)

        api.setTag("add_update_incident_report")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("update_incident_report", response.toString())

                    var data = gson.fromJson(response.toString(), LoginResponse::class.java)

                    if (data.status == true) {
                        showSuccessToast(this@ParentDailyActivity, data.message ?: "")

                    } else {
                        showErrorToast(this@ParentDailyActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar(tv, pro)
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar(tv, pro)
                    error.printStackTrace()
                    showErrorToast(this@ParentDailyActivity, error.message ?: "")
                }
            })
    }

    private fun permissionAccess(position: Int, path: String) {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1)
            requestPermission(p1)
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2)
            requestPermission(p2)
        } else {

            val fileName = (path).split("/").last()

            val request = DownloadManager.Request(
                Uri.parse(path)
            )
            request.setTitle("Swaddle-$fileName").setDescription("Downloading is in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
                .setMimeType(".pdf")
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this@ParentDailyActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@ParentDailyActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ParentDailyActivity,
                arrayOf(permission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            //Do the stuff that requires permission...
            Log.e("TAG", "Not say request")
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                Log.e("TAG", "val " + grantResults[0])
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionAccess(downloadedPosition, downloadedPath)
                } else {
                    Toast.makeText(
                        this@ParentDailyActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onITemClick(position: Int, item: CalendarModel) {
        if (item.isEnabled) {
            days.forEachIndexed { _, calendarModel ->
                calendarModel.isSelected = false
            }
            days[position].isSelected = !days[position].isSelected
            daysAdapter?.notifyDataSetChanged()
            val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
            val day = DateFormat.format("dd", item.date) as String // 20
            val monthString = DateFormat.format("MMM", item.date) as String // Jun
            val monthNumber = DateFormat.format("MM", item.date) as String // 06
            val year = DateFormat.format("yyyy", item.date) as String // 2013

            date = "$day-$monthNumber-$year"
            Log.e("date", date)

            if (date != "" && selectedChild != null) {
                showLoading()
                callAPItoActivities()
            }
        }
    }

    private fun showProgressBar(tv: TextView, pro: ProgressBar) {
        tv.viewGone()
        pro.viewVisible()
    }

    private fun hideProgressBar(tv: TextView, pro: ProgressBar) {
        tv.viewVisible()
        pro.viewGone()
    }
}