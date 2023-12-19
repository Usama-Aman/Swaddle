package com.android.swaddle.fragments.bottomSheetFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.R
import com.android.swaddle.databinding.BottomSheetAddCardBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class AddCardBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetAddCardBinding
    private lateinit var clickListener: ClickListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetAddCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        binding.tvAddBill.setOnClickListener {
            clickListener.addABill()
        }

        binding.tvAddCard.setOnClickListener {
            clickListener.addCredit()
        }


        binding.tvCancel.setOnClickListener {
            dismiss()
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
        fun addABill()
        fun addCredit()
        fun onCancelClicked()
    }
}