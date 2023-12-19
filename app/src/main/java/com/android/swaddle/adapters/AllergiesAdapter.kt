package com.android.swaddle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R

class AllergiesAdapter(
    private val context: Context,
    private var dataList: ArrayList<String>
) :
    RecyclerView.Adapter<AllergiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_allergy,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setItems(newList: ArrayList<String>) {
        this.dataList = newList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[holder.adapterPosition]
        holder.tvTagText.text = item

//        holder.ivRemoveTag.setOnClickListener {
//            clickListener.onClickRemoveInterest(holder.adapterPosition)
//        }
//        holder.rlTags.setOnClickListener {
//            clickListener.onClickOpenInterests(
//                holder.adapterPosition,
//                item
//            )
//        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTagText: TextView = view.findViewById<TextView>(R.id.tvTagText)
        val ivRemoveTag: ImageView = view.findViewById<ImageView>(R.id.ivRemoveTag)
        val rlTags: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlTags)

    }

    interface ClickListener {
//        fun onClickRemoveInterest(position: Int)
//        fun onClickOpenInterests(position: Int, item: String)
    }
}