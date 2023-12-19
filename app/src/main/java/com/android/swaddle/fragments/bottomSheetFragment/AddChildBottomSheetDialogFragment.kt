package com.android.swaddle.fragments.bottomSheetFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.R
import com.android.swaddle.databinding.DialogPromotBottomSheetBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class AddChildBottomSheetDialogFragment(
    var childInfo: ChildInfo,
    var baseActivity: com.android.swaddle.baseClasses.BaseActivity,
    var fromClassroom: Boolean = false,
    var fromChildListing: Boolean = false,
) : BottomSheetDialogFragment() {
    private lateinit var binding: DialogPromotBottomSheetBinding
    private lateinit var clickListener: ClickListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPromotBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {

        if (baseActivity.userManager.user?.type.equals("provider") || baseActivity.userManager.user?.type.equals(
                "staff"
            )
        ) {

            binding.llDelete.viewGone()
            binding.viewDelete.viewGone()

            if (childInfo.deletedAt == null) {
                binding.llPromote.viewVisible()
                binding.llProfile.viewVisible()

                binding.viewRestore.viewGone()
                binding.llRestore.viewGone()

            } else {
                binding.viewRestore.viewVisible()
                binding.llRestore.viewVisible()

                binding.llPromote.viewGone()
                binding.llProfile.viewGone()
            }

        } else {
            binding.llRestore.viewGone()
            binding.viewRestore.viewGone()
            binding.llDelete.viewGone()
            binding.viewDelete.viewGone()
            if (childInfo.deletedAt != null) {
//                binding.llRestore.viewVisible()
//                binding.viewRestore.viewVisible()
//                binding.llDelete.viewGone()
//                binding.viewDelete.viewGone()
            } else {
//                binding.llRestore.viewGone()
//                binding.viewRestore.viewGone()
//                binding.llDelete.viewVisible()
//                binding.viewDelete.viewVisible()
            }

            binding.llPromote.viewGone()
            binding.llPromote.viewGone()
        }
//        if (fromChildListing) {                   //Usama edit, commented the condition to show the Remove child option to provider.
//            binding.llRemove.viewGone()
//        } else {
        if ((baseActivity.userManager.user?.type.equals("provider") || baseActivity.userManager.user?.type.equals(
                "staff"
            )) && (childInfo.classroomDetails != null || fromClassroom) && childInfo.deletedAt == null
        ) {
            binding.llRemove.viewVisible()
            binding.llEditChild.viewGone() //Usame Edit
        } else {
            binding.llRemove.viewGone()
            binding.llEditChild.viewGone() //Usame Edit
        }
//        }
        binding.llProfile.setOnClickListener {
            clickListener.onOpenProfileClicked(this)
        }

        binding.llPromote.setOnClickListener {
            clickListener.onPromoteClicked(this)
        }

        binding.llRemove.setOnClickListener {
            clickListener.onRemoveFromClassClicked(this)
        }

        binding.llDelete.setOnClickListener {
            clickListener.onDeleteClicked(this)
        }

        binding.llCancel.setOnClickListener {
            clickListener.onCancelClicked(this)
        }

        binding.llRestore.setOnClickListener {
            clickListener.onRestoreClicked(this)
        }
        binding.llEditChild.setOnClickListener {
            clickListener.onEditProfileClicked(this)
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
        fun onPromoteClicked(dialog: BottomSheetDialogFragment)
        fun onOpenProfileClicked(dialog: BottomSheetDialogFragment)
        fun onRestoreClicked(dialog: BottomSheetDialogFragment)
        fun onDeleteClicked(dialog: BottomSheetDialogFragment)
        fun onRemoveFromClassClicked(dialog: BottomSheetDialogFragment)
        fun onCancelClicked(dialog: BottomSheetDialogFragment)
        fun onEditProfileClicked(dialog: BottomSheetDialogFragment)
    }
}