@file:Suppress("unused")

package com.android.swaddle.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.wang.avi.AVLoadingIndicatorView


class ProgressDialog : DialogFragment {
    internal lateinit var txtProgress: AppCompatTextView
    private lateinit var dialog: AlertDialog
    private lateinit var indicator: AVLoadingIndicatorView

    constructor() : super()

    @SuppressLint("ValidFragment", "ClickableViewAccessibility", "InflateParams")
    constructor(activity: Activity) {
        val factory = LayoutInflater.from(activity)
        val mainDialog = factory.inflate(R.layout.view_progress_dark, null)
        dialog = AlertDialog.Builder(activity).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val windowView = dialog.window!!.attributes
        windowView.gravity = Gravity.CENTER
        txtProgress = mainDialog.findViewById(R.id.txt_progress)
        indicator = mainDialog.findViewById(R.id.indicator)
        indicator.show()
        txtProgress.setOnTouchListener { _, _ -> false }
        dialog.setView(mainDialog)
        dialog.setOnKeyListener { _, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val factory = LayoutInflater.from(context)
        val mainDialog = factory.inflate(R.layout.view_progress_dark, null)
        dialog = AlertDialog.Builder(requireContext()).create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val windowView = dialog.window!!.attributes
        txtProgress = mainDialog.findViewById(R.id.txt_progress)
        indicator = mainDialog.findViewById(R.id.indicator)
        indicator.show()
        windowView.gravity = Gravity.CENTER
        dialog.setView(mainDialog)
        dialog.setOnKeyListener { _, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP }

        return dialog
    }

    companion object {

        fun newInstance(): ProgressDialog {
            val frag = ProgressDialog()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }

}