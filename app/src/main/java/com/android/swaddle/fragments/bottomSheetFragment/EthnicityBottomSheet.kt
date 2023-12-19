package com.android.swaddle.fragments.bottomSheetFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter.EthnicityBottomSheetAdapter
import com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter.EthnicityList
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.EthnicityBottomSheetBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewInVisible
import com.android.swaddle.helper.viewVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class EthnicityBottomSheet(
    var baseActivity: BaseActivity,
    var list: ArrayList<EthnicityList>,
    var selectedEthnicityOtherText: String
) : BottomSheetDialogFragment() {
    private lateinit var binding: EthnicityBottomSheetBinding
    private lateinit var clickListener: ClickListener
    var adapter: EthnicityBottomSheetAdapter? = null
    var selectedEthnicity: EthnicityList? = null


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
        binding = EthnicityBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
        setRecView()

        if (selectedEthnicityOtherText != "") {
            binding.etOther.setText(selectedEthnicityOtherText)
            binding.etOther.viewVisible()
        }
    }

    private fun setRecView() {
        val manager =
            LinearLayoutManager(baseActivity, LinearLayoutManager.VERTICAL, false)
        adapter = EthnicityBottomSheetAdapter(
            baseActivity,
            list,
            object : EthnicityBottomSheetAdapter.ItemClickListener {
                override fun onItemChecked(position: Int, item: EthnicityList) {

                    val isChecked = item.isChecked
                    list.forEachIndexed { index, User ->
                        list[index].isChecked = false
                    }
                    list[position].isChecked = !isChecked
                    adapter?.notifyDataSetChanged()


                    if (item.text == "Other") {
                        binding.etOther.viewVisible()
                    } else {
                        binding.etOther.setText("")
                        binding.etOther.viewInVisible()
                    }
                }
            })
        binding.rvParents.layoutManager = manager
        binding.rvParents.adapter = adapter
    }

    private fun listener() {
        binding.ivCancel.setOnClickListener {
            dismiss()
        }
        binding.cvChoose.setOnClickListener {
            if (validate()) {
                clickListener.onChooseClicked(
                    selectedEthnicity,
                    binding.etOther.text.toString()
                )
                dismiss()
            }
        }
    }

    fun setListener(listener: ClickListener) {
        clickListener = listener
    }

    interface ClickListener {
        fun onChooseClicked(selectedEthnicity: EthnicityList?, otherText: String)
        fun onCancelClicked()
    }

    private fun validate(): Boolean {

        if (!baseActivity.isNetworkConnected()) {
            showErrorToast(baseActivity, "No internet connection!")
            return false
        }
        selectedEthnicity = list.find { it.isChecked }
        if (selectedEthnicity == null) {
            showErrorToast(baseActivity, "Please select Ethnicity first.")
            return false
        }
        if (selectedEthnicity?.text == "Other" && binding.etOther.text.toString().trim() == "") {
            showErrorToast(baseActivity, "Please Enter Ethnicity first.")
            return false
        }
        return true
    }
}