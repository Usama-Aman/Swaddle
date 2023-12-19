package com.android.swaddle.adapters.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.models.ClassroomDetails
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class SelectChildSpinnerAdapter(
    private val contex: BaseActivity,
    private val listClass: ArrayList<ChildInfo>,
    private val fromProviderAttendance: Boolean = false
) :
    ArrayAdapter<ChildInfo>(contex, 0, listClass) {

    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    fun setItems(list: ArrayList<ChildInfo>) {
        listClass.clear()
        listClass.addAll(list)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View? {

        var vv = LayoutInflater.from(contex).inflate(
            R.layout.item_child_spinner, parent, false
        )

        val textViewName = vv?.findViewById<TextView>(R.id.tvSpinner)
        val tvAttendanceStatus = vv?.findViewById<TextView>(R.id.tvAttendanceStatus)
        val ivChild = vv?.findViewById<CircleImageView>(R.id.ivChild)

        textViewName.let {
            it?.text = listClass[position].firstName + " " + listClass[position].lastName
        }

//        if (listClass[position].child_attendance != null) {
//            if (fromProviderAttendance) {
//                tvAttendanceStatus?.viewVisible()
//            } else
//                tvAttendanceStatus?.viewGone()
//        } else
//            tvAttendanceStatus?.viewGone()

        ivChild?.let {
            Glide.with(contex)
                .load(Constants.IMG_BASE_PATH + listClass.get(position).profilePicture)
                .placeholder(R.drawable.ic_acc_circle_small).into(it)
        }
        return vv
    }


}