package com.android.swaddle.adapters.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.android.swaddle.R
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ClassroomDetails

class BillTypeSpinnerAdapter(
    private val contex: Context,
    private val listClass: ArrayList<BillTypeList>
) : ArrayAdapter<BillTypeList>(contex, 0, listClass) {

    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)!!
    }

    fun setItems(list: ArrayList<BillTypeList>) {
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
        textViewName?.text = listClass[position].text
        textViewName?.textSize = 16F
        val view = vv?.findViewById<View>(R.id.viewSp)
        view?.viewGone()
      /*  if (position == listClass.size-1){
            view?.viewGone()
        }else{
            view?.viewVisible()
        }*/

        return vv
    }
}
data class BillTypeList(var text : String)