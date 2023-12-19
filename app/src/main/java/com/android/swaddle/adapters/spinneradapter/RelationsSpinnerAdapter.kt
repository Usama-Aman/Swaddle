package com.android.swaddle.adapters.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import com.android.swaddle.R

class RelationsSpinnerAdapter(
    private val contex: Context,
    private val listClass: ArrayList<String>
) :
    ArrayAdapter<String>(contex, 0, listClass) {

    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var vv = convertView
        vv = LayoutInflater.from(contex).inflate(
            R.layout.item_relation_spinner, parent, false
        )

        val textViewName = vv?.findViewById<TextView>(R.id.tvSpinner)
        textViewName?.text = listClass[position]

        when {
            listClass[position] == "Mother" -> {

            }
            listClass[position] == "Father" -> {

            }
            else -> {

            }
        }


        return vv
    }


}