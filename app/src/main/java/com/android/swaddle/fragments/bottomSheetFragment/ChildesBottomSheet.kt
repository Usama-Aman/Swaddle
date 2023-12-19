package com.android.swaddle.fragments.bottomSheetFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.parents_bottom_sheet_adapter.ChildesBottomSheetAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ChildesBottomSheetBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.models.ChildInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class ChildesBottomSheet(
    var baseActivity: BaseActivity,
    var list: ArrayList<ChildInfo>
) : BottomSheetDialogFragment() {
    private lateinit var binding: ChildesBottomSheetBinding
    private lateinit var clickListener: ClickListener
    var adapter: ChildesBottomSheetAdapter? = null
    var selectedParents: ChildInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = ChildesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
        setRecyclerView()

    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager =
                LinearLayoutManager(baseActivity, LinearLayoutManager.VERTICAL, false)
            adapter = ChildesBottomSheetAdapter(baseActivity, list, object :
                ChildesBottomSheetAdapter.ItemClickListener {
                override fun onItemChecked(position: Int, item: ChildInfo) {
                    val isChecked = item.isSelected ?: false
                    list[position].isSelected = !isChecked
                    adapter?.setItems(list)
                    adapter?.notifyDataSetChanged()
                }

                override fun onAddNoteClicked(position: Int, item: ChildInfo) {

                }
            })
            binding.rvParents.layoutManager = manager
            binding.rvParents.adapter = adapter

        } else {
            adapter?.setItems(list)
            adapter?.notifyDataSetChanged()
        }

        if (list.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewGone()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation

    }

    private fun listener() {
        binding.ivCancel.setOnClickListener {
            dismiss()
        }

        binding.tvSelectNow.setOnClickListener {
            if (validate()) {
                clickListener.onSelectNowClicked(list.filter { it.isSelected == true } as ArrayList)
                dismiss()
//                val alert =
//                    AlertView(
//                        "Select Parent?",
//                        "Are you sure you want to Select ${selectedParents?.firstName} ${selectedParents?.lastName}?",
//                        AlertStyle.DIALOG
//                    )
//                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
//
//                })
//                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
//                })
//                alert.show(baseActivity)
            }
        }
    }

    private fun validate(): Boolean {

        if (!baseActivity.isNetworkConnected()) {
            showErrorToast(baseActivity, "No internet connection!")
            return false
        }
        selectedParents = list.find { it.isSelected == true }

        if (selectedParents == null) {

            showErrorToast(baseActivity, "Please select Child first.")
            return false
        }
        return true
    }

    fun setListener(listener: ClickListener) {
        clickListener = listener
    }

    interface ClickListener {
        fun onSelectNowClicked(selectedParents: ArrayList<ChildInfo>)
        fun onCancelClicked()
    }
}