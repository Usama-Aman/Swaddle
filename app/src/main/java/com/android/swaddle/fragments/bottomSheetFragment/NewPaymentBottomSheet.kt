package com.android.swaddle.fragments.bottomSheetFragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.swaddle.R
import com.android.swaddle.databinding.DialogNewPaymentMethodBinding
import com.android.swaddle.payment_screens.AddPaymentMethodsActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class NewPaymentBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: DialogNewPaymentMethodBinding
    private lateinit var clickListener: ClickListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogNewPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {
        binding.ivCross.setOnClickListener {
            dismiss()
        }


        binding.btnContinue.setOnClickListener {
            dismiss()
            val intent = Intent(context, AddPaymentMethodsActivity::class.java)
            startActivity(intent)
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
        fun onRemoveClicked(dialog: BottomSheetDialogFragment)
        fun onCancelClicked(dialog: BottomSheetDialogFragment)
    }
}