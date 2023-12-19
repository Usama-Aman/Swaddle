package com.android.swaddle.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.databinding.DialogFragmentCenterAlertBinding
import com.android.swaddle.helper.showErrorToast

class CenterAlertDialogFragment : DialogFragment() {
    private lateinit var binding: DialogFragmentCenterAlertBinding
    private var clickListener: ClickListener? = null
    private var alertMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertMessage = arguments?.getString("message")!!

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ReportDialogTheme)
    }


    fun setListener(listener: ClickListener) {
        clickListener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_fragment_center_alert,
                container,
                false
            )
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVar()
        listener()
    }

    private fun listener() {
        binding.btnSubmit.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isEmpty()) {
                showErrorToast(requireContext(), "Please enter Message")
            } else {
                dismiss()
                clickListener?.onItemClick(message)
            }
        }


        binding.ivCross.setOnClickListener {
            dismiss()
        }
    }

    private fun initVar() {
        binding.etMessage.setText(alertMessage)

    }


    fun newInstance(alertMessage: String): CenterAlertDialogFragment {
        val args = Bundle()
        args.putString("message", alertMessage)
        val fragment = CenterAlertDialogFragment()
        fragment.arguments = args
        return fragment
    }

    interface ClickListener {
        fun onItemClick(message: String)
    }


}