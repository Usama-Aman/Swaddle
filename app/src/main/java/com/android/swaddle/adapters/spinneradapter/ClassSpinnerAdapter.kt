package com.android.swaddle.adapters.spinneradapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.android.swaddle.R
import com.android.swaddle.activities.providers.class_room_ui.ClassRoomChildDetail
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassroomDetails


class ClassSpinnerAdapter(
    private val contex: Context,
    private val listClass: ArrayList<ClassroomDetails>
) : ArrayAdapter<ClassroomDetails>(contex, 0, listClass) {

    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)!!
    }

    fun setItems(list: ArrayList<ClassroomDetails>) {
        listClass.clear()
        listClass.addAll(list)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var vv = convertView
        vv = LayoutInflater.from(contex).inflate(
            R.layout.item_class_room_spinner, parent, false
        )

        val textViewName = vv?.findViewById<TextView>(R.id.tvSpinner)
        textViewName?.text = listClass[position].name

        return vv
    }
}