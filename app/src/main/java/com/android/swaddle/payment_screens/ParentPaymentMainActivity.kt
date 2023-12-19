package com.android.swaddle.payment_screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.activities.common.TransactionHistoryActivity
import com.android.swaddle.adapters.payment_adapters.PaymentDetailAdapterNew
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.bottomsheets.AddBillOrCreditBottomSheet
import com.android.swaddle.databinding.ActivityPaymentMainBinding
import com.android.swaddle.fragments.bottomSheetFragment.PayoutAccountPaymentBottomSheet
import com.android.swaddle.fragments.payment_fragment.PayBillPopup
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ParentPaymentMainActivity : BaseActivity(),
    PaymentDetailAdapterNew.ClickListener {

    private lateinit var binding: ActivityPaymentMainBinding
    private var paymentDetailAdapterNew: PaymentDetailAdapterNew? = null

    var paymentsList = ArrayList<ChildInfo>()

    private var mCalendar: Calendar = Calendar.getInstance()
    private var mCalendar2: Calendar = Calendar.getInstance()
    private var picker: DatePickerDialog? = null

    var childesList = ArrayList<ChildInfo>()

    var classRoomsList = ArrayList<ClassroomDetails>()

    var selectedChild: ChildInfo? = null

    var selectedClassRoom: ClassroomDetails? = null
    private var childesAdapter: SelectChildSpinnerAdapter? = null
    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var duration: String = "week"
    var dateFrom = ""
    var dateTo = ""

    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1
    var downloadedPosition = 0
    var downloadPath = ""
    var selectedPos = 0
    var classroom_id: Int = -1
    var child_id: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedPos = intent.getIntExtra("position", 0)

        //Setting start and end dates of the week
        mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        binding.tvDate1.text = DateConverter.formatePickerDateNew2(mCalendar.time)

        mCalendar2.set(Calendar.DAY_OF_WEEK, 7)
        mCalendar2.add(Calendar.DATE, 1)
        binding.tvDate2.text = DateConverter.formatePickerDateNew2(mCalendar2.time)

        getIntentData()
        listener()

        if (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") {
            callAPItoGetChildes()
            binding.cvSpinnerClass.viewGone()
            binding.tvClass.viewGone()
            binding.ivAdd.viewVisible()
        } else {
            binding.ivAdd.viewVisible()
            binding.cvSpinnerClass.viewVisible()
            binding.tvClass.viewVisible()
            callAPItoGetClassRooms()
        }
    }
    private fun getIntentData(){
        classroom_id = intent.getIntExtra("classroom_id",-1)
        child_id = intent.getIntExtra("child_id",-1)
    }

    private fun callAPItoGetLatestInvoicesList(childId: Int) {
        var path = Constants.SERVER_ADDRESS_NEW + "get_invoices"
        Log.e("path", path)
        var api =
            AndroidNetworking.get(path)
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
        if (selectedChild != null) {
            api.addQueryParameter("child_id", (selectedChild?.id ?: 0).toString())
            if (userManager.user?.type != "parent" && userManager.user?.type != "authorized_adult") {
                api.addQueryParameter("classroom_id", (selectedClassRoom?.id ?: 0).toString())
            }
        }

        api.addQueryParameter("date_from", dateFrom)
            .addQueryParameter("date_to", dateTo)
            .addQueryParameter("time_period", duration)

        api.setTag("get_invoices")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("get_invoices", response.toString())

                    var data = gson.fromJson(response.toString(), GetInvoicesResponse::class.java)

                    if (data.status == true) {
//                        showSuccessToast(this@ParentPaymentMainActivity, data.message ?: "")
                        paymentsList = ArrayList()
                        paymentsList = data?.data?.childesData ?: ArrayList()
                        setPaymentRec()
                        hideLoading()

                    } else {
//                        showErrorToast(this@ParentPaymentMainActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideLoading()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideLoading()
                    Log.e("error", error.errorBody.first().toString())
                    error.printStackTrace()
//                    showErrorToast(this@ParentPaymentMainActivity, error.message ?: "")
                }
            })

//
//        val call: Call<GetInvoicesResponse> = RetrofitClass.getInstance().webRequestsInstance
//            .getLatestInvoices(
//                "Bearer " + userManager.accessToken ?: "",
//                if (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") {
//                    childId.toString()
//                } else {
//                    ""
//                },
//                if (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") {
//                    ""
//                } else {
//                    "" //"${selectedClassRoom?.id ?: 0}"
//                },
//                dateFrom,
//                dateTo
//            )
//        call.enqueue(object : Callback<GetInvoicesResponse> {
//            override fun onResponse(
//                call: Call<GetInvoicesResponse>,
//                response: Response<GetInvoicesResponse>
//            ) {
//                paymentsList.clear()
//                if (response.body()?.status == true) {
//                    paymentsList = response.body()?.data?.childesData ?: ArrayList()
//                    setPaymentRec()
//                    hideLoading()
//                }
//            }
//
//            override fun onFailure(
//                call: Call<GetInvoicesResponse>,
//                t: Throwable
//            ) {
//                showErrorToast(this@ParentPaymentMainActivity, "Can't Connect to Server!")
//                hideLoading()
//                t.printStackTrace()
//            }
//        })
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
                showErrorToast(this@ParentPaymentMainActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setClassSpinner() {
        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@ParentPaymentMainActivity, classRoomsList)
            binding.spClassRoom.adapter = classRoomsAdapter
            binding.viewRooms.viewGone()
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
                //   showSuccessToast(this@ParentPaymentMainActivity,"Click")
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
            //callAPItoGetClassRoomChildesChildes(classRoomsList.first().id)
        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
        }

        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
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
                showSuccessToast(this@ParentPaymentMainActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
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
                childesList = ArrayList()
                childesList = response.body()?.data ?: ArrayList()
                if (selectedClassRoom != null) {
                    setChildSpinner()
                }
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@ParentPaymentMainActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@ParentPaymentMainActivity, childesList)
            binding.spChild.adapter = childesAdapter
            binding.viewChildes.viewGone()
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
                paymentsList.clear()
                callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            binding.viewChildes.viewGone()
            if(selectedChild?.id == -1)
                selectedChild = childesList.first()
            if(child_id != -1) {
                childesList.forEachIndexed { index, item ->
                    if (item.id == child_id) {
                        binding.spChild.setSelection(index)
                        selectedChild = item
                    }
                }
                child_id = -1
            }

            //callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
        } else {
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            binding.viewChildes.viewVisible()

        }
    }

    private fun setPaymentRec() {
        if (paymentDetailAdapterNew == null) {
            paymentDetailAdapterNew =
                PaymentDetailAdapterNew(
                    this@ParentPaymentMainActivity,
                    this@ParentPaymentMainActivity,
                    paymentsList
                )
            binding.recInProgress.adapter = paymentDetailAdapterNew
        } else {
            paymentDetailAdapterNew?.setItems(paymentsList)
        }
    }

    private fun listener() {
        binding.viewRooms.setOnClickListener {
        }
        binding.viewChildes.setOnClickListener {
            setChildSpinner()
        }
        binding.lnrDateStart.setOnClickListener {
            datePickerDialog()
        }
        binding.lnrDateEnd.setOnClickListener {
            datePickerDialog2()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvWeek.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()

            mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            dateFrom = ""

            mCalendar2.set(Calendar.DAY_OF_WEEK, 7)
            mCalendar2.add(Calendar.DATE, 1)

            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            dateTo = ""

            changeColor(binding.tvWeek)
            duration = "week"
            showLoading()
            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)

        }

        binding.tvMonth.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()
            mCalendar.set(Calendar.DAY_OF_MONTH, 1)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            dateFrom = ""

            mCalendar2.add(Calendar.MONTH, 1)
            mCalendar2.set(Calendar.DAY_OF_MONTH, 1)
            mCalendar2.add(Calendar.DATE, -1)
            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            dateTo = ""

            changeColor(binding.tvMonth)
            duration = "month"
            showLoading()
            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
        }

        binding.tvYear.setOnClickListener {
            mCalendar = Calendar.getInstance()
            mCalendar2 = Calendar.getInstance()

            mCalendar.set(Calendar.MONTH, Calendar.JANUARY)
            mCalendar.set(Calendar.DATE, 1)
            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
            dateFrom = ""

            mCalendar2.set(Calendar.MONTH, Calendar.DECEMBER)
            mCalendar2.set(Calendar.DAY_OF_MONTH, 31)
            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
            dateTo = ""

            changeColor(binding.tvYear)
            duration = "year"
            showLoading()
            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
        }


//        binding.tvMonth.setOnClickListener {
//            mCalendar = Calendar.getInstance()
//            mCalendar2 = Calendar.getInstance()
//            mCalendar.set(Calendar.DAY_OF_MONTH, 1)
//            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
//            dateFrom = ""
//            mCalendar2.add(Calendar.MONTH, 1)
//            mCalendar2.set(Calendar.DAY_OF_MONTH, 1)
//            mCalendar2.add(Calendar.DATE, -1)
//            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
//            dateTo = ""
//
//            dateFrom = DateConverter.formatePickerDateNEw(mCalendar.time)
//            dateTo = DateConverter.formatePickerDateNEw(mCalendar2.time)
//
//
//            changeColor(binding.tvMonth)
////            duration = "month"
//            duration = ""
//
//            showLoading()
//            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
//        }
//
//        binding.tvYear.setOnClickListener {
//            mCalendar = Calendar.getInstance()
//            mCalendar2 = Calendar.getInstance()
//            mCalendar.add(Calendar.YEAR, 0)
//            //mCalendar2.set(Calendar.DAY_OF_YEAR, 1)
//            mCalendar.set(Calendar.MONTH, Calendar.JANUARY)
//            mCalendar.add(Calendar.DATE, 0)
//            binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
//            dateFrom = ""
//
//            mCalendar2.add(Calendar.YEAR, 0)
//            mCalendar2.set(Calendar.MONTH, Calendar.DECEMBER)
//            //mCalendar2.set(Calendar.DAY_OF_YEAR, 1)
//            mCalendar2.add(Calendar.DATE, 30)
//
//            binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
//            dateTo = ""
//
//            dateFrom = DateConverter.formatePickerDateNEw(mCalendar.time)
//            dateTo = DateConverter.formatePickerDateNEw(mCalendar2.time)
//
//
//            changeColor(binding.tvYear)
////            duration = "year"
//            duration = ""
//            showLoading()
//            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
//        }

        binding.ivAdd.setOnClickListener {
            if (userManager.user?.type == "parent" || userManager.user?.type == "authorized_adult") {
                var sheet = PayoutAccountPaymentBottomSheet()
                sheet.show(supportFragmentManager, "PayoutAccountPaymentBottomSheet")
                sheet.setListener(object : PayoutAccountPaymentBottomSheet.ClickListener {
                    override fun onCrossClicked(dialog: BottomSheetDialogFragment) {
                        dialog.dismiss()
                    }

                    override fun onNextClicked(dialog: BottomSheetDialogFragment) {
                        dialog.dismiss()
                        startActivity<BrowserActivity>()
                    }
                })
            } else {
                var sheet = AddBillOrCreditBottomSheet()
                sheet.show(supportFragmentManager, "AddBillOrCreditBottomSheet")
                sheet.setListener(object : AddBillOrCreditBottomSheet.ClickListener {
                    override fun onAddBillClicked(dialog: BottomSheetDialogFragment) {
                        val intent =
                            Intent(this@ParentPaymentMainActivity, AddBillActivity::class.java)
                        intent.putExtra("selectedChild",selectedChild)
                        intent.putExtra("selectedClass",selectedClassRoom)
                        startActivity(intent)
                        dialog.dismiss()
                    }

                    override fun onAddCreditClicked(dialog: BottomSheetDialogFragment) {
                        val intent =
                            Intent(this@ParentPaymentMainActivity, AddCreditActivity::class.java)
                        intent.putExtra("selectedChild",selectedChild)
                        intent.putExtra("selectedClass",selectedClassRoom)
                        startActivity(intent)
                        dialog.dismiss()
                    }

                    override fun onCancelClicked(dialog: BottomSheetDialogFragment) {
                        dialog.dismiss()
                    }

                })

            }
//
//            if (userManager.user?.isBankAccountVerified != 1) {
//                var sheet = PayoutAccountPaymentBottomSheet()
//                sheet.show(supportFragmentManager, "PayoutAccountPaymentBottomSheet")
//                sheet.setListener(object : PayoutAccountPaymentBottomSheet.ClickListener {
//                    override fun onCrossClicked(dialog: BottomSheetDialogFragment) {
//                        dialog.dismiss()
//                    }
//
//                    override fun onNextClicked(dialog: BottomSheetDialogFragment) {
//                        dialog.dismiss()
//                        startActivity<BrowserActivity>()
//                    }
//                })
//            } else {
//                val intent = Intent(this, AddBillActivity::class.java)
//                startActivity(intent)
//            }
        }
    }

    private fun resetColor() {
        binding.tvWeek.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))
        binding.tvMonth.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))
        binding.tvYear.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))
    }

    private fun changeColor(tv: TextView) {
        binding.tvWeek.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))
        binding.tvMonth.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))
        binding.tvYear.setTextColor(ContextCompat.getColor(this, R.color.colorGhost))

        tv.setTextColor(ContextCompat.getColor(this, R.color.colroBlue))
    }

    private fun initVar() {

    }

    override fun onResume() {
        super.onResume()
        if (selectedChild != null)
            callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
    }

    private fun datePickerDialog() {
        val day: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val year: Int = mCalendar.get(Calendar.YEAR)
        picker = DatePickerDialog(
            this, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvDate1.text = DateConverter.formatePickerDateMonthDayYear(mCalendar.time)
                dateFrom = DateConverter.formatePickerDateNEw(mCalendar.time)
                resetColor()
                duration = ""
                /*if (type == "a") {
                    binding.tvDate1.text = DateConverter.formatePickerDate(mCalendar.time)
                    dateFrom = DateConverter.formatePickerDateNEw(mCalendar.time)
                } else {
                    binding.tvDate2.text = DateConverter.formatePickerDate(mCalendar.time)
                    dateTo = DateConverter.formatePickerDateNEw(mCalendar.time)
                }*/

                callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }, year, month, day
        )
        picker?.datePicker?.maxDate = mCalendar2.timeInMillis
        picker?.show()
    }

    private fun datePickerDialog2() {
        val day: Int = mCalendar2.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar2.get(Calendar.MONTH)
        val year: Int = mCalendar2.get(Calendar.YEAR)
        picker = DatePickerDialog(
            this@ParentPaymentMainActivity, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->

                mCalendar2.set(Calendar.YEAR, year)
                mCalendar2.set(Calendar.MONTH, monthOfYear)
                mCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvDate2.text = DateConverter.formatePickerDateMonthDayYear(mCalendar2.time)
                dateTo = DateConverter.formatePickerDateNEw(mCalendar2.time)
                resetColor()
                duration = ""

                callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
            }, year, month, day
        )

        picker?.datePicker?.minDate = mCalendar.timeInMillis
        picker?.show()
    }

    private fun permissionAccess(position: Int, path: String) {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1)
            requestPermission(p1)
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2)
            requestPermission(p2)
        } else {
            Log.e(
                "filePath", Constants.IMG_BASE_PATH + path
            )
            val fileName = (path).split("/").last()

            val request = DownloadManager.Request(
                Uri.parse(Constants.IMG_BASE_PATH + path)
            )
            request.setTitle("Swaddle-$fileName").setDescription("Downloading is in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/.Swaddle/$fileName"
                )
                .setMimeType("*/.pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this@ParentPaymentMainActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@ParentPaymentMainActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ParentPaymentMainActivity,
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
                    permissionAccess(downloadedPosition, downloadPath)
                } else {
                    Toast.makeText(
                        this@ParentPaymentMainActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onItemClick(position: Int) {
        if (selectedChild == null) {
            showErrorToast(
                this@ParentPaymentMainActivity,
                "Please select child!"
            )
        } else {
            val intent =
                Intent(this@ParentPaymentMainActivity, TransactionHistoryActivity::class.java)
            intent.putExtra("selectedChild", selectedChild)
            intent.putExtra("selectedClassRoom", selectedClassRoom)
            intent.putExtra("timePeriod", duration)
//            intent.putExtra("fromDate", dateFrom)
//            intent.putExtra("toDate", dateTo)
            startActivity(intent)
        }

    }

    override fun onCardClick(position: Int) {
        var dialog = PayBillPopup.getInstance(paymentsList[position])
        dialog.baseActivity = this@ParentPaymentMainActivity
        dialog.clickListeners = object : PayBillPopup.ClickListener {
            override fun onBillPayed(dialogFragment: DialogFragment?) {
                callAPItoGetLatestInvoicesList(selectedChild?.id ?: 0)
            }

            override fun onCancelClicked() {

            }
        }
        dialog.show(supportFragmentManager, "PayBillPopup")
    }
}