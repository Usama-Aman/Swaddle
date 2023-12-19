package com.android.swaddle.fragments.bottomSheetFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.swaddle.R
import com.android.swaddle.activities.providers.messages.ChatActivity
import com.android.swaddle.adapters.PromoteChildAdapter
import com.android.swaddle.adapters.provider_adapter.UsersForNewChatAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.BottomSheetPromoteChildBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import org.jetbrains.anko.startActivity


open class PromoteChildBottomSheet(
    var baseActivity: BaseActivity,
    var childInfo: ChildInfo,
    var classRoomsList: ArrayList<ClassroomDetails>
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPromoteChildBinding
    private lateinit var clickListener: ClickListener
    var selectedClassRoom: ClassroomDetails? = null
    var adapter: PromoteChildAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetPromoteChildBinding.inflate(inflater, container, false)
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
            adapter = PromoteChildAdapter(baseActivity, classRoomsList, object :
                PromoteChildAdapter.ItemClickListener {
                override fun onItemChecked(position: Int, item: ClassroomDetails) {
                    var isChecked = item.isSelected
                    classRoomsList.forEachIndexed { index, classroomDetails ->
                        classRoomsList[index].isSelected = false
                    }
                    classRoomsList[position].isSelected = !isChecked
                    adapter?.setItems(classRoomsList)
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
            binding.rvClasses.layoutManager = manager
            binding.rvClasses.adapter = adapter

        } else {
            adapter?.setItems(classRoomsList)
            adapter?.notifyDataSetChanged()
        }

        if (classRoomsList.size > 0) {
            binding.noData.viewGone()
        } else {
            binding.noData.viewGone()
        }
    }

    private fun listener() {

        binding.ivCancel.setOnClickListener {
            dismiss()
        }

        binding.tvPromote.setOnClickListener {
            if (validate()) {
                val alert =
                    AlertView(
                        "Promote?",
                        "Are you sure you want to Promote ${childInfo.firstName} ${childInfo.lastName}?",
                        AlertStyle.DIALOG
                    )
                alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {
                    dismiss()
                    clickListener.onPromoteClicked(selectedClassRoom)
                    dismiss()
                })
                alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                })
                alert.show(baseActivity)
            }
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
        fun onPromoteClicked(selectedClassRoom: ClassroomDetails?)
        fun onCancelClicked()
    }

    private fun validate(): Boolean {

        if (!baseActivity.isNetworkConnected()) {
            showErrorToast(baseActivity, "No internet connection!")
            return false
        }
        selectedClassRoom = classRoomsList.find { it.isSelected }

//        val password = binding.etPassword.text.toString()
        if (selectedClassRoom == null) {

            showErrorToast(baseActivity, "Please select classroom first.")
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
}