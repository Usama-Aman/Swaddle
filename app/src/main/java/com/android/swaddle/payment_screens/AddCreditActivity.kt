package com.android.swaddle.payment_screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.android.swaddle.R
import com.android.swaddle.adapters.payment_adapters.FilesAdapter
import com.android.swaddle.adapters.spinneradapter.*
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddCreditBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.maskeditor.getFileName
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.codekidlabs.storagechooser.StorageChooser
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AddCreditActivity : BaseActivity() {
    private val SAVE_REQUEST_CODE: Int = 1212
    private lateinit var binding: ActivityAddCreditBinding
    private var documentPath: ArrayList<String> = ArrayList()
    private var adapter: FilesAdapter? = null

    var childesList = java.util.ArrayList<ChildInfo>()
    var classRoomsList = java.util.ArrayList<ClassroomDetails>()
    var billTypeList = java.util.ArrayList<BillTypeList>()
    var selectIntervalList = java.util.ArrayList<SelectIntervalList>()

    var selectedChild: ChildInfo? = null

    var selectedClassRoom: ClassroomDetails? = null
    var selectedBillType: BillTypeList? = null
    var selectedBillTyp = ""
    var selectedInterval: String = ""
    private var childesAdapter: SelectChildSpinnerAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var billTypeAdapter: BillTypeSpinnerAdapter? = null
    private var selectIntervalAdapter: SelectIntervalSpinnerAdapter? = null

    private var builder = StorageChooser.Builder()
    private var chooser: StorageChooser? = null

    var formats = ArrayList<String>()
    var selectedDate = ""
    var startDate = ""
    var endingDate = ""
    private var picker: DatePickerDialog? = null
    private var mCalendar: Calendar = Calendar.getInstance()
    private var mCalendar2: Calendar = Calendar.getInstance()

    var invoiceDetails: ChildLatestInvoice? = null
    var invoiceID: Int? = null
    var update = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCreditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        invoiceDetails = intent.getSerializableExtra("item") as ChildLatestInvoice?
        update = intent.getBooleanExtra("update", false)
        if (invoiceDetails != null) {
            updateObject()
        }

        initVar()
        callAPItoGetClassRooms()
        setFileRecView()
        listener()
    }


    private fun updateObject() {
        invoiceID = invoiceDetails?.id
        binding.tvAddCredit.text = "Update Bill"
        binding.etDueDate.text = DateConverter.DueDateTimeStamp(invoiceDetails?.dueDate ?: "")
        binding.etDesc.setText(invoiceDetails?.description ?: "")
        binding.etAmount.setText(invoiceDetails?.amount.toString())
        var x = (invoiceDetails?.dueDate ?: "").split("-")
        selectedDate = "${x.last()}-${x[1]}-${x.first()}"
        if (invoiceDetails?.isRecursive == 1) {
            var y = (invoiceDetails?.startDate ?: "").split("-")
            var z = (invoiceDetails?.endDate ?: "").split("-")
            startDate = "${y.last()}-${y[1]}-${y.first()}"
            endingDate = "${z.last()}-${z[1]}-${z.first()}"
        }
        Log.e("dueDate", selectedDate)
        Log.e("startDate", startDate)
        Log.e("endingDate", endingDate)
    }

    private fun callAPItoGetClassRooms(
    ) {
        val call: Call<ClassRoomsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getClassRooms("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<ClassRoomsResponse> {
            override fun onResponse(
                call: Call<ClassRoomsResponse>,
                response: Response<ClassRoomsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                classRoomsList = response.body()?.data ?: java.util.ArrayList()
                setClassSpinner()
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddCreditActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@AddCreditActivity, classRoomsList)
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
                callAPItoGetClassRoomChildesChildes(classRoomsList[position].id)
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
            classRoomsList.forEachIndexed{ index, item ->
                if(item.id == selectedClassRoom?.id){
                    binding.spClassRoom.setSelection(index)
                }
            }
            if(selectedClassRoom?.id == -1)
                selectedClassRoom = classRoomsList.first()
            //callAPItoGetClassRoomChildesChildes(classRoomsList.first().id)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
        }
    }


    private fun callAPItoGetClassRoomChildesChildes(id: Int?) {
        showLoading()
        val call: Call<ChildesResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildesListOfRoom(
                "Bearer " + userManager.accessToken ?: "",
                (id ?: 0).toString(),
                "0"
            )
        call.enqueue(object : Callback<ChildesResponse> {
            override fun onResponse(
                call: Call<ChildesResponse>,
                response: Response<ChildesResponse>
            ) {
                hideLoading()
                childesList = java.util.ArrayList()
                childesList = response.body()?.data ?: java.util.ArrayList()
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@AddCreditActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGenerateInvoice() {

        val call: Call<ResponseBody> = RetrofitClass.getInstance().webRequestsInstance
            .generateInvoice(
                "Bearer " + userManager.accessToken ?: "",
                (selectedChild?.id ?: 0).toString(),
                (binding.etDesc.text.toString().trim()),
                (binding.etAmount.text.toString().trim()),
                selectedDate,
                "credit"
                /* if (selectedBillTyp == "Recurring") {
                     "bill&recursive"
                 } else {
                     "bill"
                 }*/,
                startDate,
                endingDate,
                selectedInterval
            )
        call.enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                hideProgressBar()
                if (response.isSuccessful) {
                    val data = JSONObject(response.body()?.string())
                    if (data.getBoolean("status")) {
                        showSuccessToast(this@AddCreditActivity, data.getString("message") ?: "")
                        finish()
                    } else {
                        showErrorToast(this@AddCreditActivity, data.getString("message") ?: "")
                    }
                } else {
                    val data = JSONObject(response.errorBody()?.string())
                    showErrorToast(this@AddCreditActivity, data.getString("message") ?: "")
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                showErrorToast(this@AddCreditActivity, "Can't Connect to Server!")
                hideProgressBar()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@AddCreditActivity, childesList)
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
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            childesList.forEachIndexed{ index, item ->
                if(item.id == selectedChild?.id){
                    binding.spChild.setSelection(index)
                }
            }
            if(selectedChild?.id == -1)
                selectedChild = childesList.first()
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()

        }
    }

    private fun setFileRecView() {
        if (adapter == null) {
            adapter = FilesAdapter(this@AddCreditActivity, documentPath)
            binding.recFiles.adapter = adapter
            adapter?.setListener(object : FilesAdapter.CLickListener {
                override fun onItemClick(pos: Int) {
//                documentPath.removeAt(pos)
//                adapter?.notifyItemRemoved(pos)
                }

                override fun onRemoveClick(pos: Int) {
                    documentPath.removeAt(pos)
                    adapter?.notifyItemRemoved(pos)
                }

                override fun onDownloadClick(position: Int) {

                }
            })
        } else {
            adapter?.setData(documentPath)
        }
    }

    private fun listener() {


        binding.tvChooseFiles.setOnClickListener {

            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                        chooser?.show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) { /* ... */
                        token?.continuePermissionRequest()
                    }
                }).check()
        }


        binding.etDueDate.setOnClickListener {
            datePickerDialog("a")
        }


        binding.tvAddCredit.setOnClickListener {
            if (validation()) {
                showProgressBar()
                if (update) {
                    callAPItoUpdateInvoice()
                } else {
                    callAPItoGenerateInvoice()
                }
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun showProgressBar() {
        binding.tvAddCredit.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvAddCredit.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun chooseFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, SAVE_REQUEST_CODE)
    }

    private fun initVar() {

        selectedChild = intent.getSerializableExtra("selectedChild") as ChildInfo?
        selectedClassRoom = intent.getSerializableExtra("selectedClass") as ClassroomDetails?
        billTypeList.add(BillTypeList("Bill"))
        billTypeList.add(BillTypeList("Recurring"))



        selectIntervalList.add(SelectIntervalList("Daily"))
        selectIntervalList.add(SelectIntervalList("Weekly"))
        selectIntervalList.add(SelectIntervalList("Monthly"))
        selectIntervalList.add(SelectIntervalList("Yearly"))

        formats.add("txt")
        formats.add("jpg")
        formats.add("jpeg")
        formats.add("xlx")
        formats.add("xlxs")
        formats.add("doc")
        formats.add("docx")
        formats.add("png")

        builder.customFilter(formats);

        builder.withActivity(this@AddCreditActivity)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .customFilter(formats)
            .build()

        chooser = builder.build()
        chooser?.setOnMultipleSelectListener { paths ->
            Log.e("SELECTED_PATH", paths.size.toString())
            documentPath = ArrayList()
            documentPath.addAll(paths)
            adapter?.setData(documentPath)
        }
        chooser?.setOnSelectListener { path ->
            Log.e("SELECTED_PATH", path)
            documentPath = ArrayList()
            documentPath.add(path)
            adapter?.setData(documentPath)
        }
    }

    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    private fun requestPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    chooseFiles()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SAVE_REQUEST_CODE) {
            if (resultCode == RESULT_OK
                && null != data
            ) {
                val uri = data.data
//                uriList.add(uri!!)
//
//                adapter?.setData(uriList)

                Log.i("PDF_FILE", "${uri.toString()}")
                Log.i("PDF_FILE", "${getFileName(uri!!, this)}")
            }
        }
    }

    private fun datePickerDialog(type: String) {
        val day: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val year: Int = mCalendar.get(Calendar.YEAR)
        picker = DatePickerDialog(
            this, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                if (type == "b") {
                    /*   binding.etStartDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)*/
                    startDate = DateConverter.formatePickerDate2(mCalendar.time)
                } else if (type == "a") {
                    binding.etDueDate.text = DateConverter.formatePickerDateNew2(mCalendar.time)
                    selectedDate = DateConverter.formatePickerDate2(mCalendar.time)
                }

                Log.i("dueDate", selectedDate)

                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }, year, month, day
        )
        picker?.datePicker?.minDate = mCalendar.timeInMillis
        picker?.show()
    }

    private fun datePickerDialog2() {
        val day: Int = mCalendar2.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar2.get(Calendar.MONTH)
        val year: Int = mCalendar2.get(Calendar.YEAR)
        picker = DatePickerDialog(
            this@AddCreditActivity, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->

                mCalendar2.set(Calendar.YEAR, year)
                mCalendar2.set(Calendar.MONTH, monthOfYear)
                mCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                /*binding.etEndDate.text =
                    DateConverter.formatePickerDate(mCalendar2.time)*/
                endingDate = DateConverter.formatePickerDate2(mCalendar.time)


                /*dateTo = DateConverter.formatePickerDateNEw(mCalendar2.time)*/

                /*  callAPItoGetInvoicesList(selectedChild?.id ?: 0)*/


            }, year, month, day
        )

        picker?.datePicker?.minDate = mCalendar.timeInMillis
        picker?.show()
    }


    private fun callAPiToUpdate() {
        showProgressBar()

        var api =
            AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "create_invoice")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")

        var files = ArrayList<File>()
        if (documentPath.size > 0) {
            documentPath.forEachIndexed { index, s ->
                val fileURI = Uri.parse(s)
                val file = File(fileURI.path ?: "")
                files.add(file)
            }
            api.addMultipartFileList("files[]", files)
        }

        if (invoiceID != null) {
            api.addMultipartParameter("invoice_id", invoiceID.toString())
        }

        api.addMultipartParameter("child_id", (selectedChild?.id ?: 0).toString())
            .addMultipartParameter("description", (binding.etDesc.text.toString().trim()))
            .addMultipartParameter("amount", (binding.etAmount.text.toString().trim()))
            .addMultipartParameter("due_date", selectedDate)

        api.setTag("create_invoice")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("create_invoice", response.toString())

                    var data = gson.fromJson(response.toString(), LoginResponse::class.java)

                    if (data.status == true) {
                        showSuccessToast(this@AddCreditActivity, data.message ?: "")
                        finish()
                    } else {
                        showErrorToast(this@AddCreditActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(this@AddCreditActivity, error.message ?: "")
                }
            })
    }


    private fun callAPItoUpdateInvoice() {

        val call: Call<ResponseBody> = RetrofitClass.getInstance().webRequestsInstance
            .updateInvoice(
                "Bearer " + userManager.accessToken ?: "",
                (invoiceDetails?.id ?: 0).toString(),
                (selectedChild?.id ?: 0).toString(),
                (binding.etDesc.text.toString().trim()),
                (binding.etAmount.text.toString().trim()),
                selectedDate,
                if (selectedBillTyp == "Recurring") {
                    "bill&recursive"
                } else {
                    "bill"
                },
                startDate,
                endingDate,
                selectedInterval
            )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                hideProgressBar()
                if (response.isSuccessful) {
                    val data = JSONObject(response.body()?.string())
                    if (data.getBoolean("status")) {
                        showSuccessToast(this@AddCreditActivity, data.getString("message") ?: "")
                        finish()
                    } else {
                        showErrorToast(this@AddCreditActivity, data.getString("message") ?: "")
                    }
                } else {
                    val data = JSONObject(response.errorBody()?.string())
                    showErrorToast(this@AddCreditActivity, data.getString("message") ?: "")
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                showErrorToast(this@AddCreditActivity, "Can't Connect to Server!")
                hideProgressBar()
                t.printStackTrace()
            }
        })
    }

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@AddCreditActivity, "No internet connection!")
            return false
        }
        if (selectedClassRoom == null) {
            showErrorToast(this, "Please select Classroom")
            return false
        } else if (selectedChild == null) {
            showErrorToast(this, "Please select child")
            return false
        } else if (selectedDate.isEmpty()) {
            showErrorToast(this, "Please select due date")
            return false

        } else if (binding.etAmount.text.toString().trim().isEmpty()) {
            showErrorToast(this, "Please enter amount")
            return false
        } else if (binding.etAmount.text.toString().toFloat() < 0.0) {
            showErrorToast(this, "Please enter valid amount")
            return false
        } else if (binding.etDesc.text.toString().trim().isEmpty()) {
            showErrorToast(this, "Please enter description")
            return false
        } else
            return true
    }
}