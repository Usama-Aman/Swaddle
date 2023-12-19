package com.android.swaddle.activities.providers.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.bumptech.glide.Glide
import com.android.swaddle.databinding.ActivityMakeNewGroupBinding
import com.android.swaddle.utils.CustomToasts
import com.theartofdev.edmodo.cropper.CropImage
import org.jetbrains.anko.startActivity

class MakeNewGroupActivity : BaseActivity() {
    private lateinit var binding: ActivityMakeNewGroupBinding
    var fileUri: Uri? = null
    var filePath = ""
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeNewGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        listener()
    }

    private fun listener() {
        binding.btnCreate.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }

        binding.btnCreate.setOnClickListener {
            if (validate()) {
                finish()
                startActivity<ChatActivity>(
                    "newChat" to true,
                    "userId" to "$userId",
                    "type" to "group",
                    "title" to binding.etName.text.toString().trim(),
                    "groupPhoto" to filePath,
                    "main_name" to binding.etName.text.toString()
                )
                NewMessageActivity.newMessageActivity
                NewGroupChatActivity.newGroupChatActivity
                finish()
            }
        }
    }

    private fun initVar() {
        userId = intent?.getStringExtra("userId") ?: ""
    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                filePath = resultUri.path.toString()
                fileUri = resultUri
                //  fileUri = Uri.fromFile(File(image.path))
                Log.e("SELECTED_URI", fileUri.toString() + "")
                Glide.with(this).load(filePath)
                    .into(binding.ivProfile)
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //   val error = result.error
        }
    }

    private fun validate(): Boolean {
        if (binding.etName.text.toString().trim() == "") {
            CustomToasts.failureToast(
                this@MakeNewGroupActivity,
                "Please enter group title!"
            )
            return false
        }
        return true
    }
}