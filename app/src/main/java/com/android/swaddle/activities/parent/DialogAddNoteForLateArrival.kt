package com.android.swaddle.activities.parent

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.android.swaddle.databinding.DialogNoteForAttendanceDelayBinding
import com.android.swaddle.helper.showErrorToast

class DialogAddNoteForLateArrival(
    private val childId: Int, private val position: Int,
    private val addNoteLateArrivalInterface: AddNoteLateArrivalInterface
) : DialogFragment() {

    private lateinit var binding: DialogNoteForAttendanceDelayBinding
    private var isAbsent: Int = 0

    interface AddNoteLateArrivalInterface {
        fun onSaveNote(text: String, childId: Int, parentId: Int, isAbsent: Int)
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
        window.setLayout((size.x * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DialogNoteForAttendanceDelayBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.ivCross.setOnClickListener {
            dismiss()
        }

        listener()
    }

    private fun listener() {
        binding.absentSwitch.setOnCheckedChangeListener { _, isChecked ->
            isAbsent = if (isChecked) 1 else 0
        }

        binding.btnSave.setOnClickListener {
            if (binding.etNote.text.isBlank()) {
                showErrorToast(requireContext(), "Please enter note to save.")
                return@setOnClickListener
            }

            dismiss()
            addNoteLateArrivalInterface.onSaveNote(
                binding.etNote.text.toString(),
                childId,
                position,
                isAbsent
            )

        }

    }

}