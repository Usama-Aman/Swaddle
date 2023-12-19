package com.android.swaddle.fragments.bottomSheetFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.adapters.PromoteChildAdapter
import com.android.swaddle.adapters.parents_bottom_sheet_adapter.ParentsBottomSheetAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.BottomSheetPromoteChildBinding
import com.android.swaddle.databinding.ParentsBottomSheetBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction


open class ParentsBottomSheet(
    var baseActivity: BaseActivity,
    var title: String = "",
    var parentsList: ArrayList<User>
) : BottomSheetDialogFragment() {
    private lateinit var binding: ParentsBottomSheetBinding
    private lateinit var clickListener: ClickListener
    var adapter: ParentsBottomSheetAdapter? = null
    var selectedParents: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = ParentsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
        setRecyclerView()

        if (title != "") {
            binding.tvTitle.text = title
        }
    }

    private fun setRecyclerView() {
        if (adapter == null) {
            val manager =
                LinearLayoutManager(baseActivity, LinearLayoutManager.VERTICAL, false)
            adapter = ParentsBottomSheetAdapter(baseActivity, parentsList, object :
                ParentsBottomSheetAdapter.ItemClickListener {
                override fun onItemChecked(position: Int, item: User) {
                    val isChecked = item.isUserSelected
                    parentsList.forEachIndexed { index, User ->
                        parentsList[index].isUserSelected = false
                    }
                    parentsList[position].isUserSelected = !isChecked
                    adapter?.setItems(parentsList)
                    adapter?.notifyDataSetChanged()
                }

//                override fun onItemNotify(position: Int, item: ClassroomDetails) {
//                    classRoomsList.forEachIndexed { index, classroomDetails ->
//                        classRoomsList[index].isSelected = false
//                    }
//                    classRoomsList[position].isSelected = true
//                    adapter?.setItems(classRoomsList)
//                    adapter?.notifyDataSetChanged()
//                }
            })
            binding.rvParents.layoutManager = manager
            binding.rvParents.adapter = adapter

        } else {
            adapter?.setItems(parentsList)
            adapter?.notifyDataSetChanged()
        }

        if (parentsList.size > 0) {
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
                clickListener.onSelectNowClicked(selectedParents)
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
        selectedParents = parentsList.find { it.isUserSelected }

//        val password = binding.etPassword.text.toString()
        if (selectedParents == null) {

            showErrorToast(baseActivity, "Please select Parent first.")
            return false
        }
//        if (!email.isValidEmail()) {
////            binding.etEmail.error = "Please enter valid email address"
//            binding.etEmail.requestFocus()
//            showErrorToast(baseActivity, "Please enter email address")
//            return false
//        }
//
//        if (password.isEmpty()) {
////            binding.etPassword.error = "Please enter password"
//            binding.etPassword.requestFocus()
//            showErrorToast(baseActivity, "Please enter password")
//            return false
//        }
//        if (password.length < 6) {
////            binding.etPassword.error = "Minimum Password length should be 6"
//            binding.etPassword.requestFocus()
//            showErrorToast(baseActivity, "Minimum Password length should be 6")
//            return false
//        }
        return true
    }

    fun setListener(listener: ClickListener) {
        clickListener = listener
    }

    interface ClickListener {
        fun onSelectNowClicked(selectedParents: User?)
        fun onCancelClicked()
    }

}