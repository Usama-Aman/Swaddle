package com.android.swaddle.fragments.bottomSheetFragment


import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.swaddle.R
import com.android.swaddle.adapters.picker_adapters.ImagePickerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.BottomSheetAddIncidentReportBinding
import com.android.swaddle.databinding.DialogPickImageBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.utils.DateConverter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vlk.multimager.activities.GalleryActivity
import com.vlk.multimager.activities.MultiCameraActivity
import com.vlk.multimager.utils.Constants
import com.vlk.multimager.utils.Image
import com.vlk.multimager.utils.Params
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList


open class AddIncidentReportBottomSheet(
    var baseActivity: BaseActivity,
    var selectedChild: ChildInfo,
    var selectedClassRoom: ClassroomDetails,
    var dateTosave: String,
    var isUpdating: Boolean = false
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAddIncidentReportBinding
    private lateinit var clickListener: ClickListener
    private var dataList: ArrayList<String> = ArrayList()
    private var imageAdapter: ImagePickerAdapter? = null
    val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetAddIncidentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImagesAdapter()
        init()
        listener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {

        binding.tvChildAge.text = DateConverter.DateFormatFour(selectedChild?.dob ?: "")

        Glide.with(baseActivity)
            .load(com.android.swaddle.utils.Constants.IMG_BASE_PATH + selectedChild.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivChild)

        binding.tvName.text = selectedChild.firstName + " " + selectedChild.lastName

        binding.tvUploadedDate.text = DateConverter.currentDateThree

    }

    private fun setImagesAdapter() {
        imageAdapter = ImagePickerAdapter(requireContext())
        binding.recImages.adapter = imageAdapter
        imageAdapter?.setListener(object : ImagePickerAdapter.CLickListener {
            override fun onItemClick(pos: Int) {

            }

            override fun onRemoveClick(pos: Int) {
                dataList.removeAt(pos)
                imageAdapter?.notifyItemRemoved(pos)

                if (dataList.isEmpty()) {
                    showUpload()
                }
            }
        })
    }

    private fun listener() {
        binding.ivCross.setOnClickListener {
            clickListener.onCanceled(this)
            dismiss()
        }

        binding.viewUpload.setOnClickListener {
            if (checkPermissions()) {
                showImagePickerDialog()
            } else {
                requestPermission()
            }
        }


        binding.btnSubmit.setOnClickListener {
            if (validation()) {
//                showSuccessToast(requireContext(), "Success")
                callAPiToUpdate()
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
    }

    fun setListener(listener: ClickListener) {
        clickListener = listener
    }


    private fun showImagePickerDialog() {
        val dialogBinding: DialogPickImageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_pick_image, null, false
        )
        val builder = AlertDialog.Builder(context)
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


    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        } else if ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            return false
        }

        return true

    }


    private fun requestPermission() {
        Dexter.withContext(requireContext())
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
        val intent = Intent(context, MultiCameraActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        params.toolbarColor = R.color.colorYellow

        params.actionButtonColor = R.color.red
        intent.putExtra(Constants.KEY_PARAMS, params)
        startActivityForResult(intent, Constants.TYPE_MULTI_CAPTURE)
    }


    private fun startGallery() {
        val intent = Intent(context, GalleryActivity::class.java)
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
        val treatment = binding.etTreatment.text.toString()

        if (dataList.isEmpty()) {
            showErrorToast(requireContext(), "Please attach at least one image")
            return false
        }
       /* if (comments.isEmpty()) {
            showErrorToast(requireContext(), "Comments field cannot be empty")
            return false
        }*/
        if (desc.isEmpty()) {
            showErrorToast(requireContext(), "Description field cannot be empty")
            return false
        }
        if (treatment.isEmpty()) {
            showErrorToast(requireContext(), "Treatment field cannot be empty")
            return false
        }
        return true
    }

    private fun callAPiToUpdate() {
        showProgressBar()
        var api =
            AndroidNetworking.upload(com.android.swaddle.utils.Constants.SERVER_ADDRESS_NEW + "add_update_incident_report")
        api.addHeaders("Authorization", "Bearer " + baseActivity.userManager.accessToken ?: "")

        var files = ArrayList<File>()
        if (dataList.size > 0) {
            dataList.forEachIndexed { index, s ->
                val fileURI = Uri.parse(s)
                val file = File(fileURI.path ?: "")
                files.add(file)
            }
            api.addMultipartFileList("files[]", files)
        }
        api.addMultipartParameter("child_id", (selectedChild.id ?: 0).toString())
            .addMultipartParameter("classroom_id", (selectedClassRoom.id ?: 0).toString())
            .addMultipartParameter("what_happened", binding.etDescription.text.toString().trim())
            .addMultipartParameter("treatment_given", binding.etTreatment.text.toString().trim())
            .addMultipartParameter("teacher_comment", binding.etComments.text.toString().trim())
            .addMultipartParameter("created_at",dateTosave)
        if (isUpdating) {
            api.addMultipartParameter("incident_report_id", "")
        }

        api.setTag("add_update_incident_report")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("add_incident_report", response.toString())

                    var data = gson.fromJson(response.toString(), LoginResponse::class.java)

                    if (data.status == true) {
                        showSuccessToast(baseActivity, data.message ?: "")
                        clickListener.onSuccessfullySubmitted(this@AddIncidentReportBottomSheet)
                    } else {
                        showErrorToast(baseActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(baseActivity, error.message ?: "")
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

}

interface ClickListener {
    fun onCanceled(dialog: BottomSheetDialogFragment)
    fun onSuccessfullySubmitted(dialog: BottomSheetDialogFragment)
}