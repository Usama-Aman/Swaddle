package com.android.swaddle.activities.providers

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.payment_adapters.FilesAdapter
import com.android.swaddle.adapters.provider_adapter.DateAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddBinding
import com.android.swaddle.databinding.DialogPickImageBinding
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
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vlk.multimager.activities.GalleryActivity
import com.vlk.multimager.activities.MultiCameraActivity
import com.vlk.multimager.utils.Image
import com.vlk.multimager.utils.Params
import kotlinx.android.synthetic.main.activity_add.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AddActivity : BaseActivity(), DateAdapter.ClickListener {
    private lateinit var binding: ActivityAddBinding


    val days = ArrayList<CalendarModel>()
    var selectedTheme: Activities? = null
    var selectedLiteracy: Activities? = null
    var selectedScience: Activities? = null
    var classRoomsList = ArrayList<ClassroomDetails>()
    var daysAdapter: DateAdapter? = null
    var selectedClassRoom: ClassroomDetails? = null
    var date = ""
    var downloadedPosition = 0
    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1

    private var classRoomsAdapter: ClassSpinnerAdapter? = null
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
    var selectedPos = 0
    var classroom_id: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedPos = intent.getIntExtra("position", 0)
        getIntentData()
        allDaysOfWeek()
        initChooser()
        setThemeRecView()
        setReadingRec()
        setMathRec()
        callAPItoGetClassRooms()
        listeners()
    }

    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
    }


    private fun setMathRec() {
        adapterMathScience = FilesAdapter(this, documentMatScience)
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
                    if (selectedScience != null) {
                        val alert =
                            AlertView(
                                "Remove?",
                                "Are you sure you want to Remove file?",
                                AlertStyle.DIALOG
                            )
                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                            //=call api to remove from Classroom...
                            callAPItoRemoveMedia(documentMatScience[pos], "${selectedScience?.id}")
                        })
                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                        })
                        alert.show(this@AddActivity)

                    }
                } else {
                    documentMatScience.removeAt(pos)
                    adapterMathScience?.setData(documentMatScience)
                }
            }

            override fun onDownloadClick(position: Int) {

            }
        })
    }

    private fun setThemeRecView() {
        themeAdapter = FilesAdapter(this, documentThemeList)
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
                    if (selectedTheme != null) {
                        val alert =
                            AlertView(
                                "Remove?",
                                "Are you sure you want to Remove file?",
                                AlertStyle.DIALOG
                            )
                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                            //=call api to remove from Classroom...
                            callAPItoRemoveMedia(documentThemeList[pos], "${selectedTheme?.id}")
                        })
                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                        })
                        alert.show(this@AddActivity)

                    }
                } else {
                    documentThemeList.removeAt(pos)
                    themeAdapter?.setData(documentThemeList)
                }
            }

            override fun onDownloadClick(position: Int) {

                permissionAccess(position)
                Log.e("click", "${permissionAccess(position)}")
                downloadedPosition = position

            }

        })
    }

    private fun permissionAccess(position: Int) {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1)
            requestPermission(p1)
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2)
            requestPermission(p2)
        } else {
            Log.e(
                "filePath", Constants.IMG_BASE_PATH + documentThemeList[position] ?: ""
            )
            val fileName = (documentThemeList[position] ?: "").split("/").last()

            val request = DownloadManager.Request(
                Uri.parse(Constants.IMG_BASE_PATH + (documentThemeList[position] ?: ""))
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
        val result = ContextCompat.checkSelfPermission(this@AddActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@AddActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@AddActivity,
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
                    permissionAccess(downloadedPosition)
                } else {
                    Toast.makeText(
                        this@AddActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setReadingRec() {
        documentLiteracyAdapter = FilesAdapter(this, documentLiteracy)
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
                        val alert =
                            AlertView(
                                "Remove?",
                                "Are you sure you want to Remove file?",
                                AlertStyle.DIALOG
                            )
                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                            //=call api to remove from Classroom...
                            callAPItoRemoveMedia(documentLiteracy[pos], "${selectedLiteracy?.id}")
                        })
                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                        })
                        alert.show(this@AddActivity)
                    }
                } else {
                    documentLiteracy.removeAt(pos)
                    documentLiteracyAdapter?.setData(documentLiteracy)
                }
            }

            override fun onDownloadClick(position: Int) {

            }
        })
    }

    private fun initChooser() {
//        formats.add("txt")
        formats.add("jpg")
        formats.add("jpeg")
//        formats.add("xlx")
//        formats.add("xlxs")
//        formats.add("doc")
//        formats.add("docx")
        formats.add("png")

//        builder.customFilter(formats)

        builder.withActivity(this)
            .customFilter(arrayListOf("jpg", "jpeg", "png"))
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
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

        chooser?.setOnCancelListener {
            Log.d("Chooser", "Cancel")
        }
    }

    private fun callAPItoGetClassRooms(
    ) {
        showLoading()
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getClassRooms("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoActivities(
    ) {
        binding.etTodayMessage.setText("")
        val call: Call<GetActivitiesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getActivities(
                "Bearer " + userManager.accessToken ?: "",
                "", "${selectedClassRoom?.id}", date
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

                if (response.body()?.status == true)
                    setActivitiesData(response.body()?.data)
            }

            override fun onFailure(
                call: Call<GetActivitiesResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setActivitiesData(data: ArrayList<Activities>?) {
        data?.forEachIndexed { index, activities ->
            when (activities.activityType) {

                "activity_message" -> {
                    if (!activities.activity_message.isNullOrBlank())
                        binding.etTodayMessage.setText(activities.activity_message)
                    else
                        binding.etTodayMessage.setText("")
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
                    }
                    if (activities.comment != null) {
                        binding.etThemeDesc.setText(activities.comment ?: "")
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
                    if (activities.comment != null) {
                        binding.etReadingDesc.setText(activities.comment ?: "")
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
                }
            }
        }
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@AddActivity, classRoomsList)
            binding.spClassRoom.adapter = classRoomsAdapter
        } else {
            classRoomsAdapter?.setItems(classRoomsList)
            classRoomsAdapter?.notifyDataSetChanged()
        }
        binding.spClassRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedClassRoom = classRoomsList[position]
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@AddActivity,"Click")
                if (date != "" && selectedClassRoom != null) {
                    showLoading()
                    callAPItoActivities()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
            if(selectedClassRoom?.id == -1)
                selectedClassRoom = classRoomsList.first()
            if(classroom_id != -1) {
                classRoomsList.forEachIndexed { index, item ->
                    if (item.id == classroom_id) {
                        binding.spClassRoom.setSelection(index)
                        selectedClassRoom = item
                    }
                }
                classroom_id = -1
            }
            //callAPItoActivities()
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
        }

        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun listeners() {

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnChooseThemeFile.setOnClickListener {
            chooserType = "theme"

            if (checkPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermissionDexter()
            }

//            chooser?.show()
        }

        binding.btnLiteracy.setOnClickListener {
            chooserType = "read"
//            chooser?.show()

            if (checkPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermissionDexter()
            }
        }

        binding.btnMath.setOnClickListener {
            chooserType = "math"
//            chooser?.show()

            if (checkPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermissionDexter()
            }
        }

        binding.rlSaveMessage.setOnClickListener {
            if (etTodayMessage.text.toString().isNullOrBlank()) {
                showErrorToast(this@AddActivity, "Please write something!")
                return@setOnClickListener
            }
            callAPiToUpdate(
                documentMatScience,
                binding.etScienceDesc.text.toString().trim(),
                binding.tvSubmitTodayMessage,
                binding.progressTodayMessage,
                "activity_message"
            )
        }

        binding.tvSubmitTheme.setOnClickListener {
            if (selectedClassRoom == null) {
                showErrorToast(this@AddActivity, "Please Select Classroom to submit activity!")
                return@setOnClickListener
            }
            if (documentThemeList.size > 0 && binding.etThemeDesc.text.isNotEmpty()) {
                callAPiToUpdate(
                    documentThemeList,
                    binding.etThemeDesc.text.toString().trim(),
                    binding.tvSubmitTheme,
                    binding.progressTheme,
                    "Theme"
                )
            } else if (documentThemeList.size <= 0 && binding.etThemeDesc.text.isNotEmpty()) {
                callAPiToUpdate(
                    documentThemeList,
                    binding.etThemeDesc.text.toString().trim(),
                    binding.tvSubmitTheme,
                    binding.progressTheme,
                    "Theme"
                )
            } else {
                //showErrorToast(this@AddActivity, "Please Choose at least one file for Theme!")
                showErrorToast(this@AddActivity, "Please add Additional comments for Theme!")
            }
        }

        binding.tvSubmitReading.setOnClickListener {
            if (selectedClassRoom == null) {
                showErrorToast(this@AddActivity, "Please Select Classroom to submit activity!")
                return@setOnClickListener
            }
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
                    this@AddActivity,
                    "Please Choose at least one file for Reading/Literacy!"
                )
            }
        }

        binding.tvSubmitScience.setOnClickListener {
            if (selectedClassRoom == null) {
                showErrorToast(this@AddActivity, "Please Select Classroom to submit activity!")
                return@setOnClickListener
            }
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
                    this@AddActivity,
                    "Please Choose at least one file for Math/Science!"
                )
            }
        }
    }

    private fun showImagePickerDialog() {
        val dialogBinding: DialogPickImageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_pick_image, null, false
        )
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        builder.show()

        dialogBinding.btnLeft.setOnClickListener {
            builder.dismiss()
            startGallery()
            //photoUtil.selectMultipleImageFromGallery()

        }

        dialogBinding.btnRight.setOnClickListener {
            builder.dismiss()
            startCamera()

            //startCamera()

        }


    }

    private fun startGallery() {
        val intent = Intent(this, GalleryActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        params.pickerLimit = 10
        params.toolbarColor = R.color.colorYellow
        params.actionButtonColor = R.color.red

        intent.putExtra(com.vlk.multimager.utils.Constants.KEY_PARAMS, params)
        startActivityForResult(intent, com.vlk.multimager.utils.Constants.TYPE_MULTI_PICKER)
    }

    private fun startCamera() {
        val intent = Intent(this, MultiCameraActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        params.toolbarColor = R.color.colorYellow

        params.actionButtonColor = R.color.red
        intent.putExtra(com.vlk.multimager.utils.Constants.KEY_PARAMS, params)
        startActivityForResult(intent, com.vlk.multimager.utils.Constants.TYPE_MULTI_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            com.vlk.multimager.utils.Constants.TYPE_MULTI_CAPTURE -> {

                val imagesList: ArrayList<Image> =
                    data?.getParcelableArrayListExtra(com.vlk.multimager.utils.Constants.KEY_BUNDLE_LIST)!!

                val paths = ArrayList<String>()

                imagesList.forEach {
                    Log.i("adad", "" + it.imagePath)
                    paths.add(it.imagePath)
                }

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

            com.vlk.multimager.utils.Constants.TYPE_MULTI_PICKER -> {
                val imagesList: ArrayList<Image> =
                    data?.getParcelableArrayListExtra(com.vlk.multimager.utils.Constants.KEY_BUNDLE_LIST)!!

                val paths = ArrayList<String>()

                imagesList.forEach {
                    Log.i("adad", "" + it.imagePath)
                    paths.add(it.imagePath)
                }

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
        }
    }

    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        }

        return true

    }

    private fun requestPermissionDexter() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    showImagePickerDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

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
                        this@AddActivity,
                        response.body()?.message ?: ""
                    )
                    callAPItoActivities()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@AddActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@AddActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddActivity, "Can't Connect to Server!")
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
            AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "add_or_update_child_activity")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
        api.addHeaders("Connection", "close")

        if (type == "activity_message") {
            api.addMultipartParameter("activity_message", etTodayMessage.text.toString())
        } else {
            if (docs.size > 0) {
                var files = ArrayList<File>()
                var fileParts = ArrayList<Any>()
                docs.forEachIndexed { index, s ->
                    val fileURI = Uri.parse(s)
                    if (!(fileURI.path ?: "").contains("storage/childs/activity_files")) {
                        val file = File(fileURI.path ?: "")
                        files.add(file)
                        fileParts.add(
                            MultipartBody.Part.createFormData(
                                "files[]",
                                file.name,
                                RequestBody.create("image/*".toMediaTypeOrNull(), file)
                            )
                        )
                    }
                }
                if (files.size > 0)
                    api.addMultipartFileList("files[]", files)
            }
        }
        api.addMultipartParameter("child_id", "")
        api.addMultipartParameter("classroom_id", (selectedClassRoom?.id ?: 0).toString())
        api.addMultipartParameter("activity_type", type)
        api.addMultipartParameter("activity_date", date)
        api.addMultipartParameter("comment", desc)

        api.setTag("add_or_update_child_activity")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("update_incident_report", response.toString())

                    var data =
                        gson.fromJson(response.toString(), UpdateDailyActivityResponse::class.java)

                    if (data.status == true) {
                        showSuccessToast(this@AddActivity, data.message ?: "")
                        callAPItoActivities()

                    } else {
                        showErrorToast(this@AddActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar(tv, pro)
                }

                override fun onError(error: ANError) {
//                    Log.i("jerror",error.errorBody.toString())
                    // handle error
                    hideProgressBar(tv, pro)
                    error.printStackTrace()
                    showErrorToast(this@AddActivity, error.message ?: "")
                }
            })
    }

//    private fun callAPiToUpdate1(
//        docs: ArrayList<String>,
//        desc: String,
//        tv: TextView,
//        pro: ProgressBar, type: String
//    ) {
//        showProgressBar(tv, pro)
//
//
//        val lng: RequestBody = RequestBody.create(
//            "multipart/form-data".toMediaTypeOrNull(),
//            lng,
//        )
//
//        var call: Call<UpdateProfileResponse>? = null
//
//        val documentURI = Uri.parse(documentPath)
//        val document = File(documentURI.path)
//        val documentParts = MultipartBody.Part.createFormData(
//            "custody_document[]",
//            document.name,
//            RequestBody.create("*/*".toMediaTypeOrNull(), document)
//        )
//
//
//        var api =
//            AndroidNetworking.upload(com.android.swaddle.utils.Constants.SERVER_ADDRESS_NEW + "add_or_update_child_activity")
//        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
//
//        if (docs.size > 0) {
//            var files = ArrayList<File>()
//            docs.forEachIndexed { index, s ->
//                val fileURI = Uri.parse(s)
//                if (!(fileURI.path ?: "").contains("storage/childs/activity_files")) {
//                    val file = File(fileURI.path ?: "")
//                    files.add(file)
//                }
//            }
//            if (files.size > 0)
//                api.addMultipartFileList("files[]", files)
//        }
//        api.addMultipartParameter("child_id", "")
//        api.addMultipartParameter("classroom_id", (selectedClassRoom?.id ?: 0).toString())
//        api.addMultipartParameter("activity_type", type)
//        api.addMultipartParameter("activity_date", date)
//        api.addMultipartParameter("comment", desc)
//
//        api.setTag("add_update_incident_report")
//            .setPriority(Priority.HIGH)
//            .build()
//            .setUploadProgressListener { _, _ ->
//
//            }
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//                    Log.e("update_incident_report", response.toString())
//
//                    var data = gson.fromJson(response.toString(), LoginResponse::class.java)
//
//                    if (data.status == true) {
//                        showSuccessToast(this@AddActivity, data.message ?: "")
//
//                    } else {
//                        showErrorToast(this@AddActivity, data.message ?: "")
//                    }
//                    // do anything with response
//                    hideProgressBar(tv, pro)
//                }
//
//                override fun onError(error: ANError) {
//                    // handle error
//                    hideProgressBar(tv, pro)
//                    error.printStackTrace()
//                    showErrorToast(this@AddActivity, error.message ?: "")
//                }
//            })
//    }


    override fun onITemClick(position: Int, item: CalendarModel) {
        if (item.isEnabled && !item.isSelected) {
            days.forEachIndexed { _, calendarModel ->
                calendarModel.isSelected = false
            }
            days[position].isSelected = !days[position].isSelected
            daysAdapter?.setItems(days)
            daysAdapter?.notifyDataSetChanged()
            val dayOfTheWeek = DateFormat.format("EEEE", item.date) as String // Thursday
            val day = DateFormat.format("dd", item.date) as String // 20
            val monthString = DateFormat.format("MMM", item.date) as String // Jun
            val monthNumber = DateFormat.format("MM", item.date) as String // 06
            val year = DateFormat.format("yyyy", item.date) as String // 2013

            date = "$day-$monthNumber-$year"
            Log.e("date", date)

            if (date != "" && selectedClassRoom != null) {
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