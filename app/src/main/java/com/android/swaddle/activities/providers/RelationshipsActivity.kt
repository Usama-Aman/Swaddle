package com.android.swaddle.activities.providers

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.activities.parent.AddRelationship
import com.android.swaddle.activities.parent.ChildProfileActivity
import com.android.swaddle.adapters.AllergiesAdapter
import com.android.swaddle.adapters.CustodyDocumentAdapter
import com.android.swaddle.adapters.provider_adapter.RelationAdapter
import com.android.swaddle.adapters.spinneradapter.ClassSpinnerAdapter
import com.android.swaddle.adapters.spinneradapter.SelectChildSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAboutRelationShipBinding
import com.android.swaddle.enums.UserType
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.networkManager.RetrofitClassNew
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.codekidlabs.storagechooser.StorageChooser
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RelationshipsActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutRelationShipBinding
    private var classRoomsList = ArrayList<ClassroomDetails>()
    private var childesList = ArrayList<ChildInfo>()
    private var relationsList = ArrayList<User>()

    private var selectedChild: ChildInfo? = null
    private var selectedClassRoom: ClassroomDetails? = null

    private var classRoomsAdapter: ClassSpinnerAdapter? = null
    private var childesAdapter: SelectChildSpinnerAdapter? = null

    private var relationAdapter: RelationAdapter? = null
    var selectedPos = 0

    private var builder = StorageChooser.Builder()
    private var chooser: StorageChooser? = null
    private var documents: ArrayList<String> = ArrayList()
    private var newDocuments: ArrayList<String> = ArrayList()

    var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 1

    var newFilesSelected = false
    private var filesAdapter: CustodyDocumentAdapter? = null
    var downloadedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutRelationShipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedPos = intent.getIntExtra("position", 0)

        initVar()

        if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
            binding.tvUpload.viewGone()
        } else {
            binding.tvUpload.viewVisible()
        }
        if (userManager.user?.type.equals("provider") || userManager.user?.type.equals("staff")) {
            showLoading()
            callAPItoGetClassRooms()
//            binding.ivAdd.viewVisible()
            binding.cvSpinnerClass.viewVisible()
            binding.tvClass.viewVisible()
        } else {
            showLoading()
            callAPItoGetParentsChildes()
            binding.cvSpinnerClass.viewGone()
            binding.tvClass.viewGone()
//            binding.ivAdd.viewGone()
        }
        listener()
    }

    private fun setAllergiesAdapter(rvTags: RecyclerView, data: ArrayList<String>) {
        var layoutManager = FlexboxLayoutManager(this@RelationshipsActivity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        rvTags.layoutManager = layoutManager
        var adapter =
            AllergiesAdapter(
                this@RelationshipsActivity,
                data
            )
        rvTags.adapter = adapter

        if (data.size > 0) {
            binding.tvNoAllergies.viewGone()
            binding.rvTags.viewVisible()
        } else {
            binding.rvTags.viewGone()
            binding.tvNoAllergies.viewVisible()
        }
    }

    private fun callAPItoGetChildDetails() {
        showLoading()
        val call: Call<ChildDetailsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChildDetails(
                "Bearer " + userManager.accessToken ?: "",
                (selectedChild?.id ?: 0).toString(),
            )
        call.enqueue(object : Callback<ChildDetailsResponse> {
            override fun onResponse(
                call: Call<ChildDetailsResponse>,
                response: Response<ChildDetailsResponse>
            ) {
                hideLoading()
                relationsList = response.body()?.childInfo?.relations ?: ArrayList()

                try {
                    var array = JSONArray(response.body()?.childInfo?.custodyDocument ?: "")
                    documents = ArrayList()
                    for (i in 0 until array.length()) {
                        documents.add(array[i].toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                setUpCustodyDocuments()
                setRelationsRecyclerView()
            }

            override fun onFailure(
                call: Call<ChildDetailsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@RelationshipsActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun listener() {
        binding.tvChildName.setOnClickListener {
            val intent =
                Intent(this@RelationshipsActivity, ChildProfileActivity::class.java)
            intent.putExtra("item", selectedChild)
            startActivity(intent)
        }



        binding.tvUpload.setOnClickListener {
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

        binding.ivAdd.setOnClickListener {
            startActivity(Intent(this, AddRelationship::class.java))
        }

//        if (userManager.user?.type == Constants.parent) {
//            binding.ivAdd.viewVisible()
//        } else {
//            binding.ivAdd.viewGone()
//        }

        binding.imgBack.setOnClickListener {
            finish()
        }

    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@RelationshipsActivity, "No internet connection!")
            return false
        }

        if (documents.size == 0) {
            showErrorToast(this@RelationshipsActivity, "Please select custody documents!")
            return false
        }

        return true
    }

    private fun initVar() {
        if (UserData.getUserType(this).equals(UserType.PARENT.type)) {
            binding.ivAdd.viewVisible()
        } else {
            binding.ivAdd.viewVisible()
        }

        builder.withActivity(this@RelationshipsActivity)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .build()

        chooser = builder.build()

        chooser?.setOnMultipleSelectListener { paths ->
            Log.e("SELECTED_PATH", paths.size.toString())
            newFilesSelected = true
            newDocuments.addAll(paths)
            filesAdapter?.setData(documents)
            if (validate())
                sendToServer()
        }
        chooser?.setOnSelectListener { path ->
            Log.e("SELECTED_PATH", path)
            newFilesSelected = true
            newDocuments.addAll(listOf(path))
            filesAdapter?.setData(documents)
            if (validate())
                sendToServer()
        }
    }

    private fun setUpCustodyDocuments() {
        if (filesAdapter == null) {
            filesAdapter = CustodyDocumentAdapter(this, documents, true)
            binding.rvFiles.adapter = filesAdapter

            filesAdapter?.setListener(object : CustodyDocumentAdapter.CLickListener {
                override fun onItemClick(pos: Int) {

                }

                override fun onRemoveClick(pos: Int) {
                    if (documents[pos].contains("/childs/custody_document")) {
                        val alert =
                            AlertView(
                                "Remove?",
                                "Are you sure you want to Remove file?",
                                AlertStyle.DIALOG
                            )
                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {

                            callAPItoRemoveMedia(documents[pos], "${selectedChild?.id}", pos)
                        })
                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                        })
                        alert.show(this@RelationshipsActivity)

                    } else {
                        documents.removeAt(pos)
                        filesAdapter?.setData(documents)
                    }
                }

                override fun onDownloadClick(position: Int) {
                    permissionAccess(position)
                    downloadedPosition = position
                }
            })
        } else {
            filesAdapter?.concatinateData(documents)
        }

        if (documents.size > 0) {
            binding.tvNoDocuments.viewGone()
        } else {
            binding.tvNoDocuments.viewVisible()
        }
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
                "filePath", Constants.IMG_BASE_PATH + documents[position] ?: ""
            )
            val fileName = (documents[position] ?: "").split("/").last()

            val request = DownloadManager.Request(
                Uri.parse(Constants.IMG_BASE_PATH + (documents[position] ?: ""))
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
        val result = ContextCompat.checkSelfPermission(this@RelationshipsActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@RelationshipsActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@RelationshipsActivity,
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
                        this@RelationshipsActivity,
                        "The app was not allowed permissions. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun callAPItoRemoveMedia(media: String, id: String, pos: Int) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteCustodyDocument("Bearer " + userManager.accessToken ?: "", id, media)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {

                    showSuccessToast(
                        this@RelationshipsActivity,
                        response.body()?.message ?: ""
                    )
                    documents.removeAt(pos)
                    filesAdapter?.setData(documents)

                    newFilesSelected = documents.any { !it.contains("/childs/custody_document") }
//                    callAPItoActivities()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@RelationshipsActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@RelationshipsActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@RelationshipsActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setRelationsRecyclerView() {
        if (relationAdapter == null) {
            val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recRelatoin.layoutManager = manager
            relationAdapter = RelationAdapter(this, relationsList)
            binding.recRelatoin.adapter = relationAdapter
        } else {
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }

        if (relationsList.size > 0) {
            binding.tvNoRelations.viewGone()
        } else {
            binding.tvNoRelations.viewVisible()
        }
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
                classRoomsList = response.body()?.data ?: ArrayList()
                setClassSpinner()
            }

            override fun onFailure(
                call: Call<ClassRoomsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@RelationshipsActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun setChildSpinner() {
        if (childesAdapter == null) {
            childesAdapter = SelectChildSpinnerAdapter(this@RelationshipsActivity, childesList)
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
                    setUpSelectedChildInfo(selectedChild ?: ChildInfo())
                    callAPItoGetChildDetails()
                }
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (childesList.size > 0) {
            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            binding.tvNoChildData.viewGone()
            binding.llChildDetails.viewVisible()
            binding.tvNoRelations.viewGone()

            selectedChild = childesList.first()
            setUpSelectedChildInfo(selectedChild ?: ChildInfo())
            callAPItoGetChildDetails()
        } else {
            binding.tvNoRelations.viewVisible()
            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            binding.tvNoChildData.viewVisible()
            binding.llChildDetails.viewGone()
            relationsList.clear()
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }
    }

    private fun setClassSpinner() {

        if (classRoomsAdapter == null) {
            classRoomsAdapter = ClassSpinnerAdapter(this@RelationshipsActivity, classRoomsList)
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
                callAPItoGetChildes(classRoomsList[position].id)
                Log.e("SelectedRoom", classRoomsList[position].id.toString())
                //   showSuccessToast(this@DailyReportActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (classRoomsList.size > 0) {
            binding.tvSpinnerClassRoomNoData.viewGone()
            binding.spClassRoom.viewVisible()
            callAPItoGetChildes(classRoomsList.first().id)

            binding.tvSpinnerChildesNoData.viewGone()
            binding.spChild.viewVisible()
            binding.tvNoChildData.viewGone()
            binding.llChildDetails.viewVisible()

        } else {
            binding.tvSpinnerClassRoomNoData.viewVisible()
            binding.spClassRoom.viewGone()

            binding.tvSpinnerChildesNoData.viewVisible()
            binding.spChild.viewGone()
            binding.tvNoChildData.viewVisible()
            binding.llChildDetails.viewGone()
            relationsList.clear()
            relationAdapter?.setItems(relationsList)
            relationAdapter?.notifyDataSetChanged()
        }

        if (selectedPos != 0) {
            binding.spClassRoom.setSelection(selectedPos)
            selectedClassRoom = classRoomsList[selectedPos]
        }
    }

    private fun callAPItoGetChildes(id: Int?) {

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
                setChildSpinner()
            }

            override fun onFailure(
                call: Call<ChildesResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@RelationshipsActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun callAPItoGetParentsChildes() {

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
                showSuccessToast(this@RelationshipsActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }


    private fun setUpSelectedChildInfo(item: ChildInfo) {
        try {
            var allAlgs = ArrayList<String>()
            if (item.foodAllergies != null || item.environmentalAllergies != null) {

                if (item.foodAllergies != null) {

                    var algs = JSONArray(item.foodAllergies ?: "")
                    for (i in 0 until algs.length()) {
                        allAlgs.add(algs[i].toString())
                    }
                }
                if (item.environmentalAllergies != null) {
                    var environmentalAllergies = JSONArray(item.environmentalAllergies ?: "")
                    for (i in 0 until environmentalAllergies.length()) {
                        allAlgs.add(environmentalAllergies[i].toString())
                    }
                }
                setAllergiesAdapter(binding.rvTags, allAlgs)
            } else {
                binding.tvNoAllergies.viewVisible()
                setAllergiesAdapter(binding.rvTags, allAlgs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Glide.with(this@RelationshipsActivity).load(Constants.IMG_BASE_PATH + item.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivChildImg)

        if (item.custodyDescription.isNullOrEmpty()) {
            binding.tvCustodyHead.viewGone()
            binding.tvCustodyOperationDetail.viewGone()
        } else {
            binding.tvCustodyHead.viewVisible()
            binding.tvCustodyOperationDetail.viewVisible()
            binding.tvCustodyOperationDetail.text = item.custodyDescription
        }
        binding.tvChildName.text = item.firstName + " " + item.lastName
        binding.tvChildDob.text = DateConverter.DateFormatFour(item.dob ?: "")
        binding.tvChildEnrolmentDate.text = DateConverter.TimeStampNew(item.createdAt ?: "")
        binding.tvChildEpiPen.text = item.doesChildRequireEpiPin
        /*  if (item.epiPenExpirationDate == null) {
              binding.llEPIPenExpiration.viewGone()
              binding.viewEPIPen.viewGone()
          } else {
              binding.llEPIPenExpiration.viewVisible()
              binding.viewEPIPen.viewVisible()
          }
          binding.tvChildEpiPenExpiryDate.text =
              DateConverter.convertEPPenDate(item?.epiPenExpirationDate ?: "")*/
        if (selectedChild?.classroomDetails != null) {
            binding.tvChildClassRoom.text = selectedChild?.classroomDetails?.name
        } else {
            binding.tvChildClassRoom.text = "No Classroom Associated."
        }
//        binding.tvChildAge.text = if ((DateConverter.getAge(
//                DateConverter.getYear(item.dob ?: "").toInt(),
//                DateConverter.getMonth(item.dob ?: "").toInt(),
//                DateConverter.getDayNew(item.dob ?: "").toInt()
//            ) ?: "0").toInt() < 1
//        ) {
//            "Less than ONE year."
//        } else {
//            (DateConverter.getAge(
//                DateConverter.getYear(item.dob ?: "").toInt(),
//                DateConverter.getMonth(item.dob ?: "").toInt(),
//                DateConverter.getDayNew(item.dob ?: "").toInt()
//            ) ?: "0")
//        }
        if (item.custody == "Part Time") {
            binding.tvChildAge.text = "Shared Custody"
        } else {
            binding.tvChildAge.text = item.custody
        }

    }


    private fun sendToServer(

    ) {
        showProgressBar()
        var call: Call<LoginResponse>? = null
        var documentParts = ArrayList<MultipartBody.Part?>()
        documents.forEachIndexed { index, s ->
            if (!s.contains("/childs/custody_document")) {
                val documentURI = Uri.parse(s)
                val document = File(documentURI.path)
                documentParts.add(
                    MultipartBody.Part.createFormData(
                        "files",
                        document.name,
                        RequestBody.create("*/*".toMediaTypeOrNull(), document)
                    )
                )
            }
        }
        newDocuments.forEachIndexed { index, s ->
            if (!s.contains("/childs/custody_document")) {
                val documentURI = Uri.parse(s)
                val document = File(documentURI.path)
                documentParts.add(
                    MultipartBody.Part.createFormData(
                        "files",
                        document.name,
                        RequestBody.create("*/*".toMediaTypeOrNull(), document)
                    )
                )
            }
        }

        val childId: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            (selectedChild?.id ?: 0).toString(),
        )

        call =
            RetrofitClassNew.getInstance().webRequestsInstance.updateCustodyDocuments(
                "Bearer " + userManager.accessToken ?: "",
                childId,
                documentParts
            )

        call?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                runOnUiThread {
                    hideLoading()
                }
                if (response.body()?.status == true) {
                    newDocuments.clear()
                    showSuccessToast(this@RelationshipsActivity, response.body()?.message ?: "")
                    hideProgressBar()
                    callAPItoGetChildDetails()
                } else {

                    hideProgressBar()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@RelationshipsActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@RelationshipsActivity, response.body()?.message ?: "")
                    }
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                t.printStackTrace()

                Log.i("errordetail", "${t.printStackTrace().toString()}")

                Toast.makeText(
                    this@RelationshipsActivity,
                    "Can't connect to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun showProgressBar() {
        binding.tvUpload.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvUpload.viewVisible()
        binding.progressbar.viewGone()
    }
}