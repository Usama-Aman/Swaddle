package com.android.swaddle.activities.providers.messages

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.adapters.provider_adapter.MessagesAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.BaseApplication
import com.android.swaddle.databinding.ActivityChatBinding
import com.android.swaddle.helper.VideoDisplayActivity
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.IOSocketManager
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.networkManager.SocketNames
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.slider.MediaSliderActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.application.brittany.dixon.utils.PhotoUtil
import com.bumptech.glide.Glide
import com.codekidlabs.storagechooser.StorageChooser
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.theartofdev.edmodo.cropper.CropImage
import com.yalantis.ucrop.UCrop
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltipUtils
import kotlinx.android.synthetic.main.activity_add_report.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.no_data_layout.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : BaseActivity(), MessagesAdapter.ClickListener {
    private lateinit var binding: ActivityChatBinding
    private var adapter: MessagesAdapter? = null
    private var chatId = ""
    private var newChat = false
    private var userId = ""
    private var type = "single"
    private var title = ""
    private var groupPhoto = ""
    var formats = ArrayList<String>()
    private var isFileSelected: Boolean = false
    private var name: String = ""


    private var p1 = Manifest.permission.READ_EXTERNAL_STORAGE
    private var p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private var p3 = Manifest.permission.ACCESS_FINE_LOCATION

    private var selectedFilePath = ""
    private var uploadedFilePath = ""
    private val PERMISSION_REQUEST_CODE = 1
    private var mPath: ArrayList<String>? = null
    private var list = ArrayList<Message>()
    var manager:
            LinearLayoutManager? = null

    private var builder = StorageChooser.Builder()
    private var chooser: StorageChooser? = null


    companion object {
        private const val PDF_SELECTION_CODE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent?.getStringExtra("chat_id") ?: ""
        userId = intent?.getStringExtra("userId") ?: ""
        newChat = intent?.getBooleanExtra("newChat", false) ?: false
        type = intent?.getStringExtra("type") ?: "single"

        Log.e("chat type", type)
        Log.e("newChat ", newChat.toString())
        Log.e("chatId ", chatId)
        title = intent?.getStringExtra("title") ?: ""
        groupPhoto = intent?.getStringExtra("groupPhoto") ?: ""
        name = intent?.getStringExtra("main_name") ?: ""
        binding.tvHead.text = name
        initListeners()

        if (!chatId.isNullOrEmpty()) {
            callAPIToGetMessages()
            callAPIToReadChat()
        }
        socketReceiveMessage()
        keyboardListener()
        initFileChooser()
    }

    private fun initFileChooser() {
        /*
        formats.add("txt")
          formats.add("xlx")
          formats.add("xlxs")
         formats.add("docx")*/

        formats.add("application/pdf")
        builder.customFilter(formats)

        builder.withActivity(this)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .customFilter(formats)
            .build()

        chooser = builder.build()

        chooser?.setOnSelectListener { path ->
            // Log.e("SELECTED_PATH", paths.size.toString())
            isFileSelected = true
            selectedFilePath = path
            if (validate()) {
                if (newChat) {
                    callAPIToCreateChatAndSendMessage(
                        binding.etMessage.text.toString().trim(),
                        selectedFilePath
                    )
                } else {
                    callAPIToSendMessage(
                        binding.etMessage.text.toString().trim(),
                        selectedFilePath
                    )
                }
            }

        }
    }

    private fun keyboardListener() {
        binding.rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            binding.rootLayout.getWindowVisibleDisplayFrame(rec)
            //finding screen height
            val screenHeight = binding.rootLayout.rootView.height
            //finding keyboard height
            val keypadHeight = screenHeight - rec.bottom
            if (keypadHeight > screenHeight * 0.15) {
                Handler().postDelayed(
                    { manager?.scrollToPosition((adapter?.itemCount!!) - 1) },
                    50
                )
            }
        }
    }

    private fun callAPIToGetMessages(showLoading: Boolean = true) {
        if (showLoading) {
            showLoading()
        }
        val call: Call<SingleChatsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getChatMessages("Bearer " + userManager.accessToken, chatId)
        call.enqueue(object : Callback<SingleChatsResponse> {
            override fun onResponse(
                call: Call<SingleChatsResponse>,
                response: Response<SingleChatsResponse>
            ) {
                val myResponse = response.body()
                if (myResponse?.status == true) {

                    if (myResponse.data?.messages != null) {
                        list = myResponse.data?.messages ?: ArrayList()
                        if (list.size > 0)
                            chatId = "${list.first().chatId}"
                    }
                    setUpMessages()
                } else {
                    setUpMessages()
//                    CustomToasts.failureToast(mActivity, response.message() ?: "")
                    Handler().postDelayed({
//                        callAPIToGetMessages(showLoading = false)
                    }, 500)
                }
                hideProgressBar()
                hideLoading()
            }

            override fun onFailure(
                call: Call<SingleChatsResponse>,
                t: Throwable
            ) {
                setUpMessages()
                Handler().postDelayed({
//                    callAPIToGetMessages()
                }, 500)
                hideLoading()
//                CustomToasts.failureToast(mActivity, "Can't connect to server")
                t.printStackTrace()
            }
        })
    }

    private fun validate(): Boolean {
        if (!isNetworkConnected()) {
            CustomToasts.failureToast(this@ChatActivity, "No internet connection!")
            return false
        }
        if (binding.etMessage.text.toString().trim() == "" && selectedFilePath == "") {
            CustomToasts.failureToast(
                this@ChatActivity,
                "Please type a valid message or select media!"
            )
            return false
        }
        return true
    }


    private fun setUpMessages() {
        binding.rvMessages.setHasFixedSize(true)
        if (adapter == null) {
            adapter =
                MessagesAdapter(
                    this@ChatActivity,
                    list,
                    this
                )
            manager =
                LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)


            binding.rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        // Scrolling up
                    } else {
                        // Scrolling down
                    }
                    try {
                        binding.tvDate.text =
                            "${DateConverter.convertDateMonthWithToday(list[manager?.findFirstCompletelyVisibleItemPosition() ?: 0].createdAt ?: "")}"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        // Do something
                    } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        // Do something
                    } else {
                        // Do something
                    }
                }
            })
            binding.rvMessages.layoutManager = manager
            binding.rvMessages.adapter = adapter
        } else {
            adapter?.setData(list)
            adapter?.notifyDataSetChanged()
        }
        Handler().postDelayed({
            //doSomethingHere()
            manager?.scrollToPosition((adapter?.itemCount ?: 0) - 1)
        }, 50)

//        Handler().postDelayed({
//            //doSomethingHere()
//            manager?.scrollToPosition((adapter?.itemCount ?: 0) - 1)
//        }, 10)

//        if (swipeToRefresh.isRefreshing) {
//            swipeToRefresh.isRefreshing = false
//        }
        if ((list.size ?: 0) > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewVisible()
        }

        Handler().postDelayed({
            runOnUiThread {
                binding.rvMessages.smoothScrollToPosition(
                    binding.rvMessages.adapter?.itemCount ?: 0 - 1
                )
            }
        }, 50)

    }

    private fun callAPIToReadChat() {
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .readChat(
                "Bearer " + userManager.accessToken,
                chatId
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
//                CustomToasts.failureToast(this@ChatActivity, "Can't connect to server")
                t.printStackTrace()
            }
        })
    }

    private fun socketReceiveMessage() {
        sharedSocket.init(SocketNames.socketReceiveMessage) { obj ->
            Log.e("socketReceiveMessage", obj.toString())
            try {
//                { "chat_id":"14", "sender_id":2, "receiver_id":"", "sender_name":"Lucas Russel", "sender_image":"storage\/users\/profile\/0247b52b7c95205057ad.png", "message":"ru", "file_type":"", "file_path":"", "file_ratio":"", "div_img":"", "created_at":"2021-12-01 09:53:59" }
                if ("${(obj.getInt("chat_id"))}" == chatId) {
                    callAPIToGetMessages(false)
//                    callAPIToReadChat()


//                    list.add(
//                        Message(
//                            -1, obj.getInt("chat_id"), obj.getInt("sender_id"),
//                            obj.getString("message"), obj.getString("file_type"),
//                            obj.getString("file_path"), obj.getString("file_ratio"),
//                            -1, obj.getString("created_at"), obj.getString("created_at"),
//                            null, null
//                        )
//                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    private fun socketSendNotification() {
        try {
            sharedSocket = IOSocketManager()
            var obj = JSONObject()
            obj.put("notification_id", "")
            obj.put("sender_id", "")
            obj.put("receiver_id", "")
            obj.put("sender_name", "")
            obj.put("sender_image", "")
            obj.put("type_id", "")
            obj.put("noti_type", "")
            obj.put("noti_text", "")
            obj.put("created_at", DateConverter.currentDateNew)

            Log.e("messageSocket", obj.toString())
            sharedSocket.send(SocketNames.socketSendMessage, obj)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun socketSendMessage() {
        try {
            sharedSocket = IOSocketManager()
            var obj = JSONObject()
            obj.put("chat_id", chatId)
            obj.put("sender_id", userManager.user?.id)
            obj.put("receiver_id", userId)
            obj.put("sender_name", userManager.user?.firstName + " " + userManager.user?.lastName)
            obj.put("sender_image", userManager.user?.profilePicture)
            obj.put("message", binding.etMessage.text.toString().trim())
            obj.put("file_type", "")
            obj.put("file_path", "")
            obj.put("file_ratio", "")
            obj.put("div_img", "")
            obj.put("created_at", DateConverter.currentDateNew)

            Log.e("socketSendMessage", obj.toString())
            sharedSocket.send(SocketNames.socketSendMessage, obj)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun chooserDialog() {
        val alert =
            AlertView(
                "Alert!",
                "Please choose an option",
                AlertStyle.DIALOG
            )
        alert.addAction(AlertAction("File", AlertActionStyle.DEFAULT) {
            permissionAccess(fileType = "file")
        })
        alert.addAction(AlertAction("Gallery", AlertActionStyle.DEFAULT) {
            permissionAccess()
        })
        alert.show(this)
    }

    private fun initListeners() {
        binding.imgViewBack.setOnClickListener {
            finish()
        }
        binding.ivOptions.setOnClickListener {
            showMenuPopup() {
                if (it) {
                    val alert =
                        AlertView(
                            "Delete Chat?",
                            "Are you sure you want delete chat? you won't be able to recover it.",
                            AlertStyle.DIALOG
                        )
                    alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {

                        showLoading("Please wait...")
                        callAPIToDeleteChats(chatId)
                    })
                    alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                    })
                    alert.show(this@ChatActivity)
                }
            }
        }
        binding.ivAttachment.setOnClickListener {

//            showMediaSelection(ivAttachFiles) {}
//            permissionAccess()
            chooserDialog()
        }

        binding.noData.retryBtn.setOnClickListener {
            callAPIToGetMessages()
        }
        binding.ivSend.setOnClickListener {
            if (validate()) {
                if (newChat) {
                    callAPIToCreateChatAndSendMessage(
                        binding.etMessage.text.toString().trim(),
                        selectedFilePath
                    )
                } else {
                    callAPIToSendMessage(
                        binding.etMessage.text.toString().trim(),
                        selectedFilePath
                    )
                }
            }
        }

        binding.ivDeleteSelectedFile.setOnClickListener {
            binding.ivAttachment.viewVisible()
            binding.ivSelectedFile.viewGone()
            binding.rlRemoveSelectedImage.viewGone()
            selectedFilePath = ""
            uploadedFilePath = ""
            binding.etMessage.viewVisible()
            mPath?.clear()
        }
    }

    private fun callAPIToCreateChatAndSendMessage(
        msg: String, filePath: String
    ) {
        showProgressBar()
        var api = AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "create_chat")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken)
        api.addHeaders("Connection", "close")
        if (selectedFilePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path)
            api.addMultipartFile("file", file)
            if (isFileSelected) {
                api.addMultipartParameter("message", "file")
                showProgressBar()

            } else {
                api.addMultipartParameter("message", msg)
            }
        } else {
            api.addMultipartParameter("message", msg)
        }
        Log.e("chatId ", chatId)

        if (chatId != "") {
            api.addMultipartParameter("chat_id", chatId)
        }
        if (type == "single") {

            val members: MutableList<String> = ArrayList()

//            for (i in 0..userId.length - 1) {
//                if (userId[i].toString() != ",")
            members.add(userId.toString())
//            }

            if (userManager.user?.type == Constants.parent && intent?.getBooleanExtra("isReceiverStaff", false) == true) {
                api.addMultipartParameter("type", "group")
                api.addMultipartParameter("title", title)
                if (!members.contains(userManager.user?.linkedTo.toString()))
                    members.add(userManager.user?.linkedTo.toString())
            } else
                api.addMultipartParameter("type", type)

            userId = TextUtils.join(",", members)

            api.addMultipartParameter("members[]", "$userId")
        } else {
            api.addMultipartParameter("type", type)
            api.addMultipartParameter("members[]", "$userId")
            if (groupPhoto != "") {
                val groupPhotoURI = Uri.parse(groupPhoto)
                val groupPhotoFile = File(groupPhotoURI.path)
                api.addMultipartFile("group_photo", groupPhotoFile)
            }
            api.addMultipartParameter("title", title)
        }
        api.addHeaders("Connection", "close")
            .setTag("send_message")
            .setPriority(com.androidnetworking.common.Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
                if (!isFileSelected) {

                    var progress = (bytesUploaded * 100) / totalBytes
                    Log.e(
                        "progress",
                        "$progress, TotalBytes: $totalBytes, BytesUploaded: $bytesUploaded"
                    )
                    if (selectedFilePath != "" && groupPhoto != "") {
                        binding.rlMediaProgress.viewVisible()
                        binding.cpvMediaProgress.progress = progress.toFloat()
                    }
                }
                isFileSelected = false
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    // do anything with response
                    val myResponse = response.toString()
                    Log.e("response", myResponse)

//                    var model = SendMessageResponse()
//                    model = gson.fromJson(response.toString(), SendMessageResponse::class.java)
//                    list.add(model.successData?.messageToSend?.message ?: Message())
//                    socketSendMessage(response.toString())
//                    socketSendNotifyMessage(
//                        (model.successData?.messageToSend?.receiverId ?: 0).toString()
//                    )
                    var model =
                        gson.fromJson(myResponse, TemporaryChatResponse::class.java)
                    chatId = "${model.data?.messages?.id ?: model.data?.id}"
                    newChat = false
                    if (model.message == "Chat has been already created.") {
                        callAPIToSendMessage(
                            binding.etMessage.text.toString().trim(),
                            selectedFilePath
                        )
                    } else {
                        socketSendMessage()
                    }

                    resetBottomChatMsg()
//                    callAPIToSendMessage(
//                        binding.etMessage.text.toString().trim(),
//                        selectedFilePath
//                    )
//                    callAPIToGetMessages(false)
//                    populateMessages(response)
//                    hideProgressBar()
                }

                override fun onError(error: ANError?) {
                    // handle error
                    hideProgressBar()
                    Log.e("response", error?.message ?: "")
                    error?.printStackTrace()
                    CustomToasts.failureToast(
                        this@ChatActivity,
                        "Error while sending message!"
                    )
                }
            })
    }

    private fun populateMessages(response: JSONObject?) {

        var model = gson.fromJson(response.toString(), SingleChatsResponse::class.java)
        list = model.data?.messages ?: ArrayList()
        if (list.size > 0)
            chatId = "${list.first().chatId}"
        adapter?.setData(list)
        adapter?.notifyDataSetChanged()
        Handler().postDelayed({
            //doSomethingHere()
            manager?.scrollToPosition((adapter?.itemCount ?: 0) - 1)
        }, 50)

    }

    private fun callAPIToSendMessage(
        msg: String, filePath: String
    ) {
        showProgressBar()
        var api = AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "send_message_mobile")
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken)
        if (selectedFilePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path)
            api.addMultipartFile("file", file)
            if (isFileSelected) {
                api.addMultipartParameter("message", "file")
                showProgressBar()
            } else {
                api.addMultipartParameter("message", msg)
            }
        } else {
            api.addMultipartParameter("message", msg)
        }

        if (chatId != "") {
            api.addMultipartParameter("chat_id", chatId)
        }
        api.setTag("send_message")
            .setPriority(com.androidnetworking.common.Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
                if (!isFileSelected) {

                    if (selectedFilePath != "" && groupPhoto != "") {
                        binding.rlMediaProgress.viewVisible()
                    }
                    var progress = (bytesUploaded * 100) / totalBytes
                    binding.cpvMediaProgress.progress = progress.toFloat()
                    Log.e(
                        "progress",
                        "$progress, TotalBytes: $totalBytes, BytesUploaded: $bytesUploaded"
                    )
                }
                isFileSelected = false
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    // do anything with response
                    Log.e("response", response.toString())
                    populateMessages(response)
                    socketSendMessage()
                    resetBottomChatMsg()


//                    var model = SendMessageResponse()
//                    model = gson.fromJson(response.toString(), SendMessageResponse::class.java)
//                    list.add(model.successData?.messageToSend?.message ?: Message())
//                    socketSendMessage(response.toString())
//                    socketSendNotifyMessage(
//                        (model.successData?.messageToSend?.receiverId ?: 0).toString()
//                    )
//                    callAPIToGetMessages(false)
                    hideProgressBar()
                }

                override fun onError(error: ANError?) {
                    // handle error

                    Log.i("chaterror", error?.errorBody.toString())

                    hideProgressBar()
                    error?.printStackTrace()
                    CustomToasts.failureToast(
                        this@ChatActivity,
                        "Error while sending message!"
                    )
                }
            })
    }

    private fun resetBottomChatMsg() {
        binding.ivSelectedFile.viewGone()
        binding.etMessage.viewVisible()
        binding.ivAttachment.viewVisible()
        binding.rlMediaProgress.viewGone()
        binding.rlRemoveSelectedImage.viewGone()

        binding.etMessage.setText("")

        selectedFilePath = ""
        uploadedFilePath = ""
        binding.etMessage.viewVisible()
        mPath?.clear()

    }

    private fun callAPIToDeleteChats(chatId: String) {

        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteChat("Bearer " + userManager.accessToken, chatId)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                val myResponse = response
                if (myResponse.body()?.status == true) {
                    finish()
                } else {
                    CustomToasts.failureToast(this@ChatActivity, myResponse.message() ?: "")
                }
                hideLoading()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                CustomToasts.failureToast(this@ChatActivity, "Can't connect to server")
                t.printStackTrace()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == NewMessageActivity.ImageSelectionRequestCode) {
            mPath = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
            var intent = Intent(this@ChatActivity, VideoDisplayActivity::class.java)
            intent.putExtra("path", mPath?.get(0) ?: "")
            if ((mPath?.get(0) ?: "").contains(".mp4")) {
                intent.putExtra("type", Constants.video)
            } else {
                intent.putExtra("type", Constants.image)
            }
            startActivityForResult(intent, NewMessageActivity.MediaDisplay)

        } else if (resultCode == Activity.RESULT_OK && requestCode == NewMessageActivity.MediaDisplay) {
            val filePath = data?.getStringExtra("path")
            val fileUri = Uri.fromFile(File(filePath ?: ""))
            val type = data?.getStringExtra("type")
            val retry = data?.getBooleanExtra("retry", false)
            if (retry == true) {
                mPath?.clear()
                pickVideo()
            } else {
                //You can get File object from intent
//                val file = ImagePicker.getFile(data)

                //You can also get File Path from intent
                selectedFilePath = fileUri.path ?: ""
                binding.ivAttachment.viewGone()
                //                        ivProfile.setImageURI(fileUri)


                val fileName: String = SimpleDateFormat("yyyyMMddHHmm'.jpg'").format(Date())
                val dir: File =
                    File(Environment.getExternalStorageDirectory(), "/.collegiateChat")
                if (!dir.exists())
                    dir.mkdirs()

//                UCrop.of(
//                    fileUri, Uri.fromFile(File(dir, fileName))
//                ).start(this@ChatActivity)


                CropImage.activity(fileUri)
                    .start(this)

//                binding.etMessage.viewGone()
//                binding.ivAttachment.viewGone()
                binding.rlRemoveSelectedImage.viewVisible()
                binding.ivSelectedFile.viewVisible()

                binding.ivSelectedFile.setOnClickListener { permissionAccess() }
            }
        } else if (resultCode == RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE/* && requestCode == UCrop.REQUEST_CROP*/) {
//            var resultUri = data?.let { UCrop.getOutput(it) };

            val result: CropImage.ActivityResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                val resultUri = result.getUri();

                selectedFilePath = resultUri?.path ?: ""
                Glide.with(this@ChatActivity).load(selectedFilePath)
                    .into(binding.ivSelectedFile)

            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE/*UCrop.RESULT_ERROR*/) {
            var cropError = data?.let { UCrop.getError(it) };
            cropError?.printStackTrace()
            CustomToasts.failureToast(this@ChatActivity, "Error while cropping!")
        }
    }


    private fun permissionAccess(setExcludeVideos: Boolean = false, fileType: String = "") {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1)
            requestPermission(p1)
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2)
            requestPermission(p2)
        } else if (!checkPermission(p3)) {
            Log.e("TAG", p3)
            requestPermission(p3)
        } else {
            if (fileType == "") {
                pickVideo(setExcludeVideos = setExcludeVideos)
            } else {
                chooseFile()
            }

        }
    }


    private fun chooseFile() {
        chooser?.show()
    }


    private fun pickVideo(setExcludeVideos: Boolean = false) {
        val options: Options = Options.init()
            .setRequestCode(NewMessageActivity.ImageSelectionRequestCode) //Request code for activity results
            .setCount(1) //Number of images to restrict selection count
            .setFrontfacing(false) //Front Facing camera on start
//            .setPreSelectedUrls(mPath ?: ArrayList<String>()) //Pre selected Image Urls
            .setExcludeVideos(setExcludeVideos) //Option to exclude videos
            .setVideoDurationLimitinSeconds(30) //Duration for video recording
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientation
            .setPath("/.collegiate/images") //Custom Path For media Storage

        Pix.start(this@ChatActivity, options)
    }

    private fun checkPermission(permission: String): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this@ChatActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this@ChatActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ChatActivity,
                arrayOf(permission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            //Do the stuff that requires permission...
            Log.e("TAG", "Not say request")
        }
    }

    private fun showMenuPopup(callback: (Boolean) -> Unit) {
        var tooltip = SimpleTooltip.Builder(this@ChatActivity).anchorView(binding.ivOptions)
            .gravity(Gravity.BOTTOM)
            .dismissOnOutsideTouch(true).dismissOnInsideTouch(false).showArrow(false)
            .modal(false)
            .animated(false).animationDuration(2000)
            .animationPadding(SimpleTooltipUtils.pxFromDp(0f))
            .contentView(R.layout.drop_messgaes_menu, R.id.parentView).focusable(true).build()

        val parentView = tooltip.findViewById<TextView>(R.id.parentView)
        val tvDelete = tooltip.findViewById<TextView>(R.id.tvDelete)
        val containerDelete = tooltip.findViewById<LinearLayout>(R.id.container_delete)

        tvDelete.setOnClickListener {
            callback(true)
        }
        tooltip.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                Log.e("TAG", "val " + grantResults[0])
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionAccess()
                } else {
                    Toast.makeText(
                        this@ChatActivity,
                        "The app was not allowed permission. Hence, it cannot function properly. Please consider granting it this permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onFileClick(pos: Int, item: Message) {
        downloadManager(Constants.IMG_BASE_PATH + list[pos].filePath, "pdf")
    }

    override fun onMediaClicked(position: Int, item: Message?) {
        val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()

        var tempList = list.filter { it.filePath != null } as ArrayList
        var clickedPosition = tempList.indexOf(item)
        tempList.forEachIndexed { index, model ->

            if (model.fileType == Constants.video) {
                mMediaArray.add(
                    MediaSliderActivity.MediaModel(
                        (Constants.IMG_BASE_PATH + model.filePath ?: ""),
                        "video", Constants.IMG_BASE_PATH + model.filePath ?: ""
                    )
                )
            } else {
                mMediaArray.add(
                    MediaSliderActivity.MediaModel(
                        Constants.IMG_BASE_PATH + model.filePath ?: "",
                        "image",
                        Constants.IMG_BASE_PATH + model.filePath ?: ""
                    )
                )
            }
        }

        loadMediaSliderView(
            mMediaArray,
            clickedPosition,
            "Media Shared with ${item?.sender?.firstName + " " + item?.sender?.lastName}",
            HIDE_DOTS = true
        )
    }

    private fun showProgressBar() {
        binding.ivSend.viewGone()
        binding.sendProgressBar.viewVisible()
        binding.rlRemoveSelectedImage.viewGone()
    }

    private fun hideProgressBar() {
        binding.ivSend.viewVisible()
        binding.sendProgressBar.viewGone()
    }


    private fun downloadManager(url: String, extension: String) {
        val downloadUri = Uri.parse(url)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        request.setTitle("Pdf File");
        request.setDescription("Downloading is in progress")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "" + System.currentTimeMillis() + "." + extension
        )
        request.setMimeType(".pdf")
        request.setMimeType("application/pdf")

        var priceListRefId: Long = -1

        try {
            priceListRefId = downloadManager.enqueue(request)
            Log.i("CheckInstallation", "" + priceListRefId)
        } catch (e: Exception) {
            Log.i("CheckInstallation", "Exception: " + e.message)

        }
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.isChatScreenOpen = true
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.isChatScreenOpen = false
    }


}