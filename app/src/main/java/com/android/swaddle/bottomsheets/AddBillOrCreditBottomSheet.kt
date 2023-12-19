package com.android.swaddle.bottomsheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.R
import com.android.swaddle.databinding.DialogPromotBottomSheetBinding
import com.android.swaddle.databinding.FragmentAddBillOrCreditBottomSheetBinding
import com.android.swaddle.fragments.bottomSheetFragment.AddChildBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AddBillOrCreditBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentAddBillOrCreditBottomSheetBinding
    private lateinit var clickListener: ClickListener

    private var selectedChild = -1
    private var selectedClass = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBillOrCreditBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        binding.llCancel.setOnClickListener {
            clickListener.onCancelClicked(this)
        }
        binding.llAddBill.setOnClickListener {
            clickListener.onAddBillClicked(this)
        }
        binding.llAddCredit.setOnClickListener {
            clickListener.onAddCreditClicked(this)
        }
    }

    fun setListener(listener: ClickListener) {
        clickListener = listener
    }

    interface ClickListener {

        fun onAddBillClicked(dialog: BottomSheetDialogFragment)
        fun onAddCreditClicked(dialog: BottomSheetDialogFragment)
        fun onCancelClicked(dialog: BottomSheetDialogFragment)
    }

}