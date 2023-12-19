package com.android.swaddle.fragments.bottomSheetFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.R
import com.android.swaddle.adapters.PromoteChildAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.BottomSheetManageStaffBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class ManageStaffBottomSheet(
    var staff: User

) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetManageStaffBinding
    private lateinit var clickListener: ClickListener
    var selectedClassRoom: ClassroomDetails? = null
    var adapter: PromoteChildAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetManageStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        if (staff.attendance?.size ?: 0 > 0)
            if (staff.attendance?.last()?.status == null) {
                binding.llUpdateAttandance.viewGone()
                binding.llMarkAttendance.viewVisible()
            } else {
                binding.llUpdateAttandance.viewVisible()
                binding.llMarkAttendance.viewGone()
            }
        else{
            binding.llUpdateAttandance.viewGone()
            binding.llMarkAttendance.viewVisible()
        }

        binding.llUpdateAttandance.setOnClickListener {
            if (validate()) {
                dismiss()
                clickListener.onMarkAttendanceClicked(selectedClassRoom)
            }
        }

        binding.llMarkAttendance.setOnClickListener {
            if (validate()) {
                dismiss()
                clickListener.onMarkAttendanceClicked(selectedClassRoom)
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.llDelete.setOnClickListener {
            if (validate()) {
                dismiss()
                clickListener.onDeleteStaffClicked(selectedClassRoom)
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

    interface ClickListener {
        fun onMarkAttendanceClicked(selectedClassRoom: ClassroomDetails?)
        fun onDeleteStaffClicked(selectedClassRoom: ClassroomDetails?)
        fun onCancelClicked()
    }

    private fun validate(): Boolean {

//        if (!baseActivity.isNetworkConnected()) {
//            showErrorToast(baseActivity, "No internet connection!")
//            return false
//        }

//        if (selectedClassRoom == null) {
//            showErrorToast(baseActivity, "Please select classroom first.")
//            return false
//        }
        return true
    }
}