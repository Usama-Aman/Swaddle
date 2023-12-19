package com.android.swaddle.fragments.dialog

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.FragmentEpiPenAcknowledgementPopupBinding
import com.android.swaddle.databinding.PopupCertificationsBinding
import com.android.swaddle.fragments.fragment_certifications.AddCertificationsPopup
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible


class EpiPenAcknowledgementPopup : DialogFragment() {
    private lateinit var binding: FragmentEpiPenAcknowledgementPopupBinding

    var clickListeners: ClickListener? = null

    var baseActivity: BaseActivity? = null

    companion object {


        var fromList : Boolean = false

        fun getInstance(fromList: Boolean): EpiPenAcknowledgementPopup {
            var epiPenAcknowledgement = EpiPenAcknowledgementPopup()
            epiPenAcknowledgement.isCancelable = false
            this.fromList = fromList
            return epiPenAcknowledgement
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.getWindowManager().getDefaultDisplay()
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window!!.setLayout((size.x * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window!!.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            FragmentEpiPenAcknowledgementPopupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.ivCross.setOnClickListener {
            dismiss()
        }
        binding.tviAgree.setOnClickListener {
            dismiss()
        }
        listener()


        binding.tvBelowAck.text =
            if(fromList){"Parent must provide Epi-Pen on first day of attendance."}
        else {"Parent will provide Epi-Pen on 1st day of enrollment."}

    }

    private fun listener() {
    }

    private fun showProgressBar() {
        binding.tviAgree.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tviAgree.viewVisible()
        binding.progressbar.viewGone()
    }

    interface ClickListener {
        fun onIAgreeClicked(dialogFragment: DialogFragment?)
        fun onCancelClicked()
    }


}