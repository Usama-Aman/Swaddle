package com.android.swaddle.bottomsheets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.providers.class_room_ui.CreateClassRoom
import com.android.swaddle.databinding.ClassroomOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class ClassroomOptionsBottomSheet() :
    BottomSheetDialogFragment() {
    private lateinit var binding: ClassroomOptionsBottomSheetBinding
    private lateinit var clickListener: ItemClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ClassroomOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llDelete.setOnClickListener {
            clickListener.deleteClassRoom()
        }

        binding.llCancel.setOnClickListener {
            dismiss()
        }

        binding.llEdit.setOnClickListener {
            clickListener.editClassRoom()
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

    fun setListener(listener: ItemClickListener) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun editClassRoom()
        fun deleteClassRoom()
    }


}