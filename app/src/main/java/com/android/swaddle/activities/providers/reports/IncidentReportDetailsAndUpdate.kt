package com.android.swaddle.activities.providers.reports

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.picker_adapters.IncidentReportImagesPickerAdapter
import com.android.swaddle.databinding.ActivityIncidentReportDetailsAndUpdateBinding
import com.android.swaddle.databinding.DialogPickImageBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.IncidentReport
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.slider.MediaSliderActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
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
import com.vlk.multimager.utils.Constants
import com.vlk.multimager.utils.Image
import com.vlk.multimager.utils.Params
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class IncidentReportDetailsAndUpdate : com.android.swaddle.baseClasses.BaseActivity() {
    private lateinit var binding: ActivityIncidentReportDetailsAndUpdateBinding

    private var dataList: ArrayList<String> = ArrayList()
    private var newList: ArrayList<String> = ArrayList()
    private var imageAdapter: IncidentReportImagesPickerAdapter? = null
    var childInfo: ChildInfo? = null
    var item: IncidentReport? = null
    var canUpdate = false
    var canUpdate2 = false
    var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportDetailsAndUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        childInfo = intent.getSerializableExtra("item") as ChildInfo
        canUpdate = intent.getBooleanExtra("canUpdate", false)

        canUpdate2 = intent.getBooleanExtra("canUpdate", true)

        selectedDate = intent.getStringExtra("selectedDate") ?: ""
        item = childInfo?.incidentReports

        try {
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

            var date = DateConverter.tempDate(selectedDate)
            calendar.time = date
            canUpdate =
                if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                    if (userManager.user?.type == "provider") {
                        item?.providerId == userManager.user?.id
                    } else {
                        item?.providerId == userManager.user?.linkedTo
                    }
                } else {
                    false
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        init()
        setData()
    }

    private fun setData() {
        binding.ivCross.setOnClickListener {
            finish()
        }

        binding.viewUpload.setOnClickListener {
            if (checkPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermission()
            }
        }

        binding.tvSignin.setOnClickListener {
            if (validation()) {
//                showSuccessToast(requireContext(), "Success")
                callAPiToUpdate()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this@IncidentReportDetailsAndUpdate,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                this@IncidentReportDetailsAndUpdate,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        } else if ((ContextCompat.checkSelfPermission(
                this@IncidentReportDetailsAndUpdate,
                Manifest.permission.CAMERA
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        }

        return true
    }

    private fun callAPiToUpdate() {
        showProgressBar()
        var api =
            AndroidNetworking.upload(com.android.swaddle.utils.Constants.SERVER_ADDRESS_NEW + "add_update_incident_report")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")

        var files = ArrayList<File>()
        if (newList.size > 0) {
            newList.forEachIndexed { _, s ->
                val fileURI = Uri.parse(s)
                val file = File(fileURI.path ?: "")
                files.add(file)
            }
            api.addMultipartFileList("files[]", files)
        }
        api.addMultipartParameter("child_id", (childInfo?.id ?: 0).toString())
            .addMultipartParameter("what_happened", binding.etDescription.text.toString().trim())
            .addMultipartParameter("teacher_comment", binding.etComments.text.toString().trim())
            .addMultipartParameter(
                "treatment_given",
                binding.etTreatmentGiven.text.toString().trim()
            )

        api.addMultipartParameter("incident_report_id", (item?.id ?: 0).toString())


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
                        showSuccessToast(this@IncidentReportDetailsAndUpdate, data.message ?: "")
                        finish()
                    } else {
                        showErrorToast(this@IncidentReportDetailsAndUpdate, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(this@IncidentReportDetailsAndUpdate, error.message ?: "")
                }
            })
    }

    private fun showProgressBar() {
        binding.tvSignin.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSignin.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun init() {
        binding.tvSignin.setOnClickListener {

        }
      /*  if (isEditable){
            binding.btnSubmit.viewGone()
            binding.viewUpload.viewGone()
            binding.ivUpload.viewGone()
            binding.tvUpload.viewGone()
        }else{
            binding.btnSubmit.viewVisible()
            binding.viewUpload.viewVisible()
            binding.ivUpload.viewVisible()
            binding.tvUpload.viewVisible()
        }*/
        if (canUpdate2) {
            Log.e("canUpdate", "$canUpdate")
            binding.btnSubmit.viewVisible()
            binding.viewUpload.viewVisible()
            binding.ivUpload.viewVisible()
            binding.tvUpload.viewVisible()
        } else if (canUpdate) {
            binding.etDescription.isEnabled = false
            binding.etComments.isEnabled = false
            binding.etTreatmentGiven.isEnabled = false
            binding.btnSubmit.viewGone()
            binding.viewUpload.viewGone()
            binding.ivUpload.viewGone()
            binding.tvUpload.viewGone()
        }
        if (item != null) {
            binding.etDescription.setText(item?.whatHappened ?: "")
            binding.etComments.setText(item?.teacherComment ?: "")
            binding.etTreatmentGiven.setText(item?.treatmentGiven ?: "")
            binding.tvCreatedDate.text = DateConverter.DateFormatFive(item!!.createdAt ?: "")
            if (item?.media != null) {
                var algs = JSONArray(item?.media ?: "")
                for (i in 0 until algs.length()) {
                    dataList.add(algs[i].toString())
                }
            }
            setReportRecyclerView()
        }
    }

    private fun setReportRecyclerView() {
        if (imageAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.recImages.layoutManager = manager
            imageAdapter =
                IncidentReportImagesPickerAdapter(
                    this@IncidentReportDetailsAndUpdate, canUpdate
                ,canUpdate2)
            binding.recImages.adapter = imageAdapter
        }
        imageAdapter?.setData(dataList)

        if (dataList.size > 0) {
            binding.tvNoMediaFound.viewGone()
        } else {
            binding.tvNoMediaFound.viewVisible()
        }
        imageAdapter?.setListener(object : IncidentReportImagesPickerAdapter.CLickListener {
            override fun onItemClick(pos: Int) {
///Show big image
                val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()

                dataList.forEachIndexed { index, model ->
                    mMediaArray.add(
                        MediaSliderActivity.MediaModel(
                            com.android.swaddle.utils.Constants.IMG_BASE_PATH + model,
                            "image",
                            com.android.swaddle.utils.Constants.IMG_BASE_PATH
                        )
                    )
                }
                loadMediaSliderView(
                    mMediaArray,
                    pos,
                    "Incident Report",
                    HIDE_DOTS = true
                )
            }

            override fun onRemoveClick(pos: Int) {
                if (dataList[pos].contains("storage/childs/incident_report")) {
                    val alert =
                        AlertView(
                            "Remove?",
                            "Are you sure you want to Remove image?",
                            AlertStyle.DIALOG
                        )
                    alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                        //=call api to remove from Classroom...
                        callAPItoImage(dataList[pos], pos)
                    })
                    alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                    })
                    alert.show(this@IncidentReportDetailsAndUpdate)
                } else {
                    dataList.removeAt(pos)
                    imageAdapter?.setData(dataList)
                }
            }
        })
    }

    private fun callAPItoImage(media: String, pos: Int) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteMedia("Bearer " + userManager.accessToken ?: "", "${item?.id}", media)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {
                    dataList.removeAt(pos)
                    imageAdapter?.setData(dataList)
                    showSuccessToast(
                        this@IncidentReportDetailsAndUpdate,
                        response.body()?.message ?: ""
                    )
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@IncidentReportDetailsAndUpdate,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@IncidentReportDetailsAndUpdate,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@IncidentReportDetailsAndUpdate, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun showImagePickerDialog() {
        val dialogBinding: DialogPickImageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this@IncidentReportDetailsAndUpdate),
            R.layout.dialog_pick_image, null, false
        )
        val builder = AlertDialog.Builder(this@IncidentReportDetailsAndUpdate)
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

    private fun requestPermission() {
        Dexter.withContext(this@IncidentReportDetailsAndUpdate)
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


    private fun startCamera() {
        val intent = Intent(this@IncidentReportDetailsAndUpdate, MultiCameraActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        params.toolbarColor = R.color.colorYellow

        params.actionButtonColor = R.color.red
        intent.putExtra(Constants.KEY_PARAMS, params)
        startActivityForResult(intent, Constants.TYPE_MULTI_CAPTURE)
    }


    private fun startGallery() {
        val intent = Intent(this@IncidentReportDetailsAndUpdate, GalleryActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        params.pickerLimit = 10
        params.toolbarColor = R.color.colorYellow
        params.actionButtonColor = R.color.red

        intent.putExtra(Constants.KEY_PARAMS, params)
        startActivityForResult(intent, Constants.TYPE_MULTI_PICKER)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            Constants.TYPE_MULTI_CAPTURE -> {

                val imagesList: ArrayList<Image> =
                    data?.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST)!!
                imagesList.forEach {
                    Log.i("adad", "" + it.imagePath)
                    dataList.add(it.imagePath)
                    newList.add(it.imagePath)


                }

                imageAdapter?.setData(dataList)

                if (imagesList.size > 0) {
                    hideUpload()
                }

            }

            Constants.TYPE_MULTI_PICKER -> {
                val imagesList: ArrayList<Image> =
                    data?.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST)!!

                imagesList.forEach {
                    Log.i("adad", "" + it.imagePath)
                    dataList.add(it.imagePath)
                    newList.add(it.imagePath)

                }

                imageAdapter?.setData(dataList)
                if (imagesList.size > 0) {
                    hideUpload()
                }
            }
        }
    }

    private fun showUpload() {
//        binding.viewUpload.viewVisible()
//        binding.tvUpload.viewVisible()
//        binding.ivUpload.viewVisible()
//        binding.recImages.viewGone()
    }

    private fun hideUpload() {
//        binding.viewUpload.viewInVisible()
//        binding.ivUpload.viewGone()
//        binding.tvUpload.viewGone()
//        binding.recImages.viewVisible()

    }


    private fun validation(): Boolean {
        val desc = binding.etDescription.text.toString()
        val comments = binding.etComments.text.toString()
        if (dataList.isEmpty()) {
            showErrorToast(this@IncidentReportDetailsAndUpdate, "Please attach at least one image")
            return false
        }
        if (desc.isEmpty()) {
            showErrorToast(this@IncidentReportDetailsAndUpdate, "Description field cannot be empty")
            return false
        }
        return true
    }
}