package com.android.swaddle.activities.providers.class_room_ui

import android.os.Bundle
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityCreateClassRoomBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.models.ClassroomDetails
import org.jetbrains.anko.startActivity

class CreateClassRoom : BaseActivity() {
    private lateinit var binding: ActivityCreateClassRoomBinding
companion object{
    var createClassRoom:CreateClassRoom? = null
}

    var type = "create"
    var classRoomDetail : ClassroomDetails? = null
    var classRoomId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateClassRoomBinding.inflate(layoutInflater)

        classRoomDetail = intent.getSerializableExtra("item") as ClassroomDetails?
        type = intent.getStringExtra("type").toString()

        setContentView(binding.root)
        init()
        listener()


        if(classRoomDetail!=null){

            classRoomId = (classRoomDetail?.id?:0).toString()
            binding.etName.setText(classRoomDetail?.name)
            binding.etDesc.setText(classRoomDetail?.description)

        }

    }

    private fun listener() {
        binding.btnNext.setOnClickListener {
            if (validation()) {
                startActivity<SelectChildrenActivity>(
                    "name" to binding.etName.text.toString().trim(),
                    "desc" to binding.etDesc.text.toString().trim(),
                    "type" to type,
                    "room_id" to classRoomId, "item" to classRoomDetail
                )
            }
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        createClassRoom = this@CreateClassRoom
    }

    private fun validation(): Boolean {
        if (!isNetworkConnected()) {
            showErrorToast(this@CreateClassRoom, "No internet connection!")
            return false
        }
        val name = binding.etName.text.toString()
        val desc = binding.etDesc.text.toString()

        if (name.isBlank()) {
            showErrorToast(this@CreateClassRoom, "Please enter name of Classroom!")
            return false
        }

        if (desc.isEmpty()) {
            showErrorToast(this@CreateClassRoom, "Please enter description of Classroom!")
            return false
        }

        return true
    }
}